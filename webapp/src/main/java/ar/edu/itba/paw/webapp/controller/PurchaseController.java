package ar.edu.itba.paw.webapp.controller;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.PurchaseStatus;
import ar.edu.itba.paw.services.PurchasePaymentProof;
import ar.edu.itba.paw.services.PurchaseService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.webapp.auth.PawAuthUser;
import ar.edu.itba.paw.webapp.form.PurchaseCreateForm;
import ar.edu.itba.paw.webapp.form.PurchaseStatusForm;
import ar.edu.itba.paw.webapp.validation.PaymentProofValidator;
import ar.edu.itba.paw.webapp.validation.PaymentProofValidator.ValidatedPaymentProof;

@Controller
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final ReviewService reviewService;

    @Autowired
    public PurchaseController(
            final PurchaseService purchaseService,
            final ReviewService reviewService) {
        this.purchaseService = purchaseService;
        this.reviewService = reviewService;
    }

    @RequestMapping(value = "/purchases", method = RequestMethod.POST)
    public ModelAndView createPurchase(
            @AuthenticationPrincipal PawAuthUser authUser,
            @Valid @ModelAttribute("purchaseCreateForm") final PurchaseCreateForm form,
            final BindingResult errors) {

        if (errors.hasErrors()) {
            if (form.getProductId() == null) {
                return new ModelAndView("redirect:/?purchaseError=1");
            }
            return new ModelAndView("redirect:/products/" + form.getProductId() + "?purchaseError=1");
        }

        if (!purchaseService.canCreatePurchases(authUser.getUser().getId())) {
            return new ModelAndView(
                    "redirect:/profile?tab=mydata&missingData=purchase&productId=" + form.getProductId());
        }

        final Purchase purchase;
        try {
            purchase = purchaseService.createPurchase(form.getProductId(), authUser.getUser().getId());
        } catch (IllegalStateException e) {
            return new ModelAndView("redirect:/?purchaseUnavailable=1");
        } catch (IllegalArgumentException e) {
            return new ModelAndView("redirect:/products/" + form.getProductId() + "?purchaseError=1");
        }
        return new ModelAndView("redirect:/purchases/" + purchase.getPurchaseId());
    }

    @RequestMapping(value = "/purchases/{id:\\d+}", method = RequestMethod.GET)
    public ModelAndView getPurchase(
            @AuthenticationPrincipal PawAuthUser authUser,
            @PathVariable("id") final Long id,
            @ModelAttribute("purchaseStatusForm") final PurchaseStatusForm form) {
        return buildPurchaseView(authUser, id);
    }

    @RequestMapping(value = "/purchases/{id:\\d+}/status", method = RequestMethod.POST)
    public ModelAndView updateStatus(
            @AuthenticationPrincipal PawAuthUser authUser,
            @PathVariable("id") final Long id,
            @Valid @ModelAttribute("purchaseStatusForm") final PurchaseStatusForm form,
            final BindingResult errors) {
        if (errors.hasErrors()) {
            return buildPurchaseView(authUser, id);
        }

        final PurchaseStatus statusObj = PurchaseStatus.valueOf(form.getNewStatus());

        ValidatedPaymentProof proof = null;
        if (statusObj == PurchaseStatus.PAID) {
            proof = PaymentProofValidator.validate(form.getProofFile());
        }

        final Purchase updated;
        try {
            updated = purchaseService.updateStatus(
                    id,
                    authUser.getUser().getId(),
                    statusObj,
                    proof != null ? proof.getData() : null,
                    proof != null ? proof.getContentType() : null,
                    proof != null ? proof.getFileName() : null);
        } catch (IllegalStateException e) {
            return new ModelAndView("redirect:/purchases/" + id + "?expired=1");
        }

        if (statusObj == PurchaseStatus.DELIVERED && authUser.getUser().getId().equals(updated.getBuyerId())) {
            return new ModelAndView("redirect:/purchases/" + id + "/review");
        }

        return new ModelAndView("redirect:/purchases/" + id + "?updated=1");
    }

    @RequestMapping(value = "/purchases/{id:\\d+}/proof", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<byte[]> downloadPaymentProof(
            @AuthenticationPrincipal PawAuthUser authUser,
            @PathVariable("id") final Long id) {

        final PurchasePaymentProof proof;
        try {
            final Optional<PurchasePaymentProof> proofOpt = purchaseService.findPaymentProofForSeller(
                    id,
                    authUser.getUser().getId());
            if (!proofOpt.isPresent()) {
                return ResponseEntity.notFound().build();
            }
            proof = proofOpt.get();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        }

        final byte[] proofData = proof.getData();
        final String safeFileName = PaymentProofValidator.safeFileName(proof.getFileName());
        return PaymentProofValidator.detectSafeContentType(proofData)
                .map(contentType -> ResponseEntity.ok()
                        .header("X-Content-Type-Options", "nosniff")
                        .header("Content-Disposition", "attachment; filename=\"" + safeFileName + "\"")
                        .contentType(MediaType.parseMediaType(contentType))
                        .contentLength(proofData.length)
                        .body(proofData))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private ModelAndView buildPurchaseView(final PawAuthUser authUser, final Long id) {
        final PurchaseService.PurchaseDetails details = purchaseService.getPurchaseDetailsForUser(
                id,
                authUser.getUser().getId());
        final Purchase purchase = details.getPurchase();

        final ModelAndView mav = new ModelAndView("purchase-panel");
        mav.addObject("purchase", purchase);
        mav.addObject("product", details.getProduct());
        mav.addObject("orderBuyer", details.getBuyer());
        mav.addObject("orderSeller", details.getSeller());
        mav.addObject("isBuyer", details.isBuyerView());
        mav.addObject("isSeller", details.isSellerView());

        if (purchase.getStatus() == PurchaseStatus.PENDING && purchase.getReservedUntil() != null) {
            long remainingSeconds = Duration.between(LocalDateTime.now(), purchase.getReservedUntil()).getSeconds();
            if (remainingSeconds < 0) {
                remainingSeconds = 0;
            }
            mav.addObject("remainingSeconds", remainingSeconds);
        }

        mav.addObject("hasPaymentProof", details.hasPaymentProof());
        mav.addObject("paymentProofFileName", details.getPaymentProofFileName());

        if (details.isBuyerView() && purchase.getStatus() == PurchaseStatus.DELIVERED) {
            mav.addObject("hasReview", reviewService.findByPurchaseId(id).isPresent());
        }

        return mav;
    }
}

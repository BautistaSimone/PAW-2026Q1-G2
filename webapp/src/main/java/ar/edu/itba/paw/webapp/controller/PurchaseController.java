package ar.edu.itba.paw.webapp.controller;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.PurchaseStatus;
import ar.edu.itba.paw.services.PurchaseDisplayService;
import ar.edu.itba.paw.services.PurchasePaymentProof;
import ar.edu.itba.paw.services.PurchaseService;
import ar.edu.itba.paw.services.ProductService;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.webapp.auth.PawAuthUser;
import ar.edu.itba.paw.webapp.form.PurchaseCreateForm;
import ar.edu.itba.paw.webapp.form.PurchaseStatusForm;
import ar.edu.itba.paw.webapp.validation.PaymentProofValidator;
import ar.edu.itba.paw.webapp.validation.PaymentProofValidator.ValidatedPaymentProof;

@Controller
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final ProductService productService;
    private final PurchaseDisplayService purchaseDisplayService;

    @Autowired
    public PurchaseController(
            final PurchaseService purchaseService,
            final ProductService productService,
            final PurchaseDisplayService purchaseDisplayService) {
        this.purchaseService = purchaseService;
        this.productService = productService;
        this.purchaseDisplayService = purchaseDisplayService;
    }

    @RequestMapping(value = "/purchases", method = RequestMethod.POST)
    public ModelAndView createPurchase(
            @AuthenticationPrincipal PawAuthUser authUser,
            @Valid @ModelAttribute("purchaseCreateForm") final PurchaseCreateForm form,
            final BindingResult errors,
            @RequestParam(value = "back", required = false) final String back) {

        if (errors.hasErrors()) {
            if (form.getProductId() == null) {
                return new ModelAndView("redirect:/?purchaseError=1");
            }
            String target = "redirect:/products/" + form.getProductId() + "?purchaseError=1";
            if (back != null && !back.isBlank()) {
                target += "&back=" + java.net.URLEncoder.encode(back, java.nio.charset.StandardCharsets.UTF_8);
            }
            return new ModelAndView(target);
        }

        if (!purchaseService.canCreatePurchases(authUser.getUser().getId())) {
            return new ModelAndView(
                    "redirect:/profile?tab=mydata&missingData=purchase&productId=" + form.getProductId());
        }

        final Product product = productService.findByIdIfAvailable(form.getProductId()).orElse(null);
        if (product == null || product.getUserId() == authUser.getUser().getId()) {
            String target = "redirect:/products/" + form.getProductId() + "?purchaseError=1";
            if (back != null && !back.isBlank()) {
                target += "&back=" + java.net.URLEncoder.encode(back, java.nio.charset.StandardCharsets.UTF_8);
            }
            return new ModelAndView(target);
        }

        if (product.getStock() <= 0) {
            return new ModelAndView("redirect:/?purchaseUnavailable=1");
        }

        final Purchase purchase = purchaseService.createPurchase(form.getProductId(), authUser.getUser().getId());
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

        byte[] proofData = null;
        String proofContentType = null;
        String proofFileName = null;
        
        if (statusObj == PurchaseStatus.PAID && form.getProofFile() != null && !form.getProofFile().isEmpty()) {
            try {
                proofData = form.getProofFile().getBytes();
                proofContentType = PaymentProofValidator.detectSafeContentType(proofData).orElse("application/pdf");
                proofFileName = PaymentProofValidator.safeFileName(form.getProofFile().getOriginalFilename());
            } catch (java.io.IOException e) {
                throw new RuntimeException("Failed to read uploaded payment proof bytes", e);
            }
        }

        final Purchase updated = purchaseService.updateStatus(
                id,
                authUser.getUser().getId(),
                statusObj,
                proofData,
                proofContentType,
                proofFileName);

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
        final PurchaseDisplayService.PurchaseDisplay display =
                purchaseDisplayService.getPurchaseDisplay(id, authUser.getUser().getId());

        final ModelAndView mav = new ModelAndView("purchase-panel");
        mav.addObject("purchase", display.getPurchase());
        mav.addObject("product", display.getProduct());
        mav.addObject("orderBuyer", display.getOrderBuyer());
        mav.addObject("orderSeller", display.getOrderSeller());
        mav.addObject("isBuyer", display.isBuyer());
        mav.addObject("isSeller", display.isSeller());

        if (display.getRemainingSeconds() != null) {
            mav.addObject("remainingSeconds", display.getRemainingSeconds());
        }

        mav.addObject("hasPaymentProof", display.isHasPaymentProof());
        mav.addObject("paymentProofFileName", display.getPaymentProofFileName());
        mav.addObject("hasReview", display.isHasReview());

        return mav;
    }
}

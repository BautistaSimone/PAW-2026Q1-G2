package ar.edu.itba.paw.webapp.controller;

import java.time.Duration;
import java.time.LocalDateTime;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ResponseBody;

import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.PurchaseStatus;
import ar.edu.itba.paw.services.ProductService;
import ar.edu.itba.paw.services.PurchaseService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.webapp.auth.PawAuthUser;
import ar.edu.itba.paw.webapp.form.PurchaseCreateForm;
import ar.edu.itba.paw.webapp.form.PurchaseStatusForm;
import ar.edu.itba.paw.webapp.validation.PaymentProofValidator;
import ar.edu.itba.paw.webapp.validation.PaymentProofValidator.ValidatedPaymentProof;

@Controller
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final ProductService productService;
    private final ReviewService reviewService;
    private final UserService userService;

    @Autowired
    public PurchaseController(
            final PurchaseService purchaseService,
            final ProductService productService,
            final ReviewService reviewService,
            final UserService userService) {
        this.purchaseService = purchaseService;
        this.productService = productService;
        this.reviewService = reviewService;
        this.userService = userService;
    }

    @RequestMapping(value = "/purchases", method = RequestMethod.POST)
    public ModelAndView createPurchase(
            @AuthenticationPrincipal PawAuthUser authUser,
            @Valid @ModelAttribute("purchaseCreateForm") final PurchaseCreateForm form,
            final BindingResult errors) {

        if (authUser == null) {
            return new ModelAndView("redirect:/login");
        }

        if (errors.hasErrors()) {
            if (form.getProductId() == null) {
                return new ModelAndView("redirect:/?purchaseError=1");
            }
            return new ModelAndView("redirect:/products/" + form.getProductId() + "?purchaseError=1");
        }

        final User user = userService.findById(authUser.getUser().getId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        if (!user.hasCompleteBuyerDataForPurchase()) {
            return new ModelAndView(
                    "redirect:/profile?tab=mydata&missingData=purchase&productId=" + form.getProductId());
        }

        final Purchase purchase;
        try {
            purchase = purchaseService.createPurchase(form.getProductId(), user.getId());
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
        if (authUser == null) {
            return new ModelAndView("redirect:/login");
        }

        final Long userId = authUser.getUser().getId();

        Purchase purchase = purchaseService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Purchase not found"));

        final boolean isBuyer = userId.equals(purchase.getBuyerId());
        final boolean isSeller = userId.equals(purchase.getSellerId());

        if (!isBuyer && !isSeller) {
            throw new IllegalArgumentException("You are not authorized to view this purchase");
        }

        Product product = productService.findById(purchase.getProductId())
                .orElseThrow(() -> new IllegalStateException("Product missing"));

        final User orderBuyer = userService.findById(purchase.getBuyerId())
                .orElseThrow(() -> new IllegalStateException("Buyer missing"));
        /* seller_user_id en la compra (no solo product.user_id) + nombres explícitos para evitar sombras en EL/JSP */
        final User orderSeller = userService.findById(purchase.getSellerId())
                .orElseGet(() -> userService.findById(product.getUserId())
                        .orElseThrow(() -> new IllegalStateException("Seller missing")));

        ModelAndView mav = new ModelAndView("purchase-panel");
        mav.addObject("purchase", purchase);
        mav.addObject("product", product);
        mav.addObject("orderBuyer", orderBuyer);
        mav.addObject("orderSeller", orderSeller);
        mav.addObject("isBuyer", isBuyer);
        mav.addObject("isSeller", isSeller);

        if (purchase.getStatus() == PurchaseStatus.PENDING && purchase.getReservedUntil() != null) {
            long remainingSeconds = Duration.between(LocalDateTime.now(), purchase.getReservedUntil()).getSeconds();
            if (remainingSeconds < 0) {
                remainingSeconds = 0;
            }
            mav.addObject("remainingSeconds", remainingSeconds);
        }

        final boolean hasPaymentProof = purchase.getPaymentProof() != null
                && purchase.getPaymentProof().length > 0
                && purchase.getPaymentProofContentType() != null;
        mav.addObject("hasPaymentProof", hasPaymentProof);
        mav.addObject("paymentProofFileName", purchase.getPaymentProofFileName());

        if (isBuyer && purchase.getStatus() == PurchaseStatus.DELIVERED) {
            mav.addObject("hasReview", reviewService.findByPurchaseId(id).isPresent());
        }

        return mav;
    }

    @RequestMapping(value = "/purchases/{id:\\d+}/status", method = RequestMethod.POST)
    public ModelAndView updateStatus(
            @AuthenticationPrincipal PawAuthUser authUser,
            @PathVariable("id") final Long id,
            @Valid @ModelAttribute("purchaseStatusForm") final PurchaseStatusForm form,
            final BindingResult errors) {
        if (authUser == null) {
            return new ModelAndView("redirect:/login");
        }

        if (errors.hasErrors()) {
            return getPurchase(authUser, id, form);
        }

        // Status was validated by PurchaseStatusFormValidator — parse directly.
        final PurchaseStatus statusObj = PurchaseStatus.valueOf(form.getNewStatus());

        // Proof was validated by PurchaseStatusFormValidator — read directly if PAID.
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
                proof != null ? proof.getFileName() : null
            );
        } catch (IllegalStateException e) {
            return new ModelAndView("redirect:/purchases/" + id + "?expired=1");
        }

        if (statusObj == PurchaseStatus.DELIVERED) {
            final boolean isBuyer = authUser.getUser().getId().equals(updated.getBuyerId());
            if (isBuyer) {
                return new ModelAndView("redirect:/purchases/" + id + "/review");
            }
        }

        return new ModelAndView("redirect:/purchases/" + id + "?updated=1");
    }

    @RequestMapping(value = "/purchases/{id:\\d+}/proof", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<byte[]> downloadPaymentProof(
            @AuthenticationPrincipal PawAuthUser authUser,
            @PathVariable("id") final Long id) {
        if (authUser == null) {
            return ResponseEntity.status(401).build();
        }

        final Purchase purchase = purchaseService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Purchase not found"));

        final Long userId = authUser.getUser().getId();
        if (!userId.equals(purchase.getSellerId())) {
            return ResponseEntity.status(403).build();
        }

        if (purchase.getPaymentProof() == null || purchase.getPaymentProof().length == 0) {
            return ResponseEntity.notFound().build();
        }

        final String safeFileName = PaymentProofValidator.safeFileName(purchase.getPaymentProofFileName());
        return PaymentProofValidator.detectSafeContentType(purchase.getPaymentProof())
            .map(contentType -> ResponseEntity.ok()
                .header("X-Content-Type-Options", "nosniff")
                .header("Content-Disposition", "attachment; filename=\"" + safeFileName + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .contentLength(purchase.getPaymentProof().length)
                .body(purchase.getPaymentProof()))
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}

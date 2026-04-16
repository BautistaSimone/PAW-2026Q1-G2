package ar.edu.itba.paw.webapp.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import ar.edu.itba.paw.models.User;
import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.PurchaseStatus;
import ar.edu.itba.paw.services.ProductService;
import ar.edu.itba.paw.services.PurchaseService;
import ar.edu.itba.paw.webapp.auth.PawAuthUser;
import ar.edu.itba.paw.webapp.form.PurchaseCreateForm;
import ar.edu.itba.paw.webapp.form.PurchaseStatusForm;

@Controller
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final ProductService productService;

    @Autowired
    public PurchaseController(final PurchaseService purchaseService, final ProductService productService) {
        this.purchaseService = purchaseService;
        this.productService = productService;
    }

    @RequestMapping(value = "/purchases", method = RequestMethod.POST)
    public ModelAndView createPurchase(
        @AuthenticationPrincipal PawAuthUser authUser,
        @Valid @ModelAttribute("purchaseCreateForm") final PurchaseCreateForm form,
        final BindingResult errors
    ) {

        if (authUser == null) {
            return new ModelAndView("redirect:/login");
        }

        if (errors.hasErrors()) {
            return new ModelAndView("redirect:/products/" + form.getProductId() + "?error=1");
        }
        
        User user = authUser.getUser();

        Purchase purchase = purchaseService.createPurchase(form.getProductId(), user.getId());
        return new ModelAndView("redirect:/purchases/" + purchase.getPurchaseId() + "?token=" + purchase.getBuyerToken());
    }

    @RequestMapping(value = "/purchases/{id}", method = RequestMethod.GET)
    public ModelAndView getPurchase(
        @PathVariable("id") final Long id,
        @RequestParam("token") final String token,
        @ModelAttribute("purchaseStatusForm") final PurchaseStatusForm form
    ) {
        Purchase purchase = purchaseService.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Purchase not found"));

        if (!token.equals(purchase.getBuyerToken()) && !token.equals(purchase.getSellerToken())) {
            throw new IllegalArgumentException("Invalid token for this purchase");
        }

        Product product = productService.findById(purchase.getProductId())
            .orElseThrow(() -> new IllegalStateException("Product missing"));

        ModelAndView mav = new ModelAndView("purchase-panel");
        mav.addObject("purchase", purchase);
        mav.addObject("product", product);
        mav.addObject("isBuyer", token.equals(purchase.getBuyerToken()));
        mav.addObject("isSeller", token.equals(purchase.getSellerToken()));
        mav.addObject("token", token);
        return mav;
    }

    @RequestMapping(value = "/purchases/{id}/status", method = RequestMethod.POST)
    public ModelAndView updateStatus(
        @PathVariable("id") final Long id,
        @Valid @ModelAttribute("purchaseStatusForm") final PurchaseStatusForm form,
        final BindingResult errors
    ) {
        if (errors.hasErrors()) {
            return getPurchase(id, form.getToken(), form);
        }

        PurchaseStatus statusObj;
        try {
            statusObj = PurchaseStatus.valueOf(form.getNewStatus());
        } catch (IllegalArgumentException e) {
            errors.rejectValue("newStatus", "Pattern.purchaseForm.newStatus");
            return getPurchase(id, form.getToken(), form);
        }

        purchaseService.updateStatus(id, form.getToken(), statusObj);
        return new ModelAndView("redirect:/purchases/" + id + "?token=" + form.getToken() + "&updated=1");
    }
}

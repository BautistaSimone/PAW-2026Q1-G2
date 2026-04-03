package ar.edu.itba.paw.webapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import ar.edu.itba.paw.models.Product;
import ar.edu.itba.paw.models.Purchase;
import ar.edu.itba.paw.models.PurchaseStatus;
import ar.edu.itba.paw.services.ProductService;
import ar.edu.itba.paw.services.PurchaseService;

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
        @RequestParam("productId") final Long productId,
        @RequestParam("buyerEmail") final String buyerEmail
    ) {
        Purchase purchase = purchaseService.createPurchase(productId, buyerEmail);
        return new ModelAndView("redirect:/purchases/" + purchase.getPurchaseId() + "?token=" + purchase.getBuyerToken());
    }

    @RequestMapping(value = "/purchases/{id}", method = RequestMethod.GET)
    public ModelAndView getPurchase(
        @PathVariable("id") final Long id,
        @RequestParam("token") final String token
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
        @RequestParam("token") final String token,
        @RequestParam("newStatus") final String newStatus
    ) {
        PurchaseStatus statusObj = PurchaseStatus.valueOf(newStatus);
        purchaseService.updateStatus(id, token, statusObj);
        return new ModelAndView("redirect:/purchases/" + id + "?token=" + token + "&updated=1");
    }
}

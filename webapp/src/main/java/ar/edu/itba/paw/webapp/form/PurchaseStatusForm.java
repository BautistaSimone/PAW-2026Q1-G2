package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.NotBlank;

public class PurchaseStatusForm {

    @NotBlank
    private String token;

    @NotBlank
    private String newStatus;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }
}

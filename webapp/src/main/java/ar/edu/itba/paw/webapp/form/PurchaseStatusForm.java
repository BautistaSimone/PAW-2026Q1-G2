package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.NotBlank;

public class PurchaseStatusForm {

    @NotBlank
    private String newStatus;

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }
}

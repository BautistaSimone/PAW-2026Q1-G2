package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.NotBlank;

import org.springframework.web.multipart.MultipartFile;

import ar.edu.itba.paw.webapp.validation.ValidPurchaseStatusForm;
import ar.edu.itba.paw.webapp.validation.ValidPaymentProof;

@ValidPurchaseStatusForm
public class PurchaseStatusForm {

    @NotBlank
    private String newStatus;

    @ValidPaymentProof
    private MultipartFile proofFile;

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public MultipartFile getProofFile() {
        return proofFile;
    }

    public void setProofFile(MultipartFile proofFile) {
        this.proofFile = proofFile;
    }
}

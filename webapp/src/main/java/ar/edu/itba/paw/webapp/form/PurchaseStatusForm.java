package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.NotNull;

import org.springframework.web.multipart.MultipartFile;

import ar.edu.itba.paw.models.PurchaseStatus;
import ar.edu.itba.paw.webapp.validation.ValidPurchaseStatusForm;
import ar.edu.itba.paw.webapp.validation.ValidPaymentProof;

@ValidPurchaseStatusForm
public class PurchaseStatusForm {

    @NotNull
    private PurchaseStatus newStatus;

    @ValidPaymentProof
    private MultipartFile proofFile;

    public PurchaseStatus getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(PurchaseStatus newStatus) {
        this.newStatus = newStatus;
    }

    public MultipartFile getProofFile() {
        return proofFile;
    }

    public void setProofFile(MultipartFile proofFile) {
        this.proofFile = proofFile;
    }
}

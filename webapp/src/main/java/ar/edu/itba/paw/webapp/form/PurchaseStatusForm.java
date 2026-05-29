package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.NotBlank;

import org.springframework.web.multipart.MultipartFile;

public class PurchaseStatusForm {

    @NotBlank
    private String newStatus;

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

package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.validation.constraints.Pattern;

import ar.edu.itba.paw.webapp.validation.FieldMatch;

@FieldMatch(first = "newPassword", second = "newPasswordConfirm", message = "{Mismatch.authForm.password}")
public class UpdatePasswordForm {

    @NotBlank(message = "{NotBlank.authForm.password}")
    @Size(min = 8, max = 100, message = "{Size.authForm.password}")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$", message = "{Pattern.authForm.password}")
    private String newPassword;

    @NotBlank(message = "{NotBlank.authForm.password}")
    private String newPasswordConfirm;

    private String token;

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(final String newPassword) {
        this.newPassword = newPassword;
    }

    public String getNewPasswordConfirm() {
        return newPasswordConfirm;
    }

    public void setNewPasswordConfirm(final String newPasswordConfirm) {
        this.newPasswordConfirm = newPasswordConfirm;
    }

    public String getToken() {
        return token;
    }

    public void setToken(final String token) {
        this.token = token;
    }

}

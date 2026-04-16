package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import javax.validation.constraints.Pattern;
import ar.edu.itba.paw.webapp.validation.FieldMatch;

@FieldMatch(
    first = "password", 
    second = "confirmPassword", 
    message = "{Mismatch.authForm.password}"
)
public class RegisterForm {

    @Email(message = "{Email.authForm.email}")
    @NotBlank(message = "{NotBlank.authForm.email}")
    @NotEmpty(message = "{NotEmpty.authForm.email}")
    @Size(max = 254)
    private String email;

    @NotBlank(message = "{NotBlank.authForm.username}")
    @Size(min = 3, max = 30, message = "{Size.authForm.username}")
    @Pattern(
        regexp = "^[a-zA-Z0-9_.-]+$", 
        message = "{Pattern.authForm.username}"
    )
    private String username;

    @NotBlank(message = "{NotBlank.authForm.password}")
    @Size(min = 8, max = 100, message = "{Size.authForm.password}")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
        message = "{Pattern.authForm.password}"
    )
    private String password;

    @NotBlank(message = "{NotBlank.authForm.password}")
    private String confirmPassword;

    public RegisterForm() {}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
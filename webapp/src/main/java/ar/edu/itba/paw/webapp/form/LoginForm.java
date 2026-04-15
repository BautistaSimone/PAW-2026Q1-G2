package ar.edu.itba.paw.webapp.forms;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import javax.validation.constraints.Pattern;

public class LoginForm {

    @Email(message = "{Email.authForm.email}")
    @NotBlank(message = "{NotBlank.authForm.email}")
    @NotEmpty(message = "{NotEmpty.authForm.email}")
    @Size(max = 254)
    private String email;

    @NotBlank(message = "{NotBlank.authForm.password}")
    @Size(min = 8, max = 100, message = "{Size.authForm.password}")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
        message = "{Pattern.authForm.password}"
    )
    private String password;
    private boolean rememberMe;

    // Getters & Setters
    public String getEmail() { 
        return email; 
    }

    public String getPassword() { 
        return password; 
    }
    
    public boolean getRememberMe() {
        return rememberMe;
    }

    public void setEmail(String email) { 
        this.email = email; 
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setRememberMe(boolean rememberMe) {
        this.rememberMe = rememberMe;
    }
}


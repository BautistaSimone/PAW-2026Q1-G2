package ar.edu.itba.paw.webapp.forms;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


public class LoginForm {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 6, max = 100)
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


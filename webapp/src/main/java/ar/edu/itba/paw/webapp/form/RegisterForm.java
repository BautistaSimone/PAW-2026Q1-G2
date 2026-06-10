package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import javax.validation.constraints.Pattern;

import ar.edu.itba.paw.webapp.validation.EmailAvailable;
import ar.edu.itba.paw.webapp.validation.FieldMatch;

@FieldMatch(
    first = "password",
    second = "confirmPassword",
    message = "{Mismatch.authForm.password}"
)
public class RegisterForm {

    @NotBlank(message = "{NotBlank.registerForm.firstName}")
    @Size(max = 100, message = "{Size.registerForm.firstName}")
    private String firstName;

    @NotBlank(message = "{NotBlank.registerForm.lastName}")
    @Size(max = 100, message = "{Size.registerForm.lastName}")
    private String lastName;

    @EmailAvailable
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

    @Size(max = 255, message = "{Size.registerForm.streetName}")
    private String streetName;

    @Size(max = 20, message = "{Size.registerForm.streetNumber}")
    @Pattern(regexp = "^$|^[1-9]\\d*$", message = "{Pattern.registerForm.streetNumber}")
    private String streetNumber;

    @Size(max = 100, message = "{Size.registerForm.neighborhood}")
    private String neighborhood;

    @Size(max = 100, message = "{Size.registerForm.province}")
    private String province;

    @Size(max = 500, message = "{Size.registerForm.extraAddressInfo}")
    private String extraAddressInfo;

    @Size(max = 22, message = "{Size.registerForm.cbuCvu}")
    @Pattern(regexp = "^$|^\\d{22}$", message = "{Pattern.registerForm.cbuCvu}")
    private String cbuCvu;

    public RegisterForm() {}

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(final String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(final String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(final String streetName) {
        this.streetName = streetName;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public void setStreetNumber(final String streetNumber) {
        this.streetNumber = streetNumber;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(final String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(final String province) {
        this.province = province;
    }

    public String getExtraAddressInfo() {
        return extraAddressInfo;
    }

    public void setExtraAddressInfo(final String extraAddressInfo) {
        this.extraAddressInfo = extraAddressInfo;
    }

    public String getCbuCvu() {
        return cbuCvu;
    }

    public void setCbuCvu(final String cbuCvu) {
        this.cbuCvu = cbuCvu;
    }
}

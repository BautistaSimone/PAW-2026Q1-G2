package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class UserProfileForm {

    @NotBlank(message = "{NotBlank.userProfileForm.firstName}")
    @Size(max = 100, message = "{Size.userProfileForm.firstName}")
    private String firstName;

    @NotBlank(message = "{NotBlank.userProfileForm.lastName}")
    @Size(max = 100, message = "{Size.userProfileForm.lastName}")
    private String lastName;

    @Size(max = 255, message = "{Size.userProfileForm.streetName}")
    private String streetName;

    @Size(max = 20, message = "{Size.userProfileForm.streetNumber}")
    private String streetNumber;

    @Size(max = 100, message = "{Size.userProfileForm.neighborhood}")
    private String neighborhood;

    @Size(max = 100, message = "{Size.userProfileForm.province}")
    private String province;

    @Size(max = 500, message = "{Size.userProfileForm.extraAddressInfo}")
    private String extraAddressInfo;

    @Size(max = 22, message = "{Size.userProfileForm.cbuCvu}")
    @Pattern(regexp = "^$|^\\d{22}$", message = "{Pattern.userProfileForm.cbuCvu}")
    private String cbuCvu;

    public static UserProfileForm fromUser(final ar.edu.itba.paw.models.User u) {
        final UserProfileForm f = new UserProfileForm();
        f.setFirstName(u.getFirstName() != null ? u.getFirstName() : "");
        f.setLastName(u.getLastName() != null ? u.getLastName() : "");
        f.setStreetName(u.getStreetName() != null ? u.getStreetName() : "");
        f.setStreetNumber(u.getStreetNumber() != null ? u.getStreetNumber() : "");
        f.setNeighborhood(u.getNeighborhood() != null ? u.getNeighborhood() : "");
        f.setProvince(u.getProvince() != null ? u.getProvince() : "");
        f.setExtraAddressInfo(u.getExtraAddressInfo() != null ? u.getExtraAddressInfo() : "");
        f.setCbuCvu(u.getCbuCvu() != null ? u.getCbuCvu() : "");
        return f;
    }

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

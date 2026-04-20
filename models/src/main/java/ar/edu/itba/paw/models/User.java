package ar.edu.itba.paw.models;

public class User {

    private final Long id;
    private final String email;
    private final String password;
    private final String username;
    private final Boolean mod;
    private final String firstName;
    private final String lastName;
    private final String streetName;
    private final String streetNumber;
    private final String neighborhood;
    private final String province;
    private final String extraAddressInfo;
    private final String cbuCvu;

    public User(
            final Long id,
            final String email,
            final String password,
            final String username,
            final Boolean mod,
            final String firstName,
            final String lastName,
            final String streetName,
            final String streetNumber,
            final String neighborhood,
            final String province,
            final String extraAddressInfo,
            final String cbuCvu) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.username = username;
        this.mod = mod;
        this.firstName = firstName;
        this.lastName = lastName;
        this.streetName = streetName;
        this.streetNumber = streetNumber;
        this.neighborhood = neighborhood;
        this.province = province;
        this.extraAddressInfo = extraAddressInfo;
        this.cbuCvu = cbuCvu;
    }

    /** Backward-compatible constructor: profile fields are null. */
    public User(final Long id, final String email, final String password, final String username, final Boolean mod) {
        this(id, email, password, username, mod, null, null, null, null, null, null, null, null);
    }

    private static boolean nonBlank(final String s) {
        return s != null && !s.trim().isEmpty();
    }

    /** True if CBU/CVU is present (non-blank). */
    public boolean hasCbuCvu() {
        return nonBlank(cbuCvu);
    }

    /** True if street, number, barrio and provincia are all filled. */
    public boolean hasAddress() {
        return nonBlank(streetName)
                && nonBlank(streetNumber)
                && nonBlank(neighborhood)
                && nonBlank(province);
    }

    /** True if nombre, apellido and full shipping address are present (required to buy). */
    public boolean hasCompleteBuyerDataForPurchase() {
        return nonBlank(firstName) && nonBlank(lastName) && hasAddress();
    }

    public String getFullName() {
        final String fn = firstName != null ? firstName.trim() : "";
        final String ln = lastName != null ? lastName.trim() : "";
        if (fn.isEmpty() && ln.isEmpty()) {
            return username != null ? username : "";
        }
        return (fn + " " + ln).trim();
    }

    public String getFormattedShippingAddress() {
        if (!hasAddress()) {
            return "";
        }
        final StringBuilder sb = new StringBuilder();
        sb.append(streetName.trim()).append(' ').append(streetNumber.trim());
        sb.append(", ").append(neighborhood.trim()).append(", ").append(province.trim());
        if (nonBlank(extraAddressInfo)) {
            sb.append(". ").append(extraAddressInfo.trim());
        }
        return sb.toString();
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public Boolean getMod() {
        return mod;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getStreetName() {
        return streetName;
    }

    public String getStreetNumber() {
        return streetNumber;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public String getProvince() {
        return province;
    }

    public String getExtraAddressInfo() {
        return extraAddressInfo;
    }

    public String getCbuCvu() {
        return cbuCvu;
    }

    @Override
    public String toString() {
        return "User [id=" + id
                + ", email=" + email
                + ", password=" + password
                + ", username=" + username
                + ", mod=" + mod
                + ", firstName=" + firstName
                + ", lastName=" + lastName
                + "]";
    }
}

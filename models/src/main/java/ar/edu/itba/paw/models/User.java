package ar.edu.itba.paw.models;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Id;
import javax.persistence.Column;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "users_userid_seq")
    @SequenceGenerator(sequenceName = "users_userid_seq", name = "users_userid_seq", allocationSize = 1)
    @Column(name = "user_id")
    private Long id;

    @Column(length = 255, nullable = false, unique = true)
    private String email;

    @Column(length = 255, nullable = false)
    private String password;
    
    @Column(length = 255, nullable = false)
    private String username;
    private Boolean mod;
    private Boolean enabled;
    private Boolean banned;
    private String firstName;
    private String lastName;
    private String streetName;
    private String streetNumber;
    private String neighborhood;
    private String province;
    private String extraAddressInfo;
    private String cbuCvu;

    User() {
        // Just for Hibernate, we love you!
    }

    public User(
            String email,
            String password,
            String username,
            Boolean mod,
            Boolean enabled,
            Boolean banned,
            String firstName,
            String lastName,
            String streetName,
            String streetNumber,
            String neighborhood,
            String province,
            String extraAddressInfo,
            String cbuCvu) {
        this.email = email;
        this.password = password;
        this.username = username;
        this.mod = mod;
        this.enabled = enabled;
        this.banned = banned;
        this.firstName = firstName;
        this.lastName = lastName;
        this.streetName = streetName;
        this.streetNumber = streetNumber;
        this.neighborhood = neighborhood;
        this.province = province;
        this.extraAddressInfo = extraAddressInfo;
        this.cbuCvu = cbuCvu;
    }

    public User(
            Long id,
            String email,
            String password,
            String username,
            Boolean mod,
            Boolean enabled,
            Boolean banned,
            String firstName,
            String lastName,
            String streetName,
            String streetNumber,
            String neighborhood,
            String province,
            String extraAddressInfo,
            String cbuCvu) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.username = username;
        this.mod = mod;
        this.enabled = enabled;
        this.banned = banned;
        this.firstName = firstName;
        this.lastName = lastName;
        this.streetName = streetName;
        this.streetNumber = streetNumber;
        this.neighborhood = neighborhood;
        this.province = province;
        this.extraAddressInfo = extraAddressInfo;
        this.cbuCvu = cbuCvu;
    }

    private static boolean nonBlank(final String s) {
        return s != null && !s.trim().isEmpty();
    }

    /** True if CBU/CVU is present (non-blank). */
    public boolean hasCbuCvu() {
        return nonBlank(cbuCvu);
    }

    /** True if barrio and provincia are both filled (required to publish using profile location). */
    public boolean hasNeighborhoodAndProvince() {
        return nonBlank(neighborhood) && nonBlank(province);
    }

    /** True if street, number, barrio and provincia are all filled. */
    public boolean hasAddress() {
        return nonBlank(streetName)
                && nonBlank(streetNumber)
                && nonBlank(neighborhood)
                && nonBlank(province);
    }

    public String getLocation() {
        final String n = neighborhood != null ? neighborhood.trim() : "";
        final String p = province != null ? province.trim() : "";
        if (n.isEmpty() && p.isEmpty()) {
            return "";
        }
        if (n.isEmpty()) {
            return p;
        }
        if (p.isEmpty()) {
            return n;
        }
        return n + ", " + p;
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

    public Boolean getEnabled() {
        return enabled;
    }

    public Boolean getBanned() {
        return banned;
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
                + ", banned=" + banned
                + ", firstName=" + firstName
                + ", lastName=" + lastName
                + "]";
    }
}
package ar.edu.itba.paw.models;

import java.util.Set;
import java.util.HashSet;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import org.hibernate.annotations.BatchSize;

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

    @Column(name = "first_name", length = 100)
    private String firstName;

    @Column(name = "last_name", length = 100)
    private String lastName;

    @Column(name = "street_name")
    private String streetName;

    @Column(name = "street_number", length = 20)
    private String streetNumber;

    private String neighborhood;
    private String province;

    @Column(name = "extra_address_info", length = 500)
    private String extraAddressInfo;

    @Column(name = "cbu_cvu", length = 22)
    private String cbuCvu;

    @BatchSize(size = 20)
    @ManyToMany
    @JoinTable(
        name = "user_wishlist_products", 
        joinColumns = { @JoinColumn(name = "user_id") }, // Owner side of relationship
        inverseJoinColumns = { @JoinColumn(name = "product_id") }
    )
    private Set<Product> wishlistProducts = new HashSet<>();

    @BatchSize(size = 20)
    @ManyToMany
    @JoinTable(
        name = "user_favorite_categories",
        joinColumns = { @JoinColumn(name = "user_id") },
        inverseJoinColumns = { @JoinColumn(name = "category_id") }
    )
    private Set<Category> favoriteCategories = new HashSet<>();

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

    public Set<Product> getWishlistProducts() {
        return wishlistProducts;
    }

    public Set<Category> getFavoriteCategories() {
        return favoriteCategories;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setBanned(Boolean banned) {
        this.banned = banned;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public void setStreetNumber(String streetNumber) {
        this.streetNumber = streetNumber;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public void setExtraAddressInfo(String extraAddressInfo) {
        this.extraAddressInfo = extraAddressInfo;
    }

    public void setCbuCvu(String cbuCvu) {
        this.cbuCvu = cbuCvu;
    }

    public void setWishlistProducts(Set<Product> wishlistProducts) {
        this.wishlistProducts = wishlistProducts;
    }

    public void setFavoriteCategories(Set<Category> favoriteCategories) {
        this.favoriteCategories = favoriteCategories;
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
package ar.edu.itba.paw.models;

public class User {

    private final Long id;
    private final String email;
    private final String password;
    private final String username;
    private final Boolean mod;

    public User(Long id, String email, String password, String username, Boolean mod) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.username = username;
        this.mod = mod;
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

    @Override
    public String toString() {
        return 
            "User [id=" + id 
            + ", email=" + email 
            + ", password=" + password 
            + ", username=" + username 
            + ", mod=" + mod 
            + "]";
    }
}
package ar.edu.itba.paw.webapp.dto;

public class UserFollowDto {

    private long id;
    private String username;
    private String firstName;
    private String lastName;

    public UserFollowDto() {
    }

    public UserFollowDto(final long id, final String username, final String firstName, final String lastName) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
}

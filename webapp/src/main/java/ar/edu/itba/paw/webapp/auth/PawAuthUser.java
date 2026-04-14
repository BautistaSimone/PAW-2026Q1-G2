package ar.edu.itba.paw.webapp.auth;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class PawAuthUser extends User {

    private static final long serialVersionUID = 1L;

    private final ar.edu.itba.paw.models.User user;

    public PawAuthUser(
            final String email,
            final String password,
            final boolean enabled,
            final boolean accountNonExpired,
            final boolean credentialsNonExpired,
            final boolean accountNonLocked,
            final Collection<? extends GrantedAuthority> authorities,
            ar.edu.itba.paw.models.User user) {

        super(
                email,
                password,
                enabled,
                accountNonExpired,
                credentialsNonExpired,
                accountNonLocked,
                authorities
        );

        this.user = user;
    }

    public ar.edu.itba.paw.models.User getUser() {
        return user;
    }
}
package ar.edu.itba.paw.webapp.auth;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class PawAuthUser extends User {

    private static final long serialVersionUID = 1L;

    private final ar.edu.itba.paw.models.User user;

    public PawAuthUser(ar.edu.itba.paw.models.User user) {
        super(
            user.getEmail(),
            user.getPassword(),
            user.getEnabled(),                 // enabled
            true,                              // accountNonExpired
            true,                              // credentialsNonExpired
            !user.getBanned(),                 // accountNonLocked
            buildAuthorities(user)             // authorities
        );
        this.user = user;
    }

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

    private static Collection<? extends GrantedAuthority> buildAuthorities(ar.edu.itba.paw.models.User user) {
    List<GrantedAuthority> authorities = new ArrayList<>();

    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

    if (Boolean.TRUE.equals(user.getMod())) {
        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    return authorities;
}
}
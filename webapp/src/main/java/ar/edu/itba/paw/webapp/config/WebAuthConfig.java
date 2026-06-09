package ar.edu.itba.paw.webapp.config;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@ComponentScan("ar.edu.itba.paw.webapp.auth")
public class WebAuthConfig {

    @Value("${auth.rememberme}")
    private String authRemeberMe;

    @Autowired
    private UserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(final HttpSecurity http) throws Exception {

        http.sessionManagement()
                .and()
                .authorizeHttpRequests()
                // Anonymous-only (redirect to home if already logged in)
                .requestMatchers("/login", "/register").anonymous()
                // Authenticated-only endpoints for purchase flow
                .requestMatchers("/purchases", "/purchases/**").authenticated()
                // Authenticated-only endpoints for product management
                .requestMatchers("/products/new").authenticated()
                .requestMatchers("/products/*/edit", "/products/*/report", "/products/*/delete", "/products/*/restore")
                .authenticated()
                // Authenticated-only miscellaneous actions
                .requestMatchers(HttpMethod.GET, "/sendVerificationEmail").authenticated()
                .requestMatchers(HttpMethod.POST, "/sendVerificationEmail", "/toggle-wishlist-product",
                        "/profile/follow")
                .authenticated()
                // Authenticated-only verification/notification routes
                .requestMatchers("/for-you", "/notifications/**").authenticated()
                // Role based routes — more specific first
                .requestMatchers("/profile/admin/**").hasRole("ADMIN")
                .requestMatchers("/profile", "/profile/**").authenticated()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/test-mail", "/test-mail/**").denyAll()
                .requestMatchers(HttpMethod.POST, "/images", "/images/**").denyAll()
                // Public routes
                .requestMatchers("/", "/changePassword", "/resetPassword", "/verificationStatus", "/verifyEmail",
                        "/notVerified", "/search-users", "/banned")
                .permitAll()
                .requestMatchers("/css/**", "/js/**", "/img/**", "/assets/**", "/favicon.ico", "/403").permitAll()
                .and().formLogin()
                .loginPage("/login")
                .usernameParameter("email")
                .passwordParameter("password")
                .defaultSuccessUrl("/", false)
                .and().rememberMe()
                .rememberMeParameter("rememberMe")
                .userDetailsService(userDetailsService)
                .key(authRemeberMe)
                .tokenValiditySeconds((int) TimeUnit.DAYS.toSeconds(30))
                .and().logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .permitAll()
                .and().exceptionHandling()
                .accessDeniedPage("/403");

        return http.build();
    }
}

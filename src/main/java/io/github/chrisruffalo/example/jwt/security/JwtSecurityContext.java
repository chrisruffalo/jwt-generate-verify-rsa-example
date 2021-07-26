package io.github.chrisruffalo.example.jwt.security;

import com.auth0.jwt.interfaces.DecodedJWT;

import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A simple security context that is derived from a sent JWT. This is mostly
 * used to just attach any roles the server decides on and to give a principal
 * name.
 */
public class JwtSecurityContext implements SecurityContext {

    private final String subject;
    private final Set<String> roles = new HashSet<>();

    private boolean isSecure = false;
    private String scheme = "bearer";

    public JwtSecurityContext(DecodedJWT jwt, final String... roles) {
        this.subject = jwt.getSubject();
        this.roles.addAll(Arrays.stream(roles).map(String::toLowerCase).collect(Collectors.toSet()));
    }

    @Override
    public Principal getUserPrincipal() {
        return () -> subject;
    }

    @Override
    public boolean isUserInRole(String s) {
        return this.roles.contains(s) || s != null && this.roles.contains(s.toLowerCase());
    }

    @Override
    public boolean isSecure() {
        return this.isSecure;
    }

    @Override
    public String getAuthenticationScheme() {
        return this.scheme;
    }

    public void setSecure(boolean secure) {
        isSecure = secure;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }
}

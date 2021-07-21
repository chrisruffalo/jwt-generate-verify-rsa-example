package io.github.chrisruffalo.example.jwt.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.github.chrisruffalo.example.jwt.pki.InstancedKeyProvider;
import io.github.chrisruffalo.example.jwt.service.AccessPairService;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.PreMatching;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;

@Provider
@PreMatching
public class JwtSignedSecurityFilter implements ContainerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER = "Bearer ";
    private static final String SIGNED_JWT_ROLE = "Authorized";

    @Inject
    AccessPairService accessPairService;

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        String authorization = containerRequestContext.getHeaderString(AUTHORIZATION_HEADER);

        // early exit if no authorization header is returned
        if (authorization == null || authorization.isEmpty()) {
            return;
        }

        authorization = authorization.trim();
        String bearer = null;
        if (authorization.toLowerCase().startsWith(BEARER.toLowerCase())) {
            bearer = authorization.substring(BEARER.length());
        }

        // no bearer provided, early return
        if (bearer == null || bearer.isEmpty()) {
            return;
        }

        // establish if the bearer has a JWT that can be decoded
        String authorizationScheme = "bearer";
        DecodedJWT jwt = null;
        try {
            jwt = JWT.decode(bearer);
        } catch (Exception ex) {
            // could not decode, leave filter
            return;
        }

        // optional: implement a scheme here that looks in other locations for the
        //           jwt token like another header or a cookie, but bearer auth
        //           is pretty standard, i would actually implement these each as
        //           a strategy or similar and then loop through them. the
        //           strategy should set the authorization scheme.


        // if there is no jwt found after exhausting all methods
        if (jwt == null) {
            return;
        }

        final JwtSecurityContext securityContext = this.getContextForJwt(jwt);

        // do not proceed if no security context is returned
        if (securityContext == null) {
            return;
        }

        if(containerRequestContext.getUriInfo().getBaseUri().getScheme().equalsIgnoreCase("https")) {
            securityContext.setSecure(true);
        }
        securityContext.setScheme(authorizationScheme);

        // add the security context to the invocation
        containerRequestContext.setSecurityContext(securityContext);
    }

    JwtSecurityContext getContextForJwt(DecodedJWT jwt) {
        if (jwt == null) {
            return null;
        }

        // look for the subject
        String subject = jwt.getSubject();

        // if no subject is provided then exit
        if (subject == null || subject.isEmpty()) {
            return null;
        }

        // find the public key that matches the token
        final PublicKey publicKey = accessPairService.getPublicKeyForToken(subject);
        if (!(publicKey instanceof RSAPublicKey)) {
            // this shouldn't be possible but in the event it can't be cast we need to leave
            return null;
        }

        final RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
        final InstancedKeyProvider provider = new InstancedKeyProvider();
        provider.setPublicKey(rsaPublicKey);

        final Algorithm algorithm = Algorithm.RSA256(provider);
        final JWTVerifier verifier = JWT.require(algorithm).build();
        try {
            jwt = verifier.verify(jwt);
        } catch (Exception jwtVerificationException) {
            // any exception means that we are done
            return null;
        }

        // optional: everything after this point is happening to a valid/verified jwt
        //           that is signed with the key that was given. you should at this point
        //           be able to look for other claims so that we can ensure that this
        //           is not part of a replay attack. the auth0 library's verify function
        //           can handle some of this but not all of it. if you allow the same jwt
        //           to be used indefinitely (without checking for expiration) then
        //           a stolen JWT allows access to the API.

        // with a verified jwt we can create a new security context
        return new JwtSecurityContext(jwt, SIGNED_JWT_ROLE);
    }

}

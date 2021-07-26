package io.github.chrisruffalo.example.jwt.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import io.github.chrisruffalo.example.jwt.model.GenerationResponse;
import io.github.chrisruffalo.example.jwt.model.InstancedKeyProvider;
import io.github.chrisruffalo.example.jwt.service.AccessGenerationService;
import io.github.chrisruffalo.example.jwt.service.AccessVerificationDetailsStorageService;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import java.security.interfaces.RSAPrivateKey;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
class JwtSignedSecurityFilterTest {

    @Inject
    AccessVerificationDetailsStorageService accessVerificationDetailsStorageService;

    @Inject
    JwtSignedSecurityFilter filter;

    @Inject
    AccessGenerationService generator;

    @Test
    public void testValidUpload() {
        // generate access and persist key to db
        final GenerationResponse response = generator.generate(accessVerificationDetailsStorageService);
        Assertions.assertNotNull(response.getAccessToken());
        Assertions.assertNotNull(response.getPrivateKey());

        // get private key
        final RSAPrivateKey privateKey = (RSAPrivateKey) GenerationResponse.getPrivateKey(response.getPrivateKey());
        final InstancedKeyProvider provider = new InstancedKeyProvider();
        provider.setPrivateKey(privateKey);
        final Algorithm algorithm = Algorithm.RSA256(provider);

        // create signed cookie
        final String jwtCookie = JWT.create().withSubject(response.getAccessToken()).sign(algorithm);
        final DecodedJWT jwt = JWT.decode(jwtCookie);

        // get response (first parse as verified token which should work because we already have the key)
        final JwtSecurityContext context = filter.getContextForJwt(Mockito.mock(ContainerRequestContext.class), jwt);
        Assertions.assertNotNull(context);
    }


    @Test
    public void testInvalidUpload() {
        // generate access and persist key to db
        GenerationResponse response = generator.generate(accessVerificationDetailsStorageService);
        Assertions.assertNotNull(response.getAccessToken());
        final String firstAccessToken = response.getAccessToken();

        // get second response to cause key mismatch
        response = generator.generate(accessVerificationDetailsStorageService);
        Assertions.assertNotNull(response.getPrivateKey());

        // get private key
        final RSAPrivateKey privateKey = (RSAPrivateKey) GenerationResponse.getPrivateKey(response.getPrivateKey());
        final InstancedKeyProvider provider = new InstancedKeyProvider();
        provider.setPrivateKey(privateKey);
        final Algorithm algorithm = Algorithm.RSA256(provider);

        // create signed cookie with second key but first access token
        final String jwtCookie = JWT.create().withSubject(firstAccessToken).sign(algorithm);
        final DecodedJWT jwt = JWT.decode(jwtCookie);

        // get response which should be null because the jwt is signed with the private key from
        // the second generation but the subject does not have the matching key
        final JwtSecurityContext context = filter.getContextForJwt(Mockito.mock(ContainerRequestContext.class), jwt);
        Assertions.assertNull(context);
    }

}
package io.github.chrisruffalo.example.jwt.endpoint;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import io.github.chrisruffalo.example.jwt.model.GenerationResponse;
import io.github.chrisruffalo.example.jwt.pki.InstancedKeyProvider;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.ws.rs.core.MediaType;
import java.security.interfaces.RSAPrivateKey;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
public class EndpointTest {

    @Inject
    ApiEndpoint apiEndpoint;

    @Inject
    GenerationEndpoint generationEndpoint;

    @Test
    @TestSecurity(authorizationEnabled = false)
    public void testDirectServices() {
        javax.ws.rs.core.Response response = generationEndpoint.generateAccess();
        Assertions.assertEquals(200, response.getStatus());

        final Object generateEntity = response.getEntity();
        Assertions.assertTrue(generateEntity instanceof GenerationResponse);

        response = apiEndpoint.check();
        Assertions.assertEquals(200, response.getStatus());
    }

    @Test
    public void testRestServices() {
        Response response = RestAssured
            .given()
                .when().get("/generate")
            .thenReturn();

        // ensure that status code is 200
        response.then().statusCode(200);

        // get the output of the generation response
        final GenerationResponse generationResponse = response.getBody().as(GenerationResponse.class);

        // get private key
        final RSAPrivateKey privateKey = (RSAPrivateKey) GenerationResponse.getPrivateKey(generationResponse.getPrivateKey());
        final InstancedKeyProvider provider = new InstancedKeyProvider();
        provider.setPrivateKey(privateKey);
        final Algorithm algorithm = Algorithm.RSA256(provider);

        // create signed cookie with second key but first access token
        final String jwtCookie = JWT.create().withSubject(generationResponse.getAccessToken()).sign(algorithm);

        // call check endpoint
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        response = RestAssured
            .given()
                .when()
                .header("Authorization", "Bearer " + jwtCookie)
                .get("/api/check");

        response.then().statusCode(200);
    }

}

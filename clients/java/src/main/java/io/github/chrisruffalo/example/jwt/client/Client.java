package io.github.chrisruffalo.example.jwt.client;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.RSAKeyProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

public class Client {

    public static void main(String[] args) {
        final ObjectMapper mapper = new ObjectMapper();

        try (final CloseableHttpClient client = HttpClients.createDefault()) {

            String accessTokenString = null;
            String privateKeyString = null;

            // initial call to generate endpoint
            final HttpGet generateGet = new HttpGet("http://localhost:8080/generate");
            try (final CloseableHttpResponse generateResponse = client.execute(generateGet)) {
                final HttpEntity entity = generateResponse.getEntity();
                final JsonNode root = mapper.readTree(entity.getContent());
                accessTokenString = root.at("/accessToken").textValue();
                privateKeyString = root.at("/privateKey").textValue(); // the private key is base64 encoded pkcs8
            }

            // check the api access
            final HttpGet checkGet = new HttpGet("http://localhost:8080/api/check");
            try (final CloseableHttpResponse apiResponse = client.execute(checkGet)) {
                System.out.println(apiResponse.getStatusLine()); // 403 Forbidden
            }

            // get private key instance from pkcs8 encoded string
            final RSAPrivateKey privateKey;
            try {
                privateKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyString)));
            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            // this is how auth0 jwt provides a signing algorithm. the key provider provides the necessary
            // keys for the signature/verification
            final Algorithm algorithm = Algorithm.RSA256(new RSAKeyProvider() {
                @Override
                public RSAPublicKey getPublicKeyById(String keyId) {
                    return null;
                }

                @Override
                public RSAPrivateKey getPrivateKey() {
                    return privateKey;
                }

                @Override
                public String getPrivateKeyId() {
                    return null;
                }
            });

            // create signed cookie with the subject ("sub") of the access token and signed with the given algorithm
            final String jwtCookie = JWT.create().withSubject(accessTokenString).sign(algorithm);

            // add the authorization bearer token header to the same get request
            checkGet.addHeader("Authorization", "Bearer " + jwtCookie);
            try (final CloseableHttpResponse apiResponse = client.execute(checkGet)) {
                System.out.println(apiResponse.getStatusLine()); // 200 OK
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

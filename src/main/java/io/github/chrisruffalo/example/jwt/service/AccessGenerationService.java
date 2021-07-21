package io.github.chrisruffalo.example.jwt.service;

import io.github.chrisruffalo.example.jwt.model.GenerationResponse;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@ApplicationScoped
public class AccessGenerationService {

    /**
     * JWTs use RSA keys for the RS signature algorithm.
     */
    public static final String ALGO = "RSA";

    /**
     * The size of the token that will be generated. Most UUIDs
     * are ~160bits or 20 bytes. The 32 bytes used here is probably
     * overkill.
     */
    private static final int TOKEN_SIZE = 32;

    /**
     * A shared secure random generation instance is fine. Not sure
     * that secure random is really necessary here because guessing
     * the tokens doesn't really get an attacker anything without
     * the keypair which is (should be) securely generated.
     */
    private final SecureRandom random = new SecureRandom();

    @PostConstruct
    public void init() {
        // it isn't clear if this is needed so some feedback might
        // be required on this segment.
        random.setSeed(System.currentTimeMillis());
        random.setSeed(System.nanoTime());
    }

    public GenerationResponse generate(final GenerationHandler handler) {
        final GenerationResponse response = new GenerationResponse();

        // build a random token
        final byte[] tokenBytes = new byte[TOKEN_SIZE];
        synchronized (random) {
            random.nextBytes(tokenBytes);
        }
        final String tokenString = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);

        // optional: check the generation service to see if the keypair exists before proceeding. this
        //           might not be worth the database cost of doing the lookup and here is left as an
        //           exercise for the implementor.
        response.setAccessToken(tokenString);

        // generate keypair
        try {
            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGO);
            final KeyPair pair = keyGen.generateKeyPair();

            // add the encoded keypair to the response object
            final String privateKey = Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded());
            response.setPrivateKey(privateKey);
            final String publicKey = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
            response.setPublicKey(publicKey);

            // allow the handler to be null
            if (handler != null) {
                handler.handleResponse(response);
            }
        } catch (NoSuchAlgorithmException e) {
            // if we can't get the right providers/algs we can't do anything so bail
            throw new RuntimeException(e);
        }

        return response;
    }

}

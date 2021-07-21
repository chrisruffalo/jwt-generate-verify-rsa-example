package io.github.chrisruffalo.example.jwt.pki;

import io.github.chrisruffalo.example.jwt.model.GenerationResponse;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

@ApplicationScoped
public class PkiGenerator {

    public static final String ALGO = "RSA";
    private static final int TOKEN_SIZE = 64;

    private final SecureRandom random = new SecureRandom();

    @PostConstruct
    public void init() {
        random.setSeed(System.currentTimeMillis());
        random.setSeed(System.nanoTime());
    }

    public GenerationResponse generate(final GenerationHandler handler) {
        final GenerationResponse response = new GenerationResponse();

        final byte[] tokenBytes = new byte[TOKEN_SIZE];
        synchronized (random) {
            random.nextBytes(tokenBytes);
        }
        final String tokenString = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        response.setAccessToken(tokenString);

        // generate keypair
        try {
            final KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGO);
            final KeyPair pair = keyGen.generateKeyPair();

            final String privateKey = Base64.getEncoder().encodeToString(pair.getPrivate().getEncoded());
            response.setPrivateKey(privateKey);
            final String publicKey = Base64.getEncoder().encodeToString(pair.getPublic().getEncoded());
            response.setPublicKey(publicKey);

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

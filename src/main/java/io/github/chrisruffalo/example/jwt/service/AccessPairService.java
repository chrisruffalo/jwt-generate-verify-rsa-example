package io.github.chrisruffalo.example.jwt.service;

import io.github.chrisruffalo.example.jwt.model.AccessPair;
import io.github.chrisruffalo.example.jwt.model.GenerationResponse;
import io.github.chrisruffalo.example.jwt.pki.GenerationHandler;
import io.github.chrisruffalo.example.jwt.pki.PkiGenerator;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@ApplicationScoped
public class AccessPairService implements GenerationHandler {

    @Inject
    EntityManager manager;

    @Override
    @Transactional
    public void handleResponse(final GenerationResponse response) {
        // create pair
        final AccessPair pair = new AccessPair();
        pair.setToken(response.getAccessToken());
        pair.setPublicKey(response.getPublicKey());

        // persist
        manager.persist(pair);
    }

    @Transactional
    public PublicKey getPublicKeyForToken(final String token) {
        if (token == null) {
            return null;
        }

        final AccessPair pair = manager.find(AccessPair.class, token);
        if (pair == null) {
            return null;
        }

        if (pair.getPublicKey() != null && !pair.getPublicKey().isEmpty()) {
            try {
                return KeyFactory.getInstance(PkiGenerator.ALGO).generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(pair.getPublicKey())));
            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }
}

package io.github.chrisruffalo.example.jwt.service;

import io.github.chrisruffalo.example.jwt.model.AccessVerificationDetails;
import io.github.chrisruffalo.example.jwt.model.GenerationResponse;

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

/**
 * The AccessVerificationDetailsStorageService is the examples implementation of what
 * to do with generated access verification artifacts. In reality this could be a
 * different service altogether or implemented in any way. It might be a good idea
 * to have the GenerationHandler implementation entirely separate because that
 * could be a write-only account and the public key retrieval is read-only.
 */
@ApplicationScoped
public class AccessVerificationDetailsStorageService implements GenerationHandler {

    @Inject
    EntityManager manager;

    @Override
    @Transactional
    public void handleResponse(final GenerationResponse response) {
        // create pair
        final AccessVerificationDetails pair = new AccessVerificationDetails();
        pair.setSubject(response.getAccessToken());
        pair.setPublicKey(response.getPublicKey());

        // persist
        manager.persist(pair);
    }

    /**
     * Get the public key for a given subject.
     *
     * @param subject the subject (id) that needs public key verification
     * @return the public key if it exists, null otherwise
     */
    @Transactional
    public PublicKey getPublicKeyForSubject(final String subject) {
        if (subject == null || subject.isEmpty()) {
            return null;
        }

        final AccessVerificationDetails pair = manager.find(AccessVerificationDetails.class, subject);
        if (pair == null) {
            return null;
        }

        if (pair.getPublicKey() != null && !pair.getPublicKey().isEmpty()) {
            try {
                return KeyFactory.getInstance(AccessGenerationService.ALGO).generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(pair.getPublicKey())));
            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }
        }

        return null;
    }
}

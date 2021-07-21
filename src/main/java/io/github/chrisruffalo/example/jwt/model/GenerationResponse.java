package io.github.chrisruffalo.example.jwt.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.github.chrisruffalo.example.jwt.pki.PkiGenerator;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class GenerationResponse {

    private String accessToken;

    private String privateKey;

    @JsonIgnore
    private String publicKey;

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public static PublicKey getPublicCert(final String publicCertBase64String) {
        try {
            return KeyFactory.getInstance(PkiGenerator.ALGO).generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(publicCertBase64String)));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static PrivateKey getPrivateKey(final String privateKeyBase64String) {
        try {
            return KeyFactory.getInstance(PkiGenerator.ALGO).generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyBase64String)));
        } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}

package io.github.chrisruffalo.example.jwt.model;

import com.auth0.jwt.interfaces.RSAKeyProvider;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * The JWT auth0 library requires a key provider be used with an
 * algorithm for signature/verification of a JWT token. This class
 * allows that adaptation.
 */
public class InstancedKeyProvider implements RSAKeyProvider {

    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;

    public void setPublicKey(RSAPublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public void setPrivateKey(RSAPrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    @Override
    public RSAPublicKey getPublicKeyById(String keyId) {
        return publicKey;
    }

    @Override
    public RSAPrivateKey getPrivateKey() {
        return privateKey;
    }

    @Override
    public String getPrivateKeyId() {
        return "";
    }
}

package io.github.chrisruffalo.example.jwt.pki;

import com.auth0.jwt.interfaces.RSAKeyProvider;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

public class InstancedKeyProvider  implements RSAKeyProvider {

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

package io.github.chrisruffalo.example.jwt.model;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

/**
 * This is the entity class that stores the pair of the
 * public key and access token in the persistence layer.
 */
@Entity
public class AccessVerificationDetails {

    /**
     * The access token is the string used to identify the
     * access attempt.
     */
    @Id
    private String subject;

    /**
     * The public key is used to verify the signature.
     */
    @Lob
    private String publicKey;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String token) {
        this.subject = token;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}

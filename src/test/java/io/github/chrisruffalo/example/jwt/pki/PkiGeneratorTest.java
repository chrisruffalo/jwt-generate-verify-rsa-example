package io.github.chrisruffalo.example.jwt.pki;

import io.github.chrisruffalo.example.jwt.model.GenerationResponse;
import io.github.chrisruffalo.example.jwt.service.AccessPairService;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.h2.H2DatabaseTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.inject.Inject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

@QuarkusTest
@QuarkusTestResource(H2DatabaseTestResource.class)
class PkiGeneratorTest {

    @Inject
    AccessPairService service;

    @Test
    public void testRoundTrip() throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        final String clear = "clear text needs to survive round trip";

        final PkiGenerator generator = new PkiGenerator();
        final GenerationResponse generationResponse = generator.generate(service);
        final PublicKey publicKey = service.getPublicKeyForToken(generationResponse.getAccessToken());
        Assertions.assertNotNull(publicKey);

        // round trip crypto
        final Cipher cipher = Cipher.getInstance(PkiGenerator.ALGO);
        final PrivateKey privateKey = GenerationResponse.getPrivateKey(generationResponse.getPrivateKey());
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        byte[] message = cipher.doFinal(clear.getBytes());
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        byte[] clearBytes = cipher.doFinal(message);

        Assertions.assertEquals(clear, new String(clearBytes), "Clear text matches round-trip encrypted/decrypted text");
    }


}
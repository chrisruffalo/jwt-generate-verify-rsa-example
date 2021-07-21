package io.github.chrisruffalo.example.jwt.pki;

import io.github.chrisruffalo.example.jwt.model.GenerationResponse;

import java.security.PublicKey;

public interface GenerationHandler {

    void handleResponse(final GenerationResponse response);

}

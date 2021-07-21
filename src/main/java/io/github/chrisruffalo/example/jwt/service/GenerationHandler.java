package io.github.chrisruffalo.example.jwt.service;

import io.github.chrisruffalo.example.jwt.model.GenerationResponse;

/**
 * Handles what to do with a generated access response. This allows
 * externalization of the generated response storage.
 *
 */
public interface GenerationHandler {

    /**
     * Does <strong>something</strong> with the generated access details. Usually
     * would store in a database but could put them on disk or really anywhere as
     * long as they can be retrieved later.
     *
     * @param response the generated access request response
     */
    void handleResponse(final GenerationResponse response);

}

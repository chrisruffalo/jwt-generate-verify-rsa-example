package io.github.chrisruffalo.example.jwt.endpoint;

import io.github.chrisruffalo.example.jwt.model.GenerationResponse;
import io.github.chrisruffalo.example.jwt.service.AccessGenerationService;
import io.github.chrisruffalo.example.jwt.service.AccessVerificationDetailsStorageService;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Simple entrypoint to generate an access token and private key. This ideally would
 * be hosted on an internal service accessed only by (or on behalf of) authorized users.
 */
@ApplicationScoped
@Path("/generate")
public class GenerationEndpoint {

    @Inject
    AccessGenerationService service;

    @Inject
    AccessVerificationDetailsStorageService pairService;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response generateAccess() {
        final GenerationResponse response = service.generate(pairService);
        return Response.ok(response).build();
    }
}

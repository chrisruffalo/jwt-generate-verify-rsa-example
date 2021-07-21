package io.github.chrisruffalo.example.jwt.endpoint;

import io.github.chrisruffalo.example.jwt.model.GenerationResponse;
import io.github.chrisruffalo.example.jwt.pki.PkiGenerator;
import io.github.chrisruffalo.example.jwt.service.AccessPairService;
import io.smallrye.mutiny.Uni;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@ApplicationScoped
@Path("/generate")
public class GenerationEndpoint {

    @Inject
    PkiGenerator service;

    @Inject
    AccessPairService pairService;

    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response generateAccess() {
        final GenerationResponse response = service.generate(pairService);
        return Response.ok(response).build();
    }
}

package io.github.chrisruffalo.example.jwt.endpoint;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * This is a sample entrypoint in to an application. It requires the "Authorized" role which is only
 * granted by {@link io.github.chrisruffalo.example.jwt.security.JwtSignedSecurityFilter} when a correctly
 * matching and signed JWT is presented.
 *
 * @see io.github.chrisruffalo.example.jwt.security.JwtSignedSecurityFilter
 * @see io.github.chrisruffalo.example.jwt.security.JwtSecurityContext
 */
@RolesAllowed("Authorized")
@ApplicationScoped
@Path("/api")
public class ApiEndpoint {

    @GET
    @Path("/check")
    @Produces(MediaType.TEXT_PLAIN)
    public Response check() {
        return Response.ok().build();
    }
}

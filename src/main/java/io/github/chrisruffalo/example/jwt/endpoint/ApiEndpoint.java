package io.github.chrisruffalo.example.jwt.endpoint;

import org.jboss.logging.Logger;

import javax.annotation.security.RolesAllowed;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@RolesAllowed("Authorized")
@ApplicationScoped
@Path("/api")
public class ApiEndpoint {

    private static final Logger logger = Logger.getLogger(ApiEndpoint.class);

    @GET
    @Path("/check")
    @Produces(MediaType.TEXT_PLAIN)
    public Response check() {
        return Response.ok().build();
    }
}

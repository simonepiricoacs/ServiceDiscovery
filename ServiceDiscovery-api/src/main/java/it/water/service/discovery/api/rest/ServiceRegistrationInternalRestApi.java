package it.water.service.discovery.api.rest;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import it.water.core.api.service.rest.FrameworkRestApi;
import it.water.core.api.service.rest.RestApi;
import it.water.core.api.service.rest.WaterJsonView;
import it.water.service.discovery.model.ServiceRegistration;
import java.util.List;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Internal REST API for framework-driven service self-registration.
 * Exposes only the minimal operations needed by a service runtime.
 */
@Path("/internal/serviceregistration")
@Api(produces = MediaType.APPLICATION_JSON, tags = "ServiceRegistration Internal API")
@FrameworkRestApi
public interface ServiceRegistrationInternalRestApi extends RestApi {

    @POST
    @Path("/register")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @JsonView(WaterJsonView.Public.class)
    @ApiOperation(value = "/register", notes = "Internal service registration endpoint", httpMethod = "POST",
            produces = MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    ServiceRegistration register(ServiceRegistration serviceRegistration);

    @GET
    @Path("/available")
    @Produces(MediaType.APPLICATION_JSON)
    @JsonView(WaterJsonView.Public.class)
    @ApiOperation(value = "/available", notes = "Anonymous list of UP service instances for internal infrastructure consumers",
            httpMethod = "GET", produces = MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful operation"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    List<ServiceRegistration> listAvailable();

    @DELETE
    @Path("/{serviceName}/{instanceId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "/{serviceName}/{instanceId}", notes = "Internal service deregistration endpoint", httpMethod = "DELETE",
            produces = MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful operation"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    void deregister(@PathParam("serviceName") String serviceName, @PathParam("instanceId") String instanceId);

    @PUT
    @Path("/heartbeat/{serviceName}/{instanceId}")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "/heartbeat/{serviceName}/{instanceId}", notes = "Internal service heartbeat endpoint", httpMethod = "PUT",
            produces = MediaType.APPLICATION_JSON)
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Successful operation"),
            @ApiResponse(code = 404, message = "Service registration not found"),
            @ApiResponse(code = 500, message = "Internal server error")
    })
    void heartbeat(@PathParam("serviceName") String serviceName, @PathParam("instanceId") String instanceId);
}

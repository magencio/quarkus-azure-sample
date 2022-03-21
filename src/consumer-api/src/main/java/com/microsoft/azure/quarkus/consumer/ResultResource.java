package com.microsoft.azure.quarkus.consumer;

import com.microsoft.azure.quarkus.consumer.model.ResultEntity;
import com.microsoft.azure.quarkus.consumer.model.ResultResponse;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/result")
public class ResultResource {

  @Inject
  protected JsonWebToken jwt;

  /**
   * Get the result of the operation.
   * Consumers can get the result of any operation.
   * Producers can only get the result of their own operations.
   * 
   * @param operationId the operation id
   * @return the result
   */
  @RolesAllowed({ "Consumer", "Producer" })
  @GET
  @Path("/{operationId}")
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> get(@Context SecurityContext ctx, @PathParam("operationId") String operationId) {
    Optional<String> oid = jwt.claim("oid");
    if (oid.isEmpty()) {
      return CompletableFuture.completedFuture(Response.status(Status.FORBIDDEN).build());
    }

    CompletionStage<ResultEntity> stage;
    if (ctx.isUserInRole("Consumer")) {
      stage = ResultEntity.findByOperationId(operationId);
    } else {
      stage = ResultEntity.findByOperationIdAndUserId(operationId, oid.get());
    }
    return stage.thenApply(entity -> {
      if (entity == null) {
        return Response.status(Status.NOT_FOUND).build();
      } else {
        ResultResponse response = new ResultResponse(entity.getOperationId(), entity.getResult());
        return Response.ok(response).build();
      }
    });
  }
}
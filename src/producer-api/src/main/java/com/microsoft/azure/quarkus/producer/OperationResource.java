package com.microsoft.azure.quarkus.producer;

import com.microsoft.azure.quarkus.producer.model.NewOperationMessage;
import com.microsoft.azure.quarkus.producer.model.NewOperationRequest;
import com.microsoft.azure.quarkus.producer.model.NewOperationResponse;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.BiConsumer;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.jboss.logging.Logger;

@Path("/operation")
public class OperationResource {

  private static final Logger LOG = Logger.getLogger(OperationResource.class);

  @Inject
  protected JsonWebToken jwt;

  @Channel("new-operation")
  protected Emitter<NewOperationMessage> emitter;

  /**
   * Create a new operation.
   * 
   * @param request the request with the new operation
   * @return the response with the operation id
   */
  @RolesAllowed("Producer")
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public CompletionStage<Response> post(@NotNull @Valid NewOperationRequest request) {
    Optional<String> oid = jwt.claim("oid");
    if (oid.isEmpty()) {
      return CompletableFuture.completedFuture(Response.status(Status.FORBIDDEN).build());
    }

    String operationId = UUID.randomUUID().toString();
    NewOperationMessage message = new NewOperationMessage(operationId, oid.get(), request);
    NewOperationResponse response = new NewOperationResponse(operationId);

    return emitter
        .send(message)
        .whenComplete(handleEmitterResponse(operationId))
        .thenApply(ignore -> Response.ok(response).status(Status.ACCEPTED).build());
  }

  private static BiConsumer<Void, Throwable> handleEmitterResponse(final String operationId) {
    return (Void success, Throwable failure) -> {
      if (failure != null) {
        LOG.error("Error processing message '" + operationId + "': " + failure.getMessage());
      } else {
        LOG.debug("Message '" + operationId + "' processed sucessfully");
      }
    };
  }
}

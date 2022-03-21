package com.microsoft.azure.quarkus.consumer.model;

import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntityBase;
import io.smallrye.mutiny.Uni;
import java.util.concurrent.CompletionStage;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.bson.codecs.pojo.annotations.BsonId;

/**
 * Entity to store results of operations.
 */
@Getter
@Setter
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PUBLIC)
@AllArgsConstructor
@NoArgsConstructor
@MongoEntity(collection = "results")
public class ResultEntity extends ReactivePanacheMongoEntityBase {
  @BsonId
  @NonNull
  private String operationId;
  private String userId;
  private Integer leftOperand;
  private String operation;
  private Integer rightOperand;
  private Double result;

  /**
   * Find the result of an operation.
   * 
   * @param operationId the operation id of the entity
   * @return the entity
   */
  public static CompletionStage<ResultEntity> findByOperationId(String operationId) {
    Uni<ResultEntity> entityUni = find("_id = ?1", operationId).firstResult();
    return entityUni.subscribe().asCompletionStage();
  }

  /**
   * Find the result of an operation.
   * 
   * @param operationId the operation id of the entity
   * @param userId      the userId of the entity
   * @return the entity
   */
  public static CompletionStage<ResultEntity> findByOperationIdAndUserId(String operationId, String userId) {
    Uni<ResultEntity> entityUni = find("_id = ?1 and userId = ?2", operationId, userId).firstResult();
    return entityUni.subscribe().asCompletionStage();
  }

  /**
   * Upsert the result of an operation.
   * 
   * @param message the operation message
   * @param result  the result of the operation
   */
  public static CompletionStage<Void> upsertResult(NewOperationMessage message, Double result) {
    Uni<ResultEntity> entityUni = findById(message.getOperationId());
    return entityUni
        .replaceIfNullWith(newEntity(message))
        .call(entity -> updateEntity(entity, message, result))
        .chain(entity -> persistOrUpdate(entity))
        .subscribe()
        .asCompletionStage();
  }

  private static ResultEntity newEntity(NewOperationMessage message) {
    return new ResultEntity()
        .setOperationId(message.getOperationId())
        .setUserId(message.getUserId());
  }

  private static Uni<ResultEntity> updateEntity(ResultEntity entity, NewOperationMessage message, Double result) {
    if (!entity.getUserId().equals(message.getUserId())) {
      throw new IllegalArgumentException("User id does not match the one in the database");
    }
    entity
        .setLeftOperand(message.getLeftOperand())
        .setOperation(message.getOperation())
        .setRightOperand(message.getRightOperand())
        .setResult(result);
    return Uni.createFrom().item(entity);
  }
}

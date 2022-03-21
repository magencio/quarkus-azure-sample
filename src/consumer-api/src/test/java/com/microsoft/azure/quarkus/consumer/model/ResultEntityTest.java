package com.microsoft.azure.quarkus.consumer.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.quarkus.mongodb.panache.reactive.ReactivePanacheMongoEntityBase;
import io.quarkus.mongodb.panache.reactive.ReactivePanacheQuery;
import io.quarkus.panache.mock.PanacheMock;
import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.mutiny.Uni;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

@QuarkusTest
public class ResultEntityTest {

  ArgumentCaptor<ResultEntity> entityCaptor;
  ArgumentCaptor<Object> ignoreCaptor;

  @BeforeEach
  void setup() {
    // Mock entity (except the methods we implemented)
    PanacheMock.mock(ResultEntity.class);
    when(ResultEntity.findByOperationId(any())).thenCallRealMethod();
    when(ResultEntity.findByOperationIdAndUserId(any(), any())).thenCallRealMethod();
    when(ResultEntity.upsertResult(any(), any())).thenCallRealMethod();

    // Argument captors
    entityCaptor = ArgumentCaptor.forClass(ResultEntity.class);
    ignoreCaptor = ArgumentCaptor.forClass(Object.class);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testFindByOperationIdWhenFound() throws Exception {
    // Arrange
    final String operationId = UUID.randomUUID().toString();
    final String userId = UUID.randomUUID().toString();

    ResultEntity expectedEntity = new ResultEntity(operationId, userId, 1, "+", 2, 3.0);
    ReactivePanacheQuery<ReactivePanacheMongoEntityBase> queryMock = mock(ReactivePanacheQuery.class);
    when(queryMock.firstResult()).thenReturn(Uni.createFrom().item(expectedEntity));
    when(ResultEntity.find("_id = ?1", operationId)).thenReturn(queryMock);

    // Act
    ResultEntity entity = ResultEntity.findByOperationId(operationId).toCompletableFuture().get();

    // Assert
    assertThat(entity).usingRecursiveComparison().isEqualTo(expectedEntity);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testFindByOperationIdWhenNotFound() throws Exception {
    // Arrange
    final String operationId = UUID.randomUUID().toString();

    ReactivePanacheQuery<ReactivePanacheMongoEntityBase> queryMock = mock(ReactivePanacheQuery.class);
    when(queryMock.firstResult()).thenReturn(Uni.createFrom().nullItem());
    when(ResultEntity.find("_id = ?1", operationId)).thenReturn(queryMock);

    // Act
    ResultEntity entity = ResultEntity.findByOperationId(operationId).toCompletableFuture().get();

    // Assert
    assertThat(entity).isNull();
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testFindByOperationIdAndUserIdWhenFound() throws Exception {
    // Arrange
    final String operationId = UUID.randomUUID().toString();
    final String userId = UUID.randomUUID().toString();

    ResultEntity expectedEntity = new ResultEntity(operationId, userId, 1, "+", 2, 3.0);
    ReactivePanacheQuery<ReactivePanacheMongoEntityBase> queryMock = mock(ReactivePanacheQuery.class);
    when(queryMock.firstResult()).thenReturn(Uni.createFrom().item(expectedEntity));
    when(ResultEntity.find("_id = ?1 and userId = ?2", operationId, userId)).thenReturn(queryMock);

    // Act
    ResultEntity entity = ResultEntity.findByOperationIdAndUserId(operationId, userId).toCompletableFuture().get();

    // Assert
    assertThat(entity).usingRecursiveComparison().isEqualTo(expectedEntity);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testFindByOperationIdAndUserIdWhenNotFound() throws Exception {
    // Arrange
    final String operationId = UUID.randomUUID().toString();
    final String userId = UUID.randomUUID().toString();

    ReactivePanacheQuery<ReactivePanacheMongoEntityBase> queryMock = mock(ReactivePanacheQuery.class);
    when(queryMock.firstResult()).thenReturn(Uni.createFrom().nullItem());
    when(ResultEntity.find("_id = ?1 and userId = ?2", operationId, userId)).thenReturn(queryMock);

    // Act
    ResultEntity entity = ResultEntity.findByOperationIdAndUserId(operationId, userId).toCompletableFuture().get();

    // Assert
    assertThat(entity).isNull();
  }

  @Test
  @SuppressWarnings("static-access")
  public void testUpsertResultWhenCreates() throws Exception {
    // Arrange
    final String operationId = UUID.randomUUID().toString();
    final String userId = UUID.randomUUID().toString();

    when(ResultEntity.findById(operationId)).thenReturn(Uni.createFrom().nullItem());

    NewOperationMessage message = new NewOperationMessage(operationId, userId, 2, "-", 1);

    // Act
    ResultEntity.upsertResult(message, 1.0).toCompletableFuture().join();

    // Assert
    PanacheMock.verify(ResultEntity.class).persistOrUpdate(entityCaptor.capture(), ignoreCaptor.capture());
    ResultEntity entity = entityCaptor.getValue();
    ResultEntity expectedEntity = new ResultEntity(operationId, userId, 2, "-", 1, 1.0);
    assertThat(entity).usingRecursiveComparison().isEqualTo(expectedEntity);
  }

  @Test
  @SuppressWarnings("static-access")
  public void testUpsertResultWhenUpdates() throws Exception {
    // Arrange
    final String operationId = UUID.randomUUID().toString();
    final String userId = UUID.randomUUID().toString();

    ResultEntity originalEntity = new ResultEntity(operationId, userId, 1, "+", 2, 3.0);
    when(ResultEntity.findById(operationId)).thenReturn(Uni.createFrom().item(originalEntity));

    NewOperationMessage message = new NewOperationMessage(operationId, userId, 2, "-", 1);

    // Act
    ResultEntity.upsertResult(message, 1.0).toCompletableFuture().join();

    // Assert
    PanacheMock.verify(ResultEntity.class).persistOrUpdate(entityCaptor.capture(), ignoreCaptor.capture());
    ResultEntity entity = entityCaptor.getValue();
    ResultEntity expectedEntity = new ResultEntity(operationId, userId, 2, "-", 1, 1.0);
    assertThat(entity).usingRecursiveComparison().isEqualTo(expectedEntity);
  }

  @Test
  public void testUpsertResultWhenUpdatesWithAnotherUserId() throws Exception {
    // Arrange
    final String operationId = UUID.randomUUID().toString();
    final String userId = UUID.randomUUID().toString();
    final String anotherUserId = UUID.randomUUID().toString();

    ResultEntity originalEntity = new ResultEntity(operationId, userId, 1, "+", 2, 3.0);
    when(ResultEntity.findById(operationId)).thenReturn(Uni.createFrom().item(originalEntity));

    NewOperationMessage message = new NewOperationMessage(operationId, anotherUserId, 2, "-", 1);

    // Act
    CompletableFuture<Void> future = ResultEntity.upsertResult(message, 1.0).toCompletableFuture();

    // Assert
    assertThatThrownBy(() -> future.join())
        .isInstanceOf(CompletionException.class)
        .hasCauseInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("User id does not match the one in the database");
  }
}

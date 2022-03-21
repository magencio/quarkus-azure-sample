package com.microsoft.azure.quarkus.producer.testutils;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * Constraint violation error response.
 */
@Getter
@Setter
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PUBLIC)
@AllArgsConstructor
@NoArgsConstructor
public class ConstraintViolationErrorResponse {
  private String title;
  private Integer status;
  private List<ConstraintViolationError> violations;
}

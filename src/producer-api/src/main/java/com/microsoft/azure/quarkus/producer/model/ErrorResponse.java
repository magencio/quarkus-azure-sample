package com.microsoft.azure.quarkus.producer.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * Error response.
 */
@Getter
@Setter
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PUBLIC)
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {
  private String title;
  private Integer status;
  private String error;
}

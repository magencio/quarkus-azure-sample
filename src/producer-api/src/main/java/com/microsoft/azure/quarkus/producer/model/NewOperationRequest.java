package com.microsoft.azure.quarkus.producer.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

/**
 * New operation request.
 */
@Getter
@Setter
@Accessors(chain = true)
@FieldDefaults(level = AccessLevel.PUBLIC)
@AllArgsConstructor
@NoArgsConstructor
public class NewOperationRequest {
  @NotNull
  private Integer leftOperand;
  @NotNull
  @Pattern(regexp = "[\\+\\-*\\/]", message = "must be one of +, -, *, /")
  private String operation;
  @NotNull
  private Integer rightOperand;
}

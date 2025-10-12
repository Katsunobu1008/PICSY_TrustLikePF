// backend-java/src/main/java/com/picsy/trustlikepf/api/dto/EvalMatrixResponse.java
package com.picsy.trustlikepf.api.dto;

import java.util.List;
import java.util.UUID;

public record EvalMatrixResponse(
        boolean ok,
        List<Row> rows
) {
    public static record Row(UUID evaluatorId, UUID evaluateeId, double value) {}
}

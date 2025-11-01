// backend-java/src/main/java/com/picsy/trustlikepf/api/dto/DashboardEvaluationResponse.java
package com.picsy.trustlikepf.api.dto;

import java.util.List;
import java.util.UUID;

public record DashboardEvaluationResponse(
        boolean ok,
        List<UUID> order,
        List<UserSummary> users,
        List<MatrixEntry> matrix
) {
    public static record UserSummary(
            UUID userId,
            String handle,
            double contribution,
            double budget,
            double purchasingPower,
            long posts,
            double deltaReceivedPosts,
            double deltaReceivedDirect,
            double deltaSent
    ) {}

    public static record MatrixEntry(UUID evaluatorId, UUID evaluateeId, double value) {}
}

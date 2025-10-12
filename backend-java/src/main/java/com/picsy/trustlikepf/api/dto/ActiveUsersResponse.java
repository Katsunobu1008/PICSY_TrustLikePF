// backend-java/src/main/java/com/picsy/trustlikepf/api/dto/ActiveUsersResponse.java
package com.picsy.trustlikepf.api.dto;

import java.util.List;
import java.util.UUID;

public record ActiveUsersResponse(
        boolean ok,
        List<UserItem> users
) {
    public static record UserItem(UUID userId, String name) {}
}

// LikeRequest.java
package com.picsy.trustlikepf.api.dto;

import java.util.UUID;

public record LikeRequest(UUID actorId, UUID postId, UUID requestId) {}

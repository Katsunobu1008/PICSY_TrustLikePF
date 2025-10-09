package com.picsy.trustlikepf.api.dto;

import java.util.UUID;

public record QuoteRequest(UUID actorId, UUID postId, UUID requestId, Double betaOverride) {}

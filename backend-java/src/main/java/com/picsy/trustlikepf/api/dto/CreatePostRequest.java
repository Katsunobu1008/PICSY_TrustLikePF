package com.picsy.trustlikepf.api.dto;

import java.util.List;
import java.util.UUID;

public record CreatePostRequest(
        UUID creatorId,
        String contentText,
        Double royaltyRate,
        UUID parentPostId,
        List<String> mediaKeys
) {}

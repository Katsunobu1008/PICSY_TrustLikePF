// AffordanceResponse.java
package com.picsy.trustlikepf.api.dto;

public record AffordanceResponse(
        boolean canLike,
        double availableLikePower,
        double likeCost,
        String  likeReason,

        boolean canQuote,
        double availableQuotePower,
        double quoteCost,
        String  quoteReason
) {}

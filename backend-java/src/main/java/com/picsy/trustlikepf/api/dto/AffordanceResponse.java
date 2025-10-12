// backend-java/src/main/java/com/picsy/trustlikepf/api/dto/AffordanceResponse.java
package com.picsy.trustlikepf.api.dto;

public record AffordanceResponse(
        boolean canLike,  double powerLike,  double neededLike,  String likeReason,
        boolean canQuote, double powerQuote, double neededQuote, String quoteReason
) {}

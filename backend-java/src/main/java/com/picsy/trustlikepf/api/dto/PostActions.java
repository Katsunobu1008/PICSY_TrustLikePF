// backend-java/src/main/java/com/picsy/trustlikepf/api/dto/PostActions.java
package com.picsy.trustlikepf.api.dto;

public record PostActions(
        // like
        boolean likeEnabled,
        String  likeReason,
        // quote
        boolean quoteEnabled,
        String  quoteReason,
        // parameters & powers
        double  alpha,
        double  beta,
        double  powerLike,
        double  powerQuote
) {
    public static PostActions disabledAll(double alpha, double beta){
        return new PostActions(false, "DISABLED", false, "DISABLED", alpha, beta, 0.0, 0.0);
    }
}

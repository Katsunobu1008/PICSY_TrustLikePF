// backend-java/src/main/java/com/picsy/trustlikepf/api/dto/PostActionsResponse.java
package com.picsy.trustlikepf.api.dto;

/**
 * @deprecated Use AffordanceResponse instead.
 */
@Deprecated(since = "0.0.1", forRemoval = false)
public record PostActionsResponse(
        boolean canLike, String likeReason,
        boolean canQuote, String quoteReason,
        double budgetE,        // E_pp
        double budgetC,        // c_p
        double budgetProduct,  // E_pp * c_p
        double costLike,       // α
        double costQuoteDefault // β(デフォルト)
) {}

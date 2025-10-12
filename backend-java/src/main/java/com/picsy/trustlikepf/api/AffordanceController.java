// AffordanceController.java
package com.picsy.trustlikepf.api;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.picsy.trustlikepf.api.dto.AffordanceResponse;
import com.picsy.trustlikepf.domain.service.AffordanceService;

/**
 * @deprecated Use GET /api/posts/{postId}/actions?actor=... instead.
 */
@Deprecated(since = "0.0.1", forRemoval = false)
@RestController
@RequestMapping("/api/affordance")
public class AffordanceController {

    private final AffordanceService afford;

    public AffordanceController(AffordanceService afford) { this.afford = afford; }

    @GetMapping("/actors/{actorId}/posts/{postId}")
    public ResponseEntity<AffordanceResponse> get(@PathVariable UUID actorId,
                                                  @PathVariable UUID postId){
        return ResponseEntity.ok(afford.forActorAndPost(actorId, postId));
    }
}

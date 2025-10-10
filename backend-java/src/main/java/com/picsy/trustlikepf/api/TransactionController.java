// backend-java/src/main/java/com/picsy/trustlikepf/api/TransactionController.java
package com.picsy.trustlikepf.api;

import java.util.Map;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.picsy.trustlikepf.api.dto.LikeRequest;
import com.picsy.trustlikepf.api.dto.QuoteRequest;
import com.picsy.trustlikepf.domain.service.TransactionService;

@RestController
@RequestMapping("/api/v1/tx")
public class TransactionController {
    private final TransactionService tx;

    public TransactionController(TransactionService tx) { this.tx = tx; }

    @PostMapping("/like")
    public Map<String,Object> like(@RequestBody LikeRequest req){
        tx.like(req);
        return Map.of("ok", true);
    }

    @PostMapping("/quote")
    public Map<String,Object> quote(@RequestBody QuoteRequest req){
        tx.quote(req);
        return Map.of("ok", true);
    }
}

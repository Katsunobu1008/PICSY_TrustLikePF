// AffordanceService.java
package com.picsy.trustlikepf.domain.service;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.picsy.trustlikepf.api.dto.AffordanceResponse;
import com.picsy.trustlikepf.domain.repository.ContributionVectorRepository;
import com.picsy.trustlikepf.domain.repository.PostRepository;

@Service
public class AffordanceService {

    private final EvaluationRowService eService;
    private final ContributionVectorRepository cRepo;
    private final PostRepository postRepo;

    @Value("${picsy.alpha:0.05}")
    private double alpha;

    @Value("${picsy.beta.default:0.12}")
    private double defaultBeta;

    public AffordanceService(EvaluationRowService eService,
                             ContributionVectorRepository cRepo,
                             PostRepository postRepo) {
        this.eService = eService;
        this.cRepo = cRepo;
        this.postRepo = postRepo;
    }

    @Transactional(readOnly = true)
    public AffordanceResponse forActorAndPost(UUID actorId, UUID postId){
        var post = postRepo.findById(postId).orElseThrow();
        var row = eService.lockAndLoad(actorId, Set.of(actorId)).cols;
        double epp = row.get(actorId).getValue();
        double cp  = cRepo.findById(actorId).orElseThrow().getValue();
        double power = epp * cp;

        // like
        boolean self = post.getCreatorId().equals(actorId);
        boolean canLike = !self && power >= alpha;
        String likeReason = canLike ? "OK" :
                (self ? "SELF_LIKE_NOT_ALLOWED" : "INSUFFICIENT_POWER");

        // quote
        double beta = defaultBeta;
        boolean canQuote = power >= beta;
        String quoteReason = canQuote ? "OK" : "INSUFFICIENT_POWER";

        return new AffordanceResponse(
                canLike,  round6(power), round6(alpha), likeReason,
                canQuote, round6(power), round6(beta),  quoteReason
        );
    }

    static double round6(double x){ return Math.round(x * 1_000_000d)/1_000_000d; }
}

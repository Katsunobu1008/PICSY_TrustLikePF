package com.picsy.trustlikepf.domain.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.picsy.trustlikepf.api.dto.QuoteRequest;
import com.picsy.trustlikepf.domain.entity.ContributionVector;
import com.picsy.trustlikepf.domain.entity.EvaluationMatrix;
import com.picsy.trustlikepf.domain.entity.Post;
import com.picsy.trustlikepf.domain.entity.TransactionLog;
import com.picsy.trustlikepf.domain.entity.User;
import com.picsy.trustlikepf.domain.repository.ContributionVectorRepository;
import com.picsy.trustlikepf.domain.repository.PostRepository;
import com.picsy.trustlikepf.domain.repository.TransactionLogRepository;
import com.picsy.trustlikepf.domain.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private EvaluationRowService evaluationRowService;
    @Mock
    private ContributionVectorRepository contributionVectorRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private TransactionLogRepository transactionLogRepository;
    @Mock
    private UserRepository userRepository;

    private TransactionService service;

    private UUID actorId;
    private UUID quotedId;
    private UUID originalId;
    private UUID requestId;

    private void init() {
        service = new TransactionService(
                evaluationRowService,
                contributionVectorRepository,
                postRepository,
                transactionLogRepository,
                userRepository);
        ReflectionTestUtils.setField(service, "defaultBeta", 0.12d);

        actorId = UUID.randomUUID();
        quotedId = UUID.randomUUID();
        originalId = UUID.randomUUID();
        requestId = UUID.randomUUID();
    }

    @Test
    @DisplayName("quote fails when actor account is frozen")
    void quoteFailsWhenActorFrozen() {
        init();
        var request = new QuoteRequest(actorId, quotedId, requestId, null);

        when(transactionLogRepository.findByRequestId(requestId)).thenReturn(Optional.empty());
        when(postRepository.findById(quotedId)).thenReturn(Optional.of(quotedPost()));
        when(userRepository.findById(actorId)).thenReturn(Optional.of(user(actorId, 0.15, false)));

        assertThatThrownBy(() -> service.quote(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("ACCOUNT_FROZEN");

        verify(transactionLogRepository, never()).save(any());
        verifyNoInteractions(contributionVectorRepository, evaluationRowService);
    }

    @Test
    @DisplayName("quote fails when original reference is missing")
    void quoteFailsWhenOriginalMissing() {
        init();
        var request = new QuoteRequest(actorId, quotedId, requestId, null);

        when(transactionLogRepository.findByRequestId(requestId)).thenReturn(Optional.empty());
        var quoted = quotedPost();
        quoted.setOriginalPostId(null);
        when(postRepository.findById(quotedId)).thenReturn(Optional.of(quoted));
        when(userRepository.findById(actorId)).thenReturn(Optional.of(user(actorId, 0.12, true)));
        when(userRepository.findById(quoted.getCreatorId()))
                .thenReturn(Optional.of(user(quoted.getCreatorId(), 0.10, true)));

        assertThatThrownBy(() -> service.quote(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("ORIGINAL_POST_ID_MISSING");

        verify(transactionLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("quote fails when original royalty is absent")
    void quoteFailsWhenOriginalRoyaltyMissing() {
        init();
        var request = new QuoteRequest(actorId, quotedId, requestId, null);

    when(transactionLogRepository.findByRequestId(requestId)).thenReturn(Optional.empty());
    var quoted = quotedPost();
    when(postRepository.findById(quotedId)).thenReturn(Optional.of(quoted));
    when(userRepository.findById(actorId)).thenReturn(Optional.of(user(actorId, 0.12, true)));
    when(userRepository.findById(quoted.getCreatorId()))
        .thenReturn(Optional.of(user(quoted.getCreatorId(), 0.10, true)));

        var original = originalPost();
        original.setRoyaltyRate(null);
        when(postRepository.findById(originalId)).thenReturn(Optional.of(original));

        assertThatThrownBy(() -> service.quote(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("ORIGINAL_ROYALTY_NOT_SET");

        verify(transactionLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("quote fails when purchasing power is insufficient")
    void quoteFailsWhenPurchasingPowerInsufficient() {
        init();
        var request = new QuoteRequest(actorId, quotedId, requestId, null);

    when(transactionLogRepository.findByRequestId(requestId)).thenReturn(Optional.empty());
    var quoted = quotedPost();
    when(postRepository.findById(quotedId)).thenReturn(Optional.of(quoted));
    when(postRepository.findById(originalId)).thenReturn(Optional.of(originalPost()));
    when(userRepository.findById(actorId)).thenReturn(Optional.of(user(actorId, 0.12, true)));
    when(userRepository.findById(quoted.getCreatorId()))
        .thenReturn(Optional.of(user(quoted.getCreatorId(), 0.10, true)));

        var row = new EvaluationRowService.Row(actorId);
        row.cols().put(actorId, new EvaluationMatrix(actorId, actorId, 1.0));
        when(evaluationRowService.lockAndLoad(eq(actorId), any())).thenReturn(row);

        when(contributionVectorRepository.findById(actorId))
                .thenReturn(Optional.of(new ContributionVector(actorId, 0.1)));

        assertThatThrownBy(() -> service.quote(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("INSUFFICIENT_PURCHASING_POWER");

        verify(transactionLogRepository, never()).save(any());
    }

    @Test
    @DisplayName("quote returns early for duplicate request id")
    void quoteReturnsEarlyForDuplicateRequest() {
        init();
        var request = new QuoteRequest(actorId, quotedId, requestId, null);

        var existing = new TransactionLog("QUOTE", actorId, quotedId, BigDecimal.valueOf(0.12), requestId, "{}");
        when(transactionLogRepository.findByRequestId(requestId)).thenReturn(Optional.of(existing));

        service.quote(request);

        verify(transactionLogRepository).findByRequestId(requestId);
        verifyNoInteractions(postRepository, userRepository, contributionVectorRepository, evaluationRowService);
    }

    private Post quotedPost() {
        var p = new Post();
        p.setPostId(quotedId);
        p.setCreatorId(UUID.randomUUID());
        p.setOriginalPostId(originalId);
        p.setParentPostId(null);
        p.setRoyaltyRate(BigDecimal.valueOf(0.2));
        p.setContentText("quoted");
        return p;
    }

    private Post originalPost() {
        var p = new Post();
        p.setPostId(originalId);
        p.setCreatorId(UUID.randomUUID());
        p.setOriginalPostId(originalId);
        p.setRoyaltyRate(BigDecimal.valueOf(0.2));
        p.setContentText("original");
        return p;
    }

    private User user(UUID id, double commission, boolean active) {
        try {
            var ctor = User.class.getDeclaredConstructor();
            ctor.setAccessible(true);
            User u = ctor.newInstance();
            ReflectionTestUtils.setField(u, "userId", id);
            ReflectionTestUtils.setField(u, "name", "user-" + id.toString().substring(0, 8));
            ReflectionTestUtils.setField(u, "commissionRate", BigDecimal.valueOf(commission));
            u.setActive(active);
            return u;
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException("Failed to prepare User", ex);
        }
    }
}

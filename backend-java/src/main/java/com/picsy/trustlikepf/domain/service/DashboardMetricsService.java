// backend-java/src/main/java/com/picsy/trustlikepf/domain/service/DashboardMetricsService.java
package com.picsy.trustlikepf.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.picsy.trustlikepf.api.dto.DashboardEvaluationResponse;
import com.picsy.trustlikepf.api.dto.DashboardEvaluationResponse.MatrixEntry;
import com.picsy.trustlikepf.api.dto.DashboardEvaluationResponse.UserSummary;
import com.picsy.trustlikepf.domain.entity.EvaluationMatrix;
import com.picsy.trustlikepf.domain.entity.Post;
import com.picsy.trustlikepf.domain.entity.TransactionLog;
import com.picsy.trustlikepf.domain.entity.User;
import com.picsy.trustlikepf.domain.repository.ContributionVectorRepository;
import com.picsy.trustlikepf.domain.repository.EvaluationMatrixRepository;
import com.picsy.trustlikepf.domain.repository.PostRepository;
import com.picsy.trustlikepf.domain.repository.TransactionLogRepository;
import com.picsy.trustlikepf.domain.repository.UserRepository;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 集計ロジック: アクティブユーザーに紐づく評価行列/貢献度/トランザクションを集め、ダッシュボード用に要約する。
 */
@Service
public class DashboardMetricsService {

    private final UserRepository userRepository;
    private final ContributionVectorRepository contributionRepository;
    private final EvaluationMatrixRepository evaluationRepository;
    private final PostRepository postRepository;
    private final TransactionLogRepository transactionLogRepository;
    private final ObjectMapper objectMapper;

    private static final String LIKE = "LIKE";
    private static final String QUOTE = "QUOTE";

    public DashboardMetricsService(UserRepository userRepository,
                                   ContributionVectorRepository contributionRepository,
                                   EvaluationMatrixRepository evaluationRepository,
                                   PostRepository postRepository,
                                   TransactionLogRepository transactionLogRepository,
                                   ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.contributionRepository = contributionRepository;
        this.evaluationRepository = evaluationRepository;
        this.postRepository = postRepository;
        this.transactionLogRepository = transactionLogRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public DashboardEvaluationResponse summarize() {
        List<User> activeUsers = userRepository.findByIsActiveTrue();
        activeUsers.sort(Comparator.comparing(User::getName, Comparator.nullsLast(String::compareToIgnoreCase))
                .thenComparing(User::getUserId));
        List<UUID> order = activeUsers.stream().map(User::getUserId).toList();

    Map<UUID, Double> contributions = loadContributionVector(order);
    Map<UUID, Double> budgets = loadSelfBudgets(order);

    List<Post> allPosts = postRepository.findAll();
    Map<UUID, Post> postsById = allPosts.stream()
        .collect(Collectors.toMap(Post::getPostId, p -> p, (a, b) -> a));
    Map<UUID, Long> postCounts = loadPostCounts(order, allPosts);

    TransactionDelta delta = accumulateTransactionDelta(order, postsById);

        List<UserSummary> users = activeUsers.stream()
                .map(user -> buildSummary(user,
                        contributions.getOrDefault(user.getUserId(), 1.0),
                        budgets.getOrDefault(user.getUserId(), 0.0),
                        postCounts.getOrDefault(user.getUserId(), 0L),
                        delta))
                .toList();

        List<MatrixEntry> matrixEntries = loadMatrixEntries(order);

        return new DashboardEvaluationResponse(true, order, users, matrixEntries);
    }

    private Map<UUID, Double> loadContributionVector(List<UUID> order) {
        Map<UUID, Double> map = new HashMap<>();
        contributionRepository.findAllById(order).forEach(cv -> map.put(cv.getUserId(), round6(cv.getValue())));
        order.forEach(id -> map.putIfAbsent(id, 1.0));
        return map;
    }

    private Map<UUID, Double> loadSelfBudgets(List<UUID> order) {
        Map<UUID, Double> budgets = new HashMap<>();
        for (UUID evaluator : order) {
            Optional<EvaluationMatrix> diag = evaluationRepository.findById(new com.picsy.trustlikepf.domain.entity.EvaluationMatrixId(evaluator, evaluator));
            budgets.put(evaluator, diag.map(EvaluationMatrix::getValue).map(DashboardMetricsService::round6).orElse(0.0));
        }
        return budgets;
    }

    private List<MatrixEntry> loadMatrixEntries(List<UUID> order) {
        List<MatrixEntry> cells = new ArrayList<>();
        var active = new java.util.HashSet<>(order);
        for (UUID evaluator : order) {
            List<EvaluationMatrix> row = evaluationRepository.findByEvaluator(evaluator);
            for (EvaluationMatrix em : row) {
                UUID evaluatee = em.getId().getEvaluateeId();
                if (!active.contains(evaluatee)) continue;
                cells.add(new MatrixEntry(evaluator, evaluatee, round6(em.getValue())));
            }
        }
        return cells;
    }

    private Map<UUID, Long> loadPostCounts(List<UUID> order, List<Post> posts) {
        Map<UUID, Long> counts = new HashMap<>();
        posts.stream()
                .collect(Collectors.groupingBy(Post::getCreatorId, Collectors.counting()))
                .forEach(counts::put);
        order.forEach(id -> counts.putIfAbsent(id, 0L));
        return counts;
    }

    private TransactionDelta accumulateTransactionDelta(List<UUID> order, Map<UUID, Post> posts) {
        Map<UUID, Double> receivedPosts = initializeDoubleMap(order);
        Map<UUID, Double> receivedDirect = initializeDoubleMap(order);
        Map<UUID, Double> sent = initializeDoubleMap(order);

        List<TransactionLog> logs = transactionLogRepository.findAll();
        for (TransactionLog log : logs) {
            UUID actor = log.getActorId();
            double amount = toDouble(log.getAmount());
            sent.computeIfPresent(actor, (k, v) -> round6(v + amount));

            if (LIKE.equalsIgnoreCase(log.getTransactionType())) {
                handleLike(log, amount, posts, receivedPosts, receivedDirect);
            } else if (QUOTE.equalsIgnoreCase(log.getTransactionType())) {
                handleQuote(log, amount, posts, receivedPosts, receivedDirect);
            }
        }
        return new TransactionDelta(receivedPosts, receivedDirect, sent);
    }

    private void handleLike(TransactionLog log,
                             double amount,
                             Map<UUID, Post> posts,
                             Map<UUID, Double> receivedPosts,
                             Map<UUID, Double> receivedDirect) {
        Post post = posts.get(log.getTargetPostId());
        if (post == null) return;

        JsonNode details = parseDetails(log.getDetails());
        if (details == null) return;

        UUID creator = post.getCreatorId();
        UUID original = resolveCreator(posts.get(post.getOriginalPostId()));
        UUID parentCreator = resolveCreator(posts.get(post.getParentPostId()));

        double r_c = details.path("r_c").asDouble(0);
        double rho = details.path("rho_s").asDouble(0);
        double rcShare = round6(r_c * amount);
        double residual = round6((1.0 - r_c) * amount);

        // 投稿者への報酬
        addToMap(receivedPosts, creator, rcShare);

        // 原作者へのロイヤリティ
        if (original != null) {
            addToMap(receivedDirect, original, round6(rho * residual));
        }

        // 親シェアへの配分
        if (parentCreator != null) {
            addToMap(receivedDirect, parentCreator, round6((1.0 - rho) * residual));
        }
    }

    private void handleQuote(TransactionLog log,
                              double amount,
                              Map<UUID, Post> posts,
                              Map<UUID, Double> receivedPosts,
                              Map<UUID, Double> receivedDirect) {
        Post quoted = posts.get(log.getTargetPostId());
        if (quoted == null) return;

        JsonNode details = parseDetails(log.getDetails());
        if (details == null) return;

        UUID quotedCreator = quoted.getCreatorId();
        UUID originalCreator = resolveCreator(posts.get(quoted.getOriginalPostId()));
        double rho = details.path("rho_s").asDouble(0);

        if (quoted.getParentPostId() == null) {
            // オリジナル投稿が引用されたケース
            addToMap(receivedPosts, quotedCreator, round6(amount));
        } else {
            // シェアが引用されたケース
            addToMap(receivedPosts, quotedCreator, round6((1.0 - rho) * amount));
            if (originalCreator != null) {
                addToMap(receivedDirect, originalCreator, round6(rho * amount));
            }
        }
    }

    private UUID resolveCreator(Post post) {
        return post == null ? null : post.getCreatorId();
    }

    private JsonNode parseDetails(String details) {
        if (details == null || details.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(details);
        } catch (IOException ex) {
            return null;
        }
    }

    private UserSummary buildSummary(User user,
                                     double contribution,
                                     double budget,
                                     long posts,
                                     TransactionDelta delta) {
        double purchasing = round6(contribution * budget);
        return new UserSummary(
                user.getUserId(),
                user.getName(),
                round6(contribution),
                round6(budget),
                purchasing,
                posts,
                round3(delta.receivedPosts().getOrDefault(user.getUserId(), 0.0)),
                round3(delta.receivedDirect().getOrDefault(user.getUserId(), 0.0)),
                round3(delta.sent().getOrDefault(user.getUserId(), 0.0))
        );
    }

    private static Map<UUID, Double> initializeDoubleMap(List<UUID> order) {
        Map<UUID, Double> map = new HashMap<>();
        order.forEach(id -> map.put(id, 0.0));
        return map;
    }

    private static void addToMap(Map<UUID, Double> map, UUID key, double delta) {
        if (key == null || !map.containsKey(key)) return;
        map.compute(key, (k, v) -> round6(v + delta));
    }

    private static double toDouble(BigDecimal value) {
        return value == null ? 0.0 : value.doubleValue();
    }

    private record TransactionDelta(Map<UUID, Double> receivedPosts,
                                     Map<UUID, Double> receivedDirect,
                                     Map<UUID, Double> sent) {}

    private static double round6(double value) {
        return BigDecimal.valueOf(value).setScale(6, RoundingMode.HALF_UP).doubleValue();
    }

    private static double round3(double value) {
        return BigDecimal.valueOf(value).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }
}

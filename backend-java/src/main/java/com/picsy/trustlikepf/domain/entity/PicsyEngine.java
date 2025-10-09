@Service
@RequiredArgsConstructor
public class PicsyEngine {
    private final EvaluationMatrixRepository eRepo;
    private final ContributionVectorRepository cRepo;
    private final UserRepository userRepo;

    // スケジューリング：MVPは固定 Delay、管理UIからの手動実行も後で追加
    @Scheduled(fixedDelayString = "${picsy.engine.interval.ms:300000}") // 5min
    @Transactional
    public void recalcC(){
        List<UUID> users = userRepo.findAll().stream().map(User::getUserId).toList();
        int N = users.size();
        if (N == 0) return;

        // c初期値（現状のc、なければ1.0）
        Map<UUID, Double> c = new HashMap<>();
        cRepo.findAll().forEach(cv -> c.put(cv.getUserId(), cv.getValue()));
        users.forEach(u -> c.putIfAbsent(u, 1.0));

        // E を行ごとにメモリへ
        Map<UUID, List<EvaluationMatrix>> rows = new HashMap<>();
        for (UUID u : users) {
            rows.put(u, eRepo.findByEvaluator(u));
        }

        // べき乗法
        for (int iter=0; iter<100; iter++){
            Map<UUID, Double> next = new HashMap<>();
            for (UUID j : users) next.put(j, 0.0);

            for (UUID i : users) {
                // 行 i
                var row = rows.get(i);
                double c_i = c.get(i);
                double Eii = 0.0;
                for (var em : row) {
                    UUID j = em.getId().getEvaluateeId();
                    double v = em.getValue();
                    if (i.equals(j)) { Eii = v; continue; }
                    // 通常分
                    next.put(j, next.get(j) + c_i * v);
                }
                // 仮想中央配分：Eii/(N-1) を全 j≠i へ
                double add = (N > 1) ? Eii / (N - 1) : 0.0;
                if (add != 0.0){
                    for (UUID j : users){
                        if (j.equals(i)) continue;
                        next.put(j, next.get(j) + c_i * add);
                    }
                }
            }

            // 正規化（∑c = N）
            double sum = next.values().stream().mapToDouble(Double::doubleValue).sum();
            if (sum == 0) break;
            double scale = N / sum;
            double diff = 0.0;
            for (UUID u : users){
                double v = next.get(u) * scale;
                diff += Math.pow(v - c.get(u), 2);
                c.put(u, v);
            }
            if (Math.sqrt(diff) < 1e-9) break;
        }

        // 保存
        for (UUID u : users){
            double v = Math.round(c.get(u) * 1_000_000d)/1_000_000d;
            cRepo.save(new ContributionVector(u, v));
        }
    }
}

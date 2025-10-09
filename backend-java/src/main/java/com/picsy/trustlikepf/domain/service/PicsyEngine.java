package com.picsy.trustlikepf.domain.service;

import com.picsy.trustlikepf.domain.entity.ContributionVector;
import com.picsy.trustlikepf.domain.entity.EvaluationMatrix;
import com.picsy.trustlikepf.domain.entity.User;
import com.picsy.trustlikepf.domain.repository.ContributionVectorRepository;
import com.picsy.trustlikepf.domain.repository.EvaluationMatrixRepository;
import com.picsy.trustlikepf.domain.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class PicsyEngine {

    private final EvaluationMatrixRepository eRepo;
    private final ContributionVectorRepository cRepo;
    private final UserRepository userRepo;

    public PicsyEngine(EvaluationMatrixRepository eRepo,
                       ContributionVectorRepository cRepo,
                       UserRepository userRepo) {
        this.eRepo = eRepo;
        this.cRepo = cRepo;
        this.userRepo = userRepo;
    }

    @Scheduled(fixedDelayString = "${picsy.engine.interval.ms:300000}") // 5min
    @Transactional
    public void recalcC(){
        List<UUID> users = userRepo.findAll().stream().map(User::getUserId).toList();
        int N = users.size();
        if (N == 0) return;

        Map<UUID, Double> c = new HashMap<>();
        cRepo.findAll().forEach(cv -> c.put(cv.getUserId(), cv.getValue()));
        users.forEach(u -> c.putIfAbsent(u, 1.0));

        Map<UUID, List<EvaluationMatrix>> rows = new HashMap<>();
        for (UUID u : users) {
            rows.put(u, eRepo.findByEvaluator(u));
        }

        for (int iter=0; iter<100; iter++){
            Map<UUID, Double> next = new HashMap<>();
            for (UUID j : users) next.put(j, 0.0);

            for (UUID i : users) {
                var row = rows.get(i);
                double c_i = c.get(i);
                double Eii = 0.0;
                for (var em : row) {
                    UUID j = em.getId().getEvaluateeId();
                    double v = em.getValue();
                    if (i.equals(j)) { Eii = v; continue; }
                    next.put(j, next.get(j) + c_i * v);
                }
                double add = (N > 1) ? Eii / (N - 1) : 0.0;
                if (add != 0.0){
                    for (UUID j : users){
                        if (j.equals(i)) continue;
                        next.put(j, next.get(j) + c_i * add);
                    }
                }
            }

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

        for (UUID u : users){
            double v = Math.round(c.get(u) * 1_000_000d)/1_000_000d;
            cRepo.save(new ContributionVector(u, v));
        }
    }
}

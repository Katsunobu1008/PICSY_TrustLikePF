-- backend-java/src/main/resources/db/migration/V2__Seed_Initial_Data.sql
-- Users: Alice, Bob, Carol, David
INSERT INTO users(user_id, name, commission_rate) VALUES
  ('11111111-1111-1111-1111-111111111111', 'Alice', 0.20),
  ('22222222-2222-2222-2222-222222222222', 'Bob',   0.20),
  ('33333333-3333-3333-3333-333333333333', 'Carol', 0.30),
  ('44444444-4444-4444-4444-444444444444', 'David', 0.25)
ON CONFLICT DO NOTHING;

-- 原作: Aliceの投稿（ρ_s=0.70）
INSERT INTO posts(post_id, creator_id, content_text, parent_post_id, original_post_id, royalty_rate)
VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
        '11111111-1111-1111-1111-111111111111',
        'Alice original post', NULL, 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 0.70)
ON CONFLICT DO NOTHING;

-- c: 全員1.0（合計N=4）から開始
INSERT INTO contribution_vector(user_id, value) VALUES
  ('11111111-1111-1111-1111-111111111111', 1.0),
  ('22222222-2222-2222-2222-222222222222', 1.0),
  ('33333333-3333-3333-3333-333333333333', 1.0),
  ('44444444-4444-4444-4444-444444444444', 1.0)
ON CONFLICT(user_id) DO UPDATE SET value = EXCLUDED.value;

-- E: 各行はE_ii=1.0, 他0.0（行和=1）
DO $$
DECLARE
  u UUID;
BEGIN
  FOR u IN SELECT user_id FROM users LOOP
    INSERT INTO evaluation_matrix(evaluator_id, evaluatee_id, value) VALUES
      (u, u, 1.0)
    ON CONFLICT (evaluator_id, evaluatee_id) DO UPDATE SET value = EXCLUDED.value;
  END LOOP;
END$$;

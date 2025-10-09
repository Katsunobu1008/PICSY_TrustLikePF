-- ========== 基本 ==========
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ユーザー（事前定義：認証なしMVP）
CREATE TABLE IF NOT EXISTS users (
  user_id         UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  name            VARCHAR(50) NOT NULL,
  profile_text    TEXT,
  icon_image_url  VARCHAR(255),
  commission_rate NUMERIC(5,4) NOT NULL DEFAULT 0.20 CHECK (commission_rate >= 0 AND commission_rate <= 0.50),
  is_active       BOOLEAN NOT NULL DEFAULT TRUE,
  created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_users_active ON users(is_active);

-- 投稿（原作/引用）
CREATE TABLE IF NOT EXISTS posts (
  post_id           UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  creator_id        UUID NOT NULL REFERENCES users(user_id),
  content_text      TEXT NOT NULL,
  parent_post_id    UUID REFERENCES posts(post_id),
  original_post_id  UUID NOT NULL, -- サーバが親から継承して設定
  royalty_rate      NUMERIC(5,4) CHECK (royalty_rate IS NULL OR (royalty_rate >= 0 AND royalty_rate <= 1.0)),
  created_at        TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT chk_original_royalty
    CHECK ((parent_post_id IS NULL AND royalty_rate IS NOT NULL) OR (parent_post_id IS NOT NULL AND royalty_rate IS NULL))
);
CREATE INDEX IF NOT EXISTS idx_posts_creator_created ON posts(creator_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_posts_original ON posts(original_post_id);

-- メディア（画像添付：MVPはローカル保存 or S3キー）
CREATE TABLE IF NOT EXISTS media_attachments (
  media_id    UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  post_id     UUID NOT NULL REFERENCES posts(post_id) ON DELETE CASCADE,
  storage_key VARCHAR(255) NOT NULL,
  mime_type   VARCHAR(50)  NOT NULL,
  metadata    JSONB,
  created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_media_post ON media_attachments(post_id);

-- コメント（1階層スレッド）
CREATE TABLE IF NOT EXISTS comments (
  comment_id  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
  post_id     UUID NOT NULL REFERENCES posts(post_id) ON DELETE CASCADE,
  author_id   UUID NOT NULL REFERENCES users(user_id),
  parent_comment_id UUID REFERENCES comments(comment_id),
  content_text TEXT NOT NULL,
  created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  CONSTRAINT chk_comment_depth CHECK (parent_comment_id IS NULL) -- MVP: 1階層のみ
);
CREATE INDEX IF NOT EXISTS idx_comments_post_created ON comments(post_id, created_at DESC);

-- 疎行列E（評価行列：行=評価者, 列=被評価者）
CREATE TABLE IF NOT EXISTS evaluation_matrix (
  evaluator_id UUID NOT NULL REFERENCES users(user_id),
  evaluatee_id UUID NOT NULL REFERENCES users(user_id),
  value        DOUBLE PRECISION NOT NULL DEFAULT 0.0,
  PRIMARY KEY (evaluator_id, evaluatee_id)
);
-- 行インデックス（行/列検索用）
CREATE INDEX IF NOT EXISTS idx_eval_by_evaluator ON evaluation_matrix(evaluator_id);
CREATE INDEX IF NOT EXISTS idx_eval_by_evaluatee ON evaluation_matrix(evaluatee_id);

-- 貢献度 c
CREATE TABLE IF NOT EXISTS contribution_vector (
  user_id UUID PRIMARY KEY REFERENCES users(user_id),
  value   DOUBLE PRECISION NOT NULL DEFAULT 1.0
);
CREATE INDEX IF NOT EXISTS idx_cv_value_desc ON contribution_vector(value DESC);

-- 取引ログ（監査）＋冪等制御
CREATE TABLE IF NOT EXISTS transaction_log (
  transaction_id   BIGSERIAL PRIMARY KEY,
  transaction_type VARCHAR(16) NOT NULL CHECK (transaction_type IN ('LIKE', 'QUOTE')),
  actor_id         UUID NOT NULL REFERENCES users(user_id),
  target_post_id   UUID NOT NULL REFERENCES posts(post_id),
  amount           NUMERIC(12,6) NOT NULL,
  request_id       UUID NOT NULL,   -- 冪等制御
  details          JSONB,
  created_at       TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE(request_id)
);
CREATE INDEX IF NOT EXISTS idx_tx_actor_created ON transaction_log(actor_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_tx_target_created ON transaction_log(target_post_id, created_at DESC);

-- 設定（自然回収率γなど）
CREATE TABLE IF NOT EXISTS settings (
  key   TEXT PRIMARY KEY,
  value TEXT NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
INSERT INTO settings(key, value) VALUES('recovery_gamma', '0.03')
  ON CONFLICT(key) DO NOTHING;

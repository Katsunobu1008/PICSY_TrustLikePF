# PICSY_TrustLikePF — README

> **情報金融（Information Finance）**の創出  
> 情報の拡散（Amplification）と価値（Value）を同一の計測系に落とし込み、**貢献と創造性が正当に報われる**エコシステムを設計する。

---

## 0. 概要（Abstract）

現行のSNSは、拡散（リーチ）と価値（貢献）が分離しており、**アテンション・エコノミー**の下で扇動的・低品質の情報が過剰に増幅されやすい。  
**PICSY_TrustLikePF** は、有限・コスト付きの「いいね」を伝播投資貨幣 **PICSY** として再定義し、**仮想中央銀行法**にもとづく行列モデルで**貢献度（contribution）**を時々刻々に再評価する。

- 「いいね」は**無限の賛意**ではなく、**希少でコストのある投資行為**である（1 like = δ PICSY）。
- 各ユーザーの可用「いいね」能力は、**購買力** \( \mathrm{PP}_i = E_{ii} \cdot c_i \) により制約される。
- 価値はネットワーク上を**伝播**し、**目利き（curation）**そのものが価値を持つ **キュレーション資本** を生む。
- クリエイターは**原作印税率** \( \rho_s \) を設定し、引用先で生じた価値の一部を持続的に還流できる（拡張仕様）。

本リポジトリは、上記の思想を**モジュラーモノリス**として実装するための**プロトタイピングの土台**（計算エンジン・API方針・UI指針・数理仕様）をまとめる。

---

## 1. 数学的基礎（Mathematical Foundations）

### 1.1 評価行列と予算

- **評価行列** \( E \in \mathbb{R}_{\ge 0}^{N \times N} \)  
  各行は**行確率**：\( \sum_j E_{ij} = 1 \)。  
  \( E_{ii} \) はユーザー \( i \) の **予算（自己評価）**。
- **貢献度ベクトル** \( \boldsymbol{c} \in \mathbb{R}_{\ge 0}^{1 \times N} \) は、合計が人数 \(N\) になるように正規化する：
  \[
    \sum_{i=1}^N c_i = N.
  \]

### 1.2 仮想中央銀行法（自己ループの除去と均等化）

自己ループ（予算）をいったん外し、**自分以外**の \(N-1\) 人へ均等再配布した実効行列 \(E'\) を用いて貢献度を定義する。

\[
  E' \;=\; E \;-\; B \;+\; \frac{1}{N-1}\,BD,\quad
  B=\mathrm{diag}(E_{11},\dots,E_{NN}),\ \ D=J-I.
\]

貢献度は **左固有ベクトル**（ペロン–フロベニウス）として与える：
\[
  \boldsymbol{c} E' = \boldsymbol{c}, \qquad \sum_i c_i = N.
\]

> 直感：自己ループを取り除き、予算分を周囲へ最低限度の「社会的ベースライン」として配ることで、価格式（下記）が**線形で透明**になる。

### 1.3 取引（いいね）と定価

買い手 \(b\) が売り手 \(s\) に「いいね」＝価値 \( \delta \) を与えるとき、**実コスト**（予算から減る量） \( \alpha \) は：
\[
  \boxed{\ \delta = \alpha \, c_b \ \Rightarrow\ \alpha = \frac{\delta}{c_b}\ }
\]
更新は買い手行の2項目のみ：
\[
  E_{bb} \leftarrow E_{bb} - \alpha,\qquad E_{bs} \leftarrow E_{bs} + \alpha.
\]

> **購買力**： \( \mathrm{PP}_i = E_{ii} \cdot c_i \)。同じ \( \delta \) を与えるとき、**重い人のいいねほど安い**（\( \alpha = \delta/c_b \) が小さい）。

### 1.4 自然回収（枯渇防止・立ち上げ）

回復率 \( \gamma \in (0,1) \) に対し、
\[
  E_{ij} \leftarrow (1-\gamma) E_{ij}\ (i\neq j),\quad
  E_{ii} \leftarrow E_{ii} + \gamma (1 - E_{ii}).
\]
使い切った人ほど早く回復し、新規参加者（初期 \(E_{ii}=0\)）も**自然に立ち上がる**。

### 1.5 メンバー追加（既存の貢献度・予算を不変）

人数 \(N\) に新規 \(N\!+\!1\) を追加するとき、以下で **既存 \(c\) と予算を不変**に保てる。  
\[
  x = \frac{1}{N},\quad
  \tilde{E}_{ij}=(1-x)E_{ij}\ (i\ne j\le N),\ \tilde{E}_{ii}=E_{ii},
\]
\[
  \tilde{E}_{i,N+1}=x(1-E_{ii}),\quad
  \tilde{E}_{N+1,j}=\frac{c_j}{N},\quad
  \tilde{E}_{N+1,N+1}=0.
\]
このとき \( \tilde{\boldsymbol{c}}=(c_1,\dots,c_N,1) \) が \((N+1)\) 人系の左固有ベクトルとなる（証明済）。

---

## 2. ミニ例（3人）

初期：
\[
  E=\begin{pmatrix}
  0.2 & 0.4 & 0.4\\
  0.4 & 0.2 & 0.4\\
  0.4 & 0.4 & 0.2
  \end{pmatrix},\quad
  B=\mathrm{diag}(0.2,0.2,0.2),\quad
  D=\begin{pmatrix}0&1&1\\1&0&1\\1&1&0\end{pmatrix}.
\]
\[
  E' = E-B+\tfrac{1}{2}BD
     = \begin{pmatrix}0&0.5&0.5\\0.5&0&0.5\\0.5&0.5&0\end{pmatrix}.
\]
\[
  \boldsymbol{c}=(1,1,1),\quad \mathrm{PP}= (0.2,0.2,0.2).
\]

B→A へ「1いいね＝δ=0.05」：
\[
  \alpha = \frac{0.05}{c_B} = 0.05.
\]
\[
  E_{BB}:0.2\to 0.15,\ \ E_{BA}:0.4\to 0.45.
\]
再計算後の貢献度はわずかに
\[
  \boldsymbol{c}\approx (1.0167,\ 1.0000,\ 0.9833)
\]
（Aが微増、第三者にも**伝播**が及ぶ）。購買力は \( \mathrm{PP}_B=0.15 \) に低下。  
自然回収（例：\( \gamma=0.1\)）で対角が回復、次の行動余力が戻る。

---

## 3. アーキテクチャ（Modular Monolith with BFF）

**モジュラーモノリス**を採用し、将来のサービス分割を前提に**ドメインごとに厳密に分割**する。

[Browser: Vue.js]
│
│ (HTML/CSS/JS, fetch /api/*)
▼
[Web Server (BFF): Node.js/Express]
│ (forwards)
▼
[API Server (Java/Spring Boot: PICSY Core)]
│ (JPA, Tx, validation)
▼
[DB: PostgreSQL]


### 採択理由
- **開発速度 vs 分散の複雑性**：初期は**単一デプロイ**でMVPを創ることを優先していました。しかし制作過程の価値検証に不要な複雑性が増えてしまう部分があることは意識していました。
- **発展可能性**：モジュール境界（`UserService`, `ContentService`, `PicsyEngine`）を厳密化し、計算負荷が高まったモジュールだけを**後から**独立サービス化できる点や、情報を投稿する機能やSNSの機能については、今後切り分けたいと考えていたためモノリスでもなければ、マイクロサービスアーキテクチャでもない、その間くらいのモジュラーモノリスとしてアーキテクチャを採用しました。
- **一貫性**：価初期は単一のPostgreSQLで**強い一貫性**を担保しました。具体的には、ビヨンドソフトウェアアーキテクチャという書籍を読みながら考えました。

### 技術選定
| 領域 | 技術 | 理由 |
|---|---|---|
| API (Core) | Java 21 + Spring Boot 3 | 型安全・豊かなTx制御で**価値移転の信頼性**を担保。 |
| BFF | Node.js 20 + Express | APIプロキシ/静的配信に適し、**疎結合**を保つ。 |
| Frontend | Vue.js 3 + Vite | 宣言的UIと高速なHMRで**プロトタイピング迅速**。 |
| DB | PostgreSQL 15+ | ACID, インデックス/トランザクションが堅牢、Flywayでマイグレーション管理。 |
| Packaging | Docker + docker-compose | 誰でも**一発再現**できる開発環境。 |

---

## 4. データモデル（要点）

### 4.1 コア・エンティティ
- `users(id, handle, created_at, ...)`
- `posts(id, author_id, title, body, tags[], image_url, created_at, ...)`
- `likes(id, post_id, from_user, to_user, delta, alpha, c_b_at_tx, created_at)`  
  **監査台帳**：数理の全パラメータ（δ, α, c_b）を必ず記録。
- `matrix_entries(i, j, value)`（Eの疎表現） / `budgets(i, value)`（対角キャッシュ）

### 4.2 不変条件（Invariants）
- 行確率：`Σ_j E[i,j] = 1`（更新は常に `(-α, +α)` の**対**で行う）
- 非負：`E[i,j] ≥ 0`
- 貢献度：`sum(c) = N`（毎回の再計算で維持）

---

## 5. API設計（抜粋）

- `POST /api/likes`  
  - 入力：`postId`, `delta`（default 0.05）  
  - 処理：`alpha = delta / c_b` → `E_bb -= alpha, E_bs += alpha` → `recompute c`  
  - 出力：`{ alpha, newC, newBudget, ledgerId }`
- `POST /api/recovery`  
  - 入力：`gamma`  
  - 処理：自然回収 → `recompute c`
- `POST /api/members`（加入）  
  - 処理：`x=1/N`で**不変拡張**（再計算不要）  
  - 出力：`{ newN, userId }`

> **トランザクション**：いずれも**単一Tx**で、台帳（`likes`）への書き込みと `E` の更新

---

## 6. アルゴリズム

### 6.1 反復法（左固有）｜貢献度の計算のためのアルゴリズム
```pseudo
function powerIterationLeft(E):
  v := normalize_to_sum1([1/N]*N)
  repeat:
     v_next := vE' = (vE) - (v ⊙ diag(E)) + (S - v ⊙ diag(E))/(N-1)
       where S = Σ_i v_i E_ii
     v_next := normalize_to_sum1(v_next)
  until ||v_next - v||_1 < tol
  return v_next * N

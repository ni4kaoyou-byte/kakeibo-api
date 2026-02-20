# Implementation Backlog (Start to Finish)

このバックログは `IMP-001` から `IMP-024` まで順に進める実装計画です。

## Execution Order

1. Phase 1: Foundation & Security Bootstrap
2. Phase 2: Data / Domain
3. Phase 3: Command APIs
4. Phase 4: Query APIs
5. Phase 5: Observability / Audit / Security Hardening
6. Phase 6: Reliability / Release

## Tickets Overview

| ID | Phase | Type | Priority | Estimate | Depends On |
|---|---|---|---|---|---|
| IMP-001 | 1 | Task | P0 | 1.0d | - |
| IMP-002 | 1 | Story | P0 | 1.5d | IMP-001 |
| IMP-003 | 1 | Story | P0 | 1.5d | IMP-002 |
| IMP-004 | 2 | Story | P0 | 1.0d | IMP-001 |
| IMP-005 | 2 | Task | P1 | 0.5d | IMP-004 |
| IMP-006 | 2 | Story | P0 | 1.5d | IMP-004 |
| IMP-007 | 2 | Story | P0 | 1.5d | IMP-006 |
| IMP-008 | 3 | Story | P0 | 1.5d | IMP-003, IMP-007 |
| IMP-009 | 3 | Story | P0 | 1.0d | IMP-008 |
| IMP-010 | 3 | Story | P0 | 1.0d | IMP-007, IMP-003 |
| IMP-011 | 3 | Story | P1 | 1.0d | IMP-010, IMP-007 |
| IMP-012 | 4 | Story | P0 | 1.0d | IMP-009 |
| IMP-013 | 4 | Story | P1 | 1.0d | IMP-010, IMP-011 |
| IMP-014 | 4 | Story | P0 | 1.5d | IMP-012, IMP-009 |
| IMP-015 | 4 | Task | P1 | 1.0d | IMP-012, IMP-013 |
| IMP-016 | 5 | Story | P0 | 1.0d | IMP-008, IMP-012 |
| IMP-017 | 5 | Story | P0 | 1.5d | IMP-008, IMP-016 |
| IMP-018 | 5 | Task | P0 | 0.5d | IMP-008 |
| IMP-019 | 5 | Story | P1 | 1.0d | IMP-012 |
| IMP-020 | 5 | Story | P1 | 1.0d | IMP-019 |
| IMP-021 | 6 | Story | P1 | 1.5d | IMP-005 |
| IMP-022 | 6 | Story | P0 | 1.0d | IMP-014, IMP-015 |
| IMP-023 | 6 | Story | P0 | 1.0d | IMP-001, IMP-020 |
| IMP-024 | 6 | Task | P0 | 1.0d | IMP-022, IMP-023, IMP-021, IMP-017 |

## Ticket Details

### IMP-001 開発基盤と品質ゲート整備
- Type: Task
- Priority: P0
- Estimate: 1.0d
- Depends On: -
- Goal: PRテンプレート、CIの最小パイプライン、テスト実行基盤を整える。
- Acceptance Criteria:
  - `build + test` がCIで実行される。
  - PRテンプレートにチケットID/TDD証跡/チェックリスト欄がある。
  - `AGENT.md` と整合する運用手順が確認されている。

### IMP-002 OIDCローカル基盤（Keycloak）構築
- Type: Story
- Priority: P0
- Estimate: 1.5d
- Depends On: IMP-001
- Goal: Docker環境でKeycloakを起動し、開発用Realm/Client/Userを用意する。
- Acceptance Criteria:
  - `docker compose up` でKeycloakが起動できる。
  - 開発用クライアント設定がコード管理される。
  - JWT発行と公開鍵取得が確認できる。

### IMP-003 Spring Security + JWT Resource Server 実装
- Type: Story
- Priority: P0
- Estimate: 1.5d
- Depends On: IMP-002
- Goal: API認証をJWT検証に切り替える。
- Acceptance Criteria:
  - 未認証アクセスが `401` になる。
  - 有効トークンでAPIアクセスできる。
  - 失効/不正トークンで拒否される。

### IMP-004 Flywayスキーマ作成（users/categories/transactions）
- Type: Story
- Priority: P0
- Estimate: 1.0d
- Depends On: IMP-001
- Goal: REQ-004準拠の主要テーブルを作成する。
- Acceptance Criteria:
  - `V2-V4` migration が追加される。
  - FK/CK/UK制約が適用される。
  - ローカルDBで適用成功する。

### IMP-005 Flyway追加（budgets/index/制約）
- Type: Task
- Priority: P1
- Estimate: 0.5d
- Depends On: IMP-004
- Goal: 予算テーブルと必要インデックスを追加する。
- Acceptance Criteria:
  - `V5-V6` migration が追加される。
  - 集計向けインデックスが作成される。
  - migration再実行で破綻しない。

### IMP-006 Domainモデル実装（VO/Entity）
- Type: Story
- Priority: P0
- Estimate: 1.5d
- Depends On: IMP-004
- Goal: Transaction/Category/Budget の不変条件を実装する。
- Acceptance Criteria:
  - 金額・種別・日付のルールがDomainで担保される。
  - 不正データが生成できない設計になる。
  - Domain単体テストが追加される。

### IMP-007 Repositoryアダプタ + Testcontainers検証
- Type: Story
- Priority: P0
- Estimate: 1.5d
- Depends On: IMP-006
- Goal: 永続化層を実装し、PostgreSQLで検証する。
- Acceptance Criteria:
  - Repository実装が追加される。
  - Testcontainers(PostgreSQL)統合テストが通る。
  - N+1の初期対策が入る。

### IMP-008 Transaction Command: Create
- Type: Story
- Priority: P0
- Estimate: 1.5d
- Depends On: IMP-003, IMP-007
- Goal: `POST /transactions` を実装する。
- Acceptance Criteria:
  - 正常時 `201` と作成レスポンスを返す。
  - バリデーション違反で `400` を返す。
  - TDDで実装される。

### IMP-009 Transaction Command: Update/Delete
- Type: Story
- Priority: P0
- Estimate: 1.0d
- Depends On: IMP-008
- Goal: `PATCH/DELETE /transactions/{id}` を実装する。
- Acceptance Criteria:
  - 更新/削除で `204` を返す。
  - 論理削除が反映される。
  - 他ユーザーリソースは `403`。

### IMP-010 Category Command APIs
- Type: Story
- Priority: P0
- Estimate: 1.0d
- Depends On: IMP-007, IMP-003
- Goal: `POST/PATCH/DELETE /categories` を実装する。
- Acceptance Criteria:
  - 重複カテゴリ名で `409` を返す。
  - 使用中カテゴリは物理削除しない。
  - 監査対象アクションに含まれる。

### IMP-011 Budget Command APIs
- Type: Story
- Priority: P1
- Estimate: 1.0d
- Depends On: IMP-010, IMP-007
- Goal: `POST/PATCH/DELETE /budgets` を実装する。
- Acceptance Criteria:
  - `yearMonth` フォーマット検証がある。
  - 正常系/異常系テストがある。
  - 他ユーザーアクセスは `403`。

### IMP-012 Transaction Query APIs
- Type: Story
- Priority: P0
- Estimate: 1.0d
- Depends On: IMP-009
- Goal: `GET /ledger/transactions`, `GET /ledger/transactions/{id}` を実装する。
- Acceptance Criteria:
  - フィルタ（from/to/type/categoryId）が機能する。
  - 自分のデータのみ返る。
  - レスポンスがOpenAPI契約に一致する。

### IMP-013 Category/Budget Query APIs
- Type: Story
- Priority: P1
- Estimate: 1.0d
- Depends On: IMP-010, IMP-011
- Goal: `GET /catalog/categories`, `GET /planning/budgets` を実装する。
- Acceptance Criteria:
  - 一覧取得が契約どおり動作する。
  - 認可と未認証の挙動が正しい。
  - APIテストが追加される。

### IMP-014 Analytics Query APIs（日/月/年）
- Type: Story
- Priority: P0
- Estimate: 1.5d
- Depends On: IMP-012, IMP-009
- Goal: `GET /analytics/reports/*` を実装する。
- Acceptance Criteria:
  - 日次/月次/年次集計が正しい。
  - `month=13` など不正入力で `400`。
  - 削除済み取引を除外する。

### IMP-015 Query改善（ページング/ソート/入力検証）
- Type: Task
- Priority: P1
- Estimate: 1.0d
- Depends On: IMP-012, IMP-013
- Goal: Query APIの実運用最低機能を追加する。
- Acceptance Criteria:
  - ページングが導入される。
  - 安全なソートキー制限がある。
  - クエリバリデーションが統一される。

### IMP-016 RBAC強化（USER/ADMIN）
- Type: Story
- Priority: P0
- Estimate: 1.0d
- Depends On: IMP-008, IMP-012
- Goal: 役割ベース認可を全APIに適用する。
- Acceptance Criteria:
  - 権限不足は `403`。
  - ADMIN専用操作の制御がある。
  - セキュリティテストが追加される。

### IMP-017 監査ログ実装（作成/更新/削除/認証イベント）
- Type: Story
- Priority: P0
- Estimate: 1.5d
- Depends On: IMP-008, IMP-016
- Goal: 監査対象イベントを記録し、追跡可能にする。
- Acceptance Criteria:
  - `actorId/action/targetId/timestamp/ip` が記録される。
  - before/after 差分を保持できる。
  - 監査ログ検証テストがある。

### IMP-018 共通エラーハンドリング
- Type: Task
- Priority: P0
- Estimate: 0.5d
- Depends On: IMP-008
- Goal: エラーフォーマットを `code/message/details` へ統一する。
- Acceptance Criteria:
  - 主要例外が統一形式で返る。
  - 4xx/5xxで契約崩れがない。
  - OpenAPIと整合する。

### IMP-019 メトリクス実装（Micrometer/Actuator）
- Type: Story
- Priority: P1
- Estimate: 1.0d
- Depends On: IMP-012
- Goal: レイテンシ・エラー率・DB指標を取得可能にする。
- Acceptance Criteria:
  - Prometheus scrapeできる。
  - APIごとのメトリクスを取得できる。
  - 監視対象がドキュメント化される。

### IMP-020 監視スタック構築（Prometheus/Grafana/Alertmanager）
- Type: Story
- Priority: P1
- Estimate: 1.0d
- Depends On: IMP-019
- Goal: ローカルで監視とアラート検証が可能な構成を作る。
- Acceptance Criteria:
  - ダッシュボードを1枚以上提供。
  - 閾値アラートが発火検証できる。
  - 運用ドキュメントを追加する。

### IMP-021 バックアップ/復旧自動化（pgBackRest）
- Type: Story
- Priority: P1
- Estimate: 1.5d
- Depends On: IMP-005
- Goal: バックアップ・復旧手順を実運用レベルで整備する。
- Acceptance Criteria:
  - 日次フル + WALアーカイブが自動化される。
  - リストア手順がrunbook化される。
  - RPO/RTO検証結果を記録する。

### IMP-022 負荷試験（k6）とSLO検証
- Type: Story
- Priority: P0
- Estimate: 1.0d
- Depends On: IMP-014, IMP-015
- Goal: レイテンシSLOを計測し、ボトルネックを把握する。
- Acceptance Criteria:
  - CRUD/API集計のk6シナリオがある。
  - `p95/p99` 計測結果が記録される。
  - SLO未達時は改善項目が起票される。

### IMP-023 セキュリティスキャンCI統合
- Type: Story
- Priority: P0
- Estimate: 1.0d
- Depends On: IMP-001, IMP-020
- Goal: SAST/Dependency/DAST をCIに組み込む。
- Acceptance Criteria:
  - Trivy/Semgrep(or SonarQube)/ZAP が実行される。
  - 重大検知でCI failにできる。
  - 結果レポートの参照先が残る。

### IMP-024 リリース判定（Finish Ticket）
- Type: Task
- Priority: P0
- Estimate: 1.0d
- Depends On: IMP-022, IMP-023, IMP-021, IMP-017
- Goal: DoDとNFRを満たし、`v1.0.0-rc1` を判定する。
- Acceptance Criteria:
  - `docs/definition-of-done.md` の全項目を満たす。
  - 未解決リスクが整理されている。
  - リリースノートを作成してタグ付け準備が完了する。

## Where to Start / Where to End

- Start: `IMP-001` から着手する。
- End: `IMP-024` を完了し、リリース判定を通して終了する。

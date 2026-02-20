# Code Review Checklist (DDD lite + Layered)

このチェックリストは、家計簿APIを `DDD lite` と `レイヤードアーキテクチャ` で実装する前提です。

## 1. Architecture

- [ ] 依存方向が `presentation -> application -> domain <- infrastructure` を守っている
- [ ] `controller` から `repository` を直接呼んでいない
- [ ] `domain` が Spring / Web / JPA アノテーションに直接依存していない
- [ ] 1つのPRでユースケースを詰め込みすぎていない（責務が明確）

## 2. Domain

- [ ] 業務ルール（不変条件）が Entity / Value Object で表現されている
- [ ] 金額・日付・種別などの重要概念がプリミティブのまま放置されていない
- [ ] 不正状態を作れる public setter がない
- [ ] ドメイン命名がユビキタス言語に揃っている
- [ ] 集約境界を越える更新を1つの操作で行っていない

## 3. Application

- [ ] UseCase単位で入力と出力が明確（DTOやCommandが分離）
- [ ] トランザクション境界が Application Service で管理されている
- [ ] 認可ルール（自分のデータのみ操作可能）がユースケースに組み込まれている
- [ ] 例外変換方針（業務例外とシステム例外）が統一されている

## 4. Presentation (API)

- [ ] HTTPメソッドが意図に合っている（`GET/POST/PATCH/DELETE`）
- [ ] Request DTO にバリデーションがある（null, range, format）
- [ ] Entity をそのままレスポンスに返していない
- [ ] エラーレスポンス形式が統一されている
- [ ] OpenAPI仕様と実装に差分がない

## 5. Infrastructure

- [ ] Repository実装がドメイン制約を壊していない
- [ ] N+1や不要なEAGERロードがない
- [ ] スキーマ変更が Flyway migration で管理されている
- [ ] migration が再実行可能で、順序・ロールバック方針が明確
- [ ] `ddl-auto` への依存が本番運用前提になっていない

## 6. Security & Ops

- [ ] 機密情報（パスワード、トークン、個人情報）をログ出力していない
- [ ] Actuator の公開範囲が最小化されている
- [ ] 監査上必要な操作（作成・更新・削除）の追跡方針がある
- [ ] 環境変数と `.env` の取り扱いが統一されている

## 7. Testing

- [ ] Domainルールの単体テストがある（境界値・異常系を含む）
- [ ] UseCaseの結合テストがある（主要シナリオ）
- [ ] Repositoryテストを Testcontainers(PostgreSQL) で確認している
- [ ] APIテストで正常系とバリデーションエラー系を検証している
- [ ] セキュリティ要件（未認証・権限不足）のテストがある

## Review Output Template

レビューコメントを残すときは、以下の形式で統一すると追跡しやすくなります。

- `Severity`: Critical / High / Medium / Low
- `Category`: Architecture / Domain / Application / Presentation / Infrastructure / Security / Test
- `Location`: file path + line
- `Issue`: 何が問題か
- `Impact`: どんな不具合につながるか
- `Fix`: 修正案

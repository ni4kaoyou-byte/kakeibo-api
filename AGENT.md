# AGENT.md

このファイルは、このリポジトリでコードを作成・変更するエージェント向けの必須ルールです。

## 0. 最優先ルール

- 新規コードを書く前に、必ず `docs/review-checklist.md` を確認し、対象項目を洗い出すこと。
- 実装は必ずテスト駆動開発（TDD）で進めること。
- 例外が必要な場合は、理由・影響・代替策を明記して合意を取ること。

## 1. TDD ルール（必須）

- `Red -> Green -> Refactor` の順序を守る。
- 失敗するテストを書く前に、プロダクションコードを追加しない。
- 最小実装でテストを通し、その後にリファクタリングする。
- バグ修正時は、必ず再現テストを先に追加する。
- テストが不安定（flaky）な状態でマージしない。

## 2. 実装ワークフロー

1. 要件確認: 目的、入力、出力、制約を明確化する。
2. チェックリスト確認: `docs/review-checklist.md` の該当項目を確認する。
3. テスト設計: unit / integration / API のどこで担保するか決める。
4. Red: 失敗するテストを追加する。
5. Green: テストを満たす最小実装を追加する。
6. Refactor: 振る舞いを変えずに設計を改善する。
7. 自己レビュー: チェックリストで差分を再点検する。
8. 検証: 関連テストを実行し、結果を記録する。

## 3. アーキテクチャ方針（DDD lite + Layered）

- 依存方向は `presentation -> application -> domain <- infrastructure` を維持する。
- `controller` から `repository` を直接呼ばない。
- ドメインルールは `domain` に置き、UI/DB 都合を混ぜない。
- `Entity` と API の `DTO` は分離する。
- ドメイン層で Spring / Web / JPA への直接依存を避ける。

## 4. テスト方針

- Domain: 単体テストで業務ルールと境界値を保証する。
- Application: ユースケース単位の結合テストを用意する。
- Infrastructure: Repository は Testcontainers(PostgreSQL) で検証する。
- Presentation: API の正常系・異常系・バリデーションを検証する。
- Security: 未認証/権限不足のテストを含める。

## 5. DB・マイグレーション方針

- スキーマ変更は Flyway migration で管理する。
- 本番想定では `ddl-auto` 依存の運用をしない。
- migration は再実行可能性と順序を意識して作成する。

## 6. セキュリティ・運用方針

- パスワードやトークンなどの機密情報をコード・ログに出さない。
- `.env` はコミットしない（`.env.example` を更新する）。
- Actuator の公開範囲は最小限にする。

## 7. PR / レビュー運用

- PR には以下を必ず記載する。
  - 変更概要（何を、なぜ）
  - 追加/更新したテスト
  - 実行した検証コマンドと結果
  - `docs/review-checklist.md` の主要確認結果
- PR作成時は `.github/pull_request_template.md` を必ず使用する。
- CI（`.github/workflows/ci.yml`）で `build + test` が成功していることをマージ条件にする。
- レビュー指摘は `Severity / Category / Location / Issue / Impact / Fix` で記録する。

## 8. Definition of Done

- 要件を満たすテストが存在し、全て成功している。
- `docs/review-checklist.md` の該当項目を満たしている。
- 変更に必要なドキュメント（README, API仕様, migration）が更新されている。
- 将来の保守者が意図を追える状態である。

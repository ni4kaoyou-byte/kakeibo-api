# Definition of Done (DoD)

このドキュメントは、家計簿APIプロジェクトでPRを完了とみなす基準です。

## Merge Checklist

- [ ] 対象チケットIDがPRに記載されている
- [ ] チケット受け入れ条件との対応が説明されている
- [ ] TDD（Red/Green/Refactor）の実施内容が記載されている
- [ ] 追加/更新したテストがすべて成功している
- [ ] `docs/review-checklist.md` の該当項目を自己点検済み
- [ ] API変更時は `docs/openapi-draft.yaml` を更新済み
- [ ] DB変更時は Flyway migration を追加し適用確認済み
- [ ] 機密情報の漏洩がない
- [ ] 監査ログ要件を満たしている
- [ ] 変更に応じたドキュメント更新が完了している

## Mandatory Quality Gates

1. Test Gate
- Unit / Integration / API の対象テストを実行済み。
- 401/403 を含むセキュリティテストがある。

2. Architecture Gate
- 依存方向が `presentation -> application -> domain <- infrastructure` を守る。
- Domainにフレームワーク都合を漏らさない。

3. Contract Gate
- CQRS-liteのCommand/Query分離を守る。
- エラー契約は `code/message/details` に統一する。

4. Data Gate
- スキーマ変更は Flywayで管理する。
- migration は再実行可能である。

## Exception Handling

例外的に未達項目がある場合は、PRに以下を必ず記載する。

- 未達項目
- 理由
- 影響範囲
- 補完予定日

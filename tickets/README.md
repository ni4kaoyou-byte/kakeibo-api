# Tickets

家計簿APIの要件定義チケットをこのディレクトリで管理します。

## Status

- `Todo`: 未着手
- `In Progress`: 進行中
- `Done`: 完了

## Ticket Board

| ID | Title | Status | Depends On |
|---|---|---|---|
| REQ-001 | 対象ユーザーと目的の定義 | Done | - |
| REQ-002 | MVP機能の確定 | Done | REQ-001 |
| REQ-003 | 業務ルールの確定 | Done | REQ-002 |
| REQ-004 | データモデル定義 | Done | REQ-003 |
| REQ-005 | API契約定義 | Done | REQ-003, REQ-004 |
| REQ-006 | 非機能要件定義 | Done | REQ-005 |
| REQ-007 | 受け入れ条件定義 | Done | REQ-005, REQ-006 |
| REQ-008 | Definition of Done定義 | Done | REQ-007 |
| REQ-009 | 実装チケット分解 | Done | REQ-008 |

## Rules

- ステータス変更時は `README.md` と各チケットの両方を更新する。
- 完了時は受け入れ条件のチェックを埋める。
- 実装前にこのチケット群が合意済みであることを確認する。

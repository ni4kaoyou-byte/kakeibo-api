# Keycloak Local Setup (IMP-002)

## Purpose

ローカル開発で OIDC の発行元を用意し、JWT発行と公開鍵取得を確認する。

## Configuration

- Realm: `kakeibo-dev`
- Client: `kakeibo-api-dev-client`
- Test User: `dev-user` / `dev-pass`
- Admin User: `${KEYCLOAK_ADMIN}` / `${KEYCLOAK_ADMIN_PASSWORD}`

定義ファイル:
- `infra/keycloak/realm-export.json`

## Start

```bash
docker compose up -d keycloak
```

Keycloak URL:
- `http://localhost:${KEYCLOAK_PORT:-8081}`

## Verify OpenID Configuration

```bash
curl -s http://localhost:8081/realms/kakeibo-dev/.well-known/openid-configuration
```

## Issue Access Token (Resource Owner Password)

```bash
curl -s -X POST http://localhost:8081/realms/kakeibo-dev/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=kakeibo-api-dev-client" \
  -d "username=dev-user" \
  -d "password=dev-pass"
```

期待値:
- `access_token` が返る
- `token_type` は `Bearer`

## Retrieve Public Key (JWKS)

```bash
curl -s http://localhost:8081/realms/kakeibo-dev/protocol/openid-connect/certs
```

期待値:
- `keys` 配列が返る

## Notes

- `start-dev --import-realm` で起動時に `infra/keycloak/realm-export.json` を読み込む。
- 認証方式のアプリ統合は `IMP-003` で実施する。

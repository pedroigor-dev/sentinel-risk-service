# ADR 0004: usar API key no recorte inicial

- Status: aceito
- Data: 2026-08-16

## Contexto

O projeto precisa demonstrar uma fronteira autenticada sem incluir um provedor de identidade inteiro. Deixar o endpoint aberto seria um exemplo ruim, enquanto implementar OAuth só para a demonstração desviaria o foco do problema distribuído.

## Decisão

Uma API key configurada por variável de ambiente protege API e métricas. O health check permanece público. A comparação usa `MessageDigest.isEqual`, e a chave nunca entra em logs.

## Consequências

O ambiente local fica simples. Não há identidade por cliente, escopos ou rotação coordenada. Uma implantação empresarial deveria usar OAuth 2.1 com `client_credentials`, mTLS ou ambos.

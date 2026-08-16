# ADR 0003: combinar chave de idempotência e fingerprint

- Status: aceito
- Data: 2026-08-16

## Contexto

Clientes repetem chamadas após timeout. Retornar uma nova análise em cada tentativa cria registros e eventos duplicados. Confiar apenas na chave também é perigoso: o cliente poderia reutilizá-la com outro corpo e receber uma resposta que não corresponde à solicitação atual.

## Decisão

O serviço normaliza a chave, gera SHA-256 de uma representação canônica do comando e persiste os dois valores. Chave e fingerprint iguais reproduzem a resposta; chave igual com fingerprint diferente retorna `409 Conflict`.

## Consequências

Repetições ficam seguras e previsíveis. Alterações futuras no contrato exigirão cuidado com a representação canônica. A restrição única do banco continua necessária para concorrência entre instâncias.

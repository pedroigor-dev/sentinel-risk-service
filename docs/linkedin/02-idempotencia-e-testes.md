# Post 2: idempotência não termina na chave

Ao implementar idempotência, encontrei uma situação que costuma passar batida: duas requisições usam a mesma chave, mas enviam corpos diferentes.

Se o serviço olhar apenas para `Idempotency-Key`, pode devolver uma resposta antiga para uma operação que nunca foi processada. No Sentinel, a chave é armazenada com um SHA-256 da representação canônica da requisição.

O comportamento ficou assim:

- mesma chave e mesmo corpo: devolve a análise já criada;
- mesma chave e corpo diferente: responde `409 Conflict`;
- duas inserções concorrentes: a restrição única do PostgreSQL decide quem vence, e o cliente pode repetir a chamada.

A suíte testa o fluxo pela API, incluindo autenticação, migration Flyway, persistência dos fatores e leitura da resposta anterior. Também há testes isolados para os limiares do motor e para as falhas do relay do outbox.

Durante essa etapa, os testes ainda encontraram duas mudanças modulares do Spring Boot 4: Flyway e Kafka precisavam dos starters próprios para ativar a autoconfiguração. Melhor descobrir isso na suíte do que no primeiro deploy.

Repositório: https://github.com/pedroigor-dev/sentinel-risk-service

#testes #idempotencia #postgresql #springboot #engenhariadesoftware

# ADR 0001: separar domínio por portas e adaptadores

- Status: aceito
- Data: 2026-08-16

## Contexto

O motor de risco precisa evoluir sem depender de HTTP, JPA ou Kafka. Misturar anotações de persistência às regras encurtaria o primeiro commit, mas tornaria cada teste mais caro e esconderia a lógica entre detalhes do framework.

## Decisão

O domínio contém modelos, regras e o `RiskEngine`. Casos de uso ficam em `application` e dependem de portas. JPA e outbox implementam essas portas em `infrastructure`; controllers pertencem a `presentation`.

## Consequências

Regras são testadas como objetos Java. Persistência e transporte podem mudar sem reescrever a pontuação. O custo é um número maior de classes e mapeamentos explícitos. Para este projeto, esse custo deixa as fronteiras visíveis e facilita a discussão arquitetural.

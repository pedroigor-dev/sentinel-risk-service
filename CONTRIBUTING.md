# Como contribuir

## Ambiente

- JDK 21
- Docker com Compose v2, caso queira executar PostgreSQL e Kafka

## Fluxo local

1. Crie uma branch curta a partir de `main`.
2. Faça uma mudança por commit, com mensagem no imperativo.
3. Execute `./mvnw clean verify`.
4. Atualize OpenAPI, ADR ou README quando o comportamento público mudar.
5. Abra um pull request explicando a decisão e os testes executados.

## Regras do código

- Regras de risco pertencem ao domínio e não importam Spring.
- Controllers não acessam repositories.
- Uma mudança de schema exige migration Flyway; `ddl-auto` permanece em `validate`.
- Eventos novos recebem versão no nome e contrato documentado.
- Falhas não devem imprimir API key, payload completo ou stack trace para o cliente.

## Antes do pull request

```bash
./mvnw clean verify
docker compose config --quiet
```

Se o Docker estiver disponível, construa também a imagem:

```bash
docker build -t sentinel-risk-service:local .
```

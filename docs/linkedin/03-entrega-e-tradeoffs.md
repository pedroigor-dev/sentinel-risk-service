# Post 3: o que ficou pronto e o que ficou de fora

Fechei a primeira versão do Sentinel Risk Service.

O projeto tem API protegida por chave, regras explicáveis, PostgreSQL com Flyway, outbox transacional, Kafka, métricas Prometheus, correlation ID e respostas Problem Details. O pipeline executa testes, cobertura mínima de 85%, Checkstyle, SpotBugs, SBOM, build da imagem e CodeQL.

Também deixei as limitações no README:

- H2 deixa a suíte rápida, mas um ambiente de homologação precisa testar PostgreSQL e Kafka reais;
- API key é suficiente para a demonstração, não para identidade entre serviços de uma empresa;
- múltiplas instâncias do relay pediriam claim curto com `SKIP LOCKED`;
- pesos fixos não substituem calibração com dados e análise de falsos positivos.

Essa parte importa para mim. Um projeto de portfólio fica mais interessante quando dá material para discutir decisões, inclusive as que eu mudaria em outro contexto.

Código, POC e ADRs: https://github.com/pedroigor-dev/sentinel-risk-service

#java #microsservicos #devportfolio #kafka #observabilidade

# Post 1: o problema antes do framework

Comecei meu segundo projeto de portfólio com uma pergunta simples de formular e chata de resolver:

Uma análise antifraude foi salva no banco, mas o Kafka ficou indisponível antes da publicação. Como impedir que os outros serviços percam essa decisão?

O Sentinel Risk Service usa Java 21 e Spring Boot 4.1, mas a parte que mais me interessou não foi criar o endpoint. Foi desenhar os limites.

O motor de risco não conhece Spring, HTTP, JPA nem Kafka. Ele recebe um contexto, executa regras pequenas e devolve score, decisão e fatores. A aplicação grava essa resposta junto com um evento de outbox na mesma transação do PostgreSQL. Depois, um relay tenta publicar o evento.

Esse desenho não promete exatamente uma vez. Se o processo cair entre o ack do Kafka e o commit do outbox, haverá duplicata. Por isso o evento carrega um `eventId` e o consumidor precisa deduplicar.

Prefiro um contrato que admite a falha real a uma garantia bonita que o sistema não consegue cumprir.

Código e diagrama: https://github.com/pedroigor-dev/sentinel-risk-service

#java #springboot #kafka #arquiteturadesoftware #backend

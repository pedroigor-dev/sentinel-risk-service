# ADR 0002: usar outbox transacional

- Status: aceito
- Data: 2026-08-16

## Contexto

Salvar a análise e publicar no Kafka em duas operações independentes cria dois estados ruins: decisão sem evento ou evento sem decisão confirmada. Uma transação distribuída adicionaria coordenação e dependência operacional desproporcionais ao serviço.

## Decisão

A aplicação grava análise e outbox na mesma transação PostgreSQL. Um processo agendado publica eventos pendentes e marca a confirmação em outra transação.

## Consequências

O banco elimina o dual write no caminho da requisição. A API não espera o Kafka para responder. A entrega é pelo menos uma vez, portanto consumidores devem deduplicar pelo `eventId`. O relay também precisa de monitoramento para evitar acúmulo silencioso.

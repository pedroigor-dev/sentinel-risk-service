# Política de segurança

## Versões suportadas

O branch `main` recebe correções. O projeto ainda não publica versões estáveis.

## Reportar uma vulnerabilidade

Use o recurso privado de reporte de vulnerabilidades do GitHub. Não abra uma issue pública com chave, payload sensível, instruções completas de exploração ou dados pessoais.

Inclua, quando possível:

- componente e commit afetados;
- impacto observado;
- passos mínimos para reprodução;
- sugestão de mitigação;
- indicação de qualquer dado real envolvido.

## Segredos

O valor `local-development-key` existe somente no ambiente de demonstração. Defina `SENTINEL_API_KEY` com um segredo longo e aleatório fora do repositório. Altere também as credenciais padrão do PostgreSQL antes de expor o serviço em qualquer rede compartilhada.

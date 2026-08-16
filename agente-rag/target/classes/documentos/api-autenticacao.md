# Documentação Técnica — CloudFlow API

## Módulo de Autenticação

### Visão geral
A CloudFlow API utiliza autenticação via **OAuth 2.0 com Client Credentials Flow** para
comunicação server-to-server, e **JWT Bearer Token** para sessões de usuário final.

### Endpoint de autenticação

`POST /v1/auth/token`

**Corpo da requisição:**
```json
{
  "client_id": "string",
  "client_secret": "string",
  "grant_type": "client_credentials"
}
```

**Resposta (200 OK):**
```json
{
  "access_token": "string",
  "token_type": "Bearer",
  "expires_in": 3600
}
```

### Erros comuns

| Código | Significado                          | Ação recomendada                       |
|--------|---------------------------------------|-----------------------------------------|
| 401    | Client ID ou secret inválidos         | Verificar credenciais no painel admin   |
| 429    | Rate limit excedido (100 req/min)     | Implementar backoff exponencial         |
| 503    | Serviço de autenticação indisponível  | Retry com Circuit Breaker recomendado   |

### Renovação de token
Tokens expiram em 3600 segundos. Recomenda-se implementar um cache local do token com
renovação automática 60 segundos antes da expiração, evitando falhas em rajadas de requisições.

### Rate Limiting
O plano Free permite até 100 requisições/minuto por client_id. Planos Enterprise
podem solicitar aumento de limite através do suporte técnico.

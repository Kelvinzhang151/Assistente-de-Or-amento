# Assistente de Orçamento por Voz

API em **Java 17 + Spring Boot + Spring AI** para controlar despesas por comando de voz. A pessoa fala (ou escreve) algo como *"gastei 25 reais no almoço"*, a IA entende, executa a função certa da aplicação, grava no banco e responde em texto e em áudio.

## Como funciona

```
Áudio (voz da pessoa)
   │
   ▼
VozService.transcrever()      áudio  ->  texto          (transcrição, Whisper)
   │
   ▼
AssistenteService             texto  ->  ChatClient + Tool Calling
   │                          a IA escolhe e executa uma função (tool)
   ▼
OrcamentoTools                registrar despesa / consultar totais / listar
   │
   ▼
DespesaService  ->  DespesaRepository  ->  banco H2 (arquivo)
   │
   ▼
VozService.sintetizar()       texto  ->  áudio MP3       (síntese de voz, TTS)
```

## Conceitos do Spring AI usados

| Conceito | Onde | Para quê |
|---|---|---|
| **ChatClient** | `AssistenteService` | Conversar com o modelo de IA, com instruções fixas (system prompt) |
| **Tool Calling** | `OrcamentoTools` (`@Tool`) | A IA decide chamar funções reais da aplicação |
| **Transcrição de áudio** | `VozService` | Transformar a fala em texto |
| **Síntese de voz** | `VozService` | Transformar a resposta em áudio |

## Estrutura

```
controller/   AssistenteController   endpoints do assistente (texto e voz)
              DespesaController      lista as despesas gravadas (sem IA)
service/      AssistenteService      ChatClient + tools
              VozService             transcrição e síntese de voz
              DespesaService         regras de negócio do orçamento
tools/        OrcamentoTools         funções que a IA pode chamar
repository/   DespesaRepository      acesso ao banco (Spring Data JPA)
model/        Despesa                entidade (tabela do banco)
exception/    NegocioException       erro de regra de negócio
              ApiExceptionHandler    transforma o erro em resposta HTTP 400
```

Cada camada tem uma responsabilidade: o controller só recebe e devolve dados, as tools só chamam o serviço, e as regras de negócio ficam no `DespesaService`, sem depender de IA nem de HTTP.

## Como rodar

Requisitos: Java 17+, Maven e uma chave de API da OpenAI (a transcrição, o chat e a voz usam a API paga da OpenAI).

A chave fica em uma variável de ambiente e **nunca** no código nem no GitHub.

```powershell
$env:OPENAI_API_KEY="sua-chave-aqui"
mvn spring-boot:run
```

Depois abra **http://localhost:8080**: a página permite gravar a voz pelo microfone ou escrever a pergunta.

## Endpoints

| Método | Rota | O que faz |
|---|---|---|
| POST | `/assistente/texto` | Recebe `{"mensagem": "..."}` e devolve a resposta em texto |
| POST | `/assistente/transcrever` | Recebe um áudio (campo `audio`) e devolve o texto transcrito |
| POST | `/assistente/voz` | Recebe um áudio e devolve a resposta em áudio MP3 |
| GET | `/despesas` | Lista as despesas gravadas no banco |

Exemplos no PowerShell:

```powershell
# texto
Invoke-RestMethod -Method Post -Uri http://localhost:8080/assistente/texto -ContentType "application/json" -Body '{"mensagem":"gastei 25 reais no almoco"}'

# voz (arquivo gravado, por exemplo pergunta.m4a)
curl.exe -X POST -F "audio=@pergunta.m4a" http://localhost:8080/assistente/voz --output resposta.mp3

# conferir o que foi gravado
Invoke-RestMethod http://localhost:8080/despesas
```

Exemplos de frases: *"gastei 25 reais no almoço"*, *"paguei 18 de uber"*, *"quanto gastei com alimentação?"*, *"quanto gastei no total?"*, *"quais foram as últimas despesas?"*.

## Testes

```bash
mvn test
```

Os testes cobrem o serviço e as tools, sem chamar a IA e sem precisar da chave.

## Banco de dados

H2 em arquivo (`./data/orcamento`): os dados continuam depois de reiniciar a aplicação. A pasta `data/` não vai para o Git.

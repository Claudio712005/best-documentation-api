# best-documentation-api

Modelo de referência de uma API REST **exaustivamente documentada e internacionalizada**, construída para ser lida tanto quanto chamada.

Cada endpoint existe para demonstrar um problema concreto de documentação de API e sua solução: requisições de dois campos até grafos profundamente aninhados, contratos montados por composição em vez de cópia, hierarquias polimórficas em que só as variantes concretas aparecem nos schemas, e respostas cujo formato muda conforme o que foi enviado.

Toda a superfície é negociada por `Accept-Language` — não apenas mensagens de erro e conteúdo, mas **o próprio documento OpenAPI**.

## Stack

| | |
|---|---|
| Java | 25 |
| Spring Boot | 4.1.2 (Spring Framework 7, Jackson 3) |
| springdoc-openapi | 3.2.0 |
| OpenAPI | 3.1.0 |
| Maven | 3.9.16 (via wrapper) |
| Persistência | em memória (`ConcurrentHashMap`) |

Sem banco por decisão de escopo: o objetivo é o contrato e a documentação, não mapeamento ORM.

## Como rodar

O `pom.xml` exige **Java 25**. Se o seu `JAVA_HOME` apontar para outra versão, exporte na chamada:

```bash
export JAVA_HOME=/caminho/para/jdk-25
./mvnw spring-boot:run
```

| Recurso | URL |
|---|---|
| Documentação (Swagger UI) | http://localhost:8080/docs |
| Documento OpenAPI | http://localhost:8080/v3/api-docs |
| Documento em português | http://localhost:8080/v3/api-docs/language/pt-BR |
| Documento em espanhol | http://localhost:8080/v3/api-docs/language/es-ES |

```bash
./mvnw test
```

Os documentos gerados são escritos em `target/openapi/openapi-{en-US,pt-BR,es-ES}.json` para inspeção.

## Endpoints

| Método | Rota | `operationId` | Demonstra |
|---|---|---|---|
| `GET` | `/v1/health` | `getHealth` | a menor resposta possível, como linha de base |
| `GET` | `/v1/languages` | `listSupportedLanguages` | descoberta dos idiomas em tempo de execução |
| `POST` | `/v1/payment-instruments` | `registerPaymentInstrument` | união discriminada no corpo da requisição |
| `GET` | `/v1/payment-instruments/{instrumentId}` | `getPaymentInstrument` | união aninhada em um recurso por composição |
| `GET` | `/v1/payment-instruments` | `listPaymentInstruments` | coleção de variantes mistas |
| `POST` | `/v1/boletos` | `issueBoleto` | **requisição e resposta polimórficas** no mesmo endpoint |
| `GET` | `/v1/boletos/{boletoId}` | `getBoleto` | resposta que volta como a variante emitida |
| `GET` | `/v1/boletos` | `listBoletos` | coleção que mistura categorias |

## Documentação internacionalizada

Três idiomas: `en-US` (padrão e fallback), `pt-BR`, `es-ES`. Quatro camadas distintas, deliberadamente separadas:

| Camada | Onde vive | Como é resolvida |
|---|---|---|
| Documento OpenAPI | `i18n/openapi*.properties` | chaves `doc.*` nas anotações, resolvidas por locale |
| Respostas de runtime | `i18n/messages*.properties` | `ProblemFactory` com o locale negociado |
| Violações de constraint | `i18n/validation*.properties` | `MessageSource` substituindo o bundle do Hibernate Validator |
| Painel de massa de teste | `i18n/messages*.properties` | injetado na página pelo transformer |

Peça o documento com `Accept-Language: pt-BR` e cada resumo, descrição e anotação de schema volta traduzido. O Swagger UI tem um seletor de idioma, porque cada idioma também é servido em URL própria.

**Duas armadilhas do springdoc resolvidas aqui.** A property `springdoc.api-docs.resolve-schema-properties` está desligada de propósito: o resolver interno chama `resolveProperties(schema, Locale.getDefault())` — locale padrão da JVM, não o da requisição — o que congelaria toda descrição de schema no idioma em que o servidor subiu. `DocumentLocalizationCustomizer` substitui esse comportamento como `OpenApiLocaleCustomizer`, a única interface de customizer que recebe o locale resolvido. E o springdoc não resolve chaves dentro de `@ExampleObject`, então os resumos de exemplo passam por uma segunda etapa.

## Herança no código, união discriminada no contrato

`components.schemas` publica **somente as variantes concretas**. As classes base existem no código e não aparecem no documento.

O springdoc, por conta própria, publicaria a base, um schema `allOf` por subclasse e um wrapper `<Base>Variant`. Três coisas estão erradas nisso para um contrato público:

1. a base é publicada, embora nenhuma operação a aceite ou retorne, e geradores de client produzem um tipo que ninguém consegue instanciar;
2. o wrapper não carrega discriminator, então o cliente sabe quais formatos são possíveis mas não qual campo os distingue;
3. um uso via `@Schema(implementation = Base.class)` aponta para a base, fazendo o documento afirmar que o endpoint aceita apenas os campos compartilhados.

`DiscriminatedUnionCustomizer` achata cada variante, monta a união com o mapping completo do discriminator, reescreve toda referência à base ou ao wrapper e remove ambos. As famílias são declaradas explicitamente como beans, com o mapping derivado de um `switch` exaustivo e do `@Schema(name)` de cada classe — nem um tipo novo nem um rename conseguem deixar o mapping desatualizado.

```yaml
requestBody:
  content:
    application/json:
      schema:
        oneOf:
          - $ref: '#/components/schemas/SimpleBoletoRequest'
          - $ref: '#/components/schemas/HybridBoletoRequest'
          - $ref: '#/components/schemas/SplitBoletoRequest'
        discriminator:
          propertyName: category
          mapping:
            SIMPLE: '#/components/schemas/SimpleBoletoRequest'
            HYBRID: '#/components/schemas/HybridBoletoRequest'
            SPLIT: '#/components/schemas/SplitBoletoRequest'
```

### Um endpoint, três formatos de resposta

`POST /v1/boletos` recebe a categoria numa flag do corpo, e **a resposta é a variante correspondente**:

| Categoria | Requisição | Resposta | Campos exclusivos da resposta |
|---|---|---|---|
| `SIMPLE` | 5 campos | 8 campos | — |
| `HYBRID` | 8 campos | 10 campos | `qrCode` (objeto aninhado), `partialPaymentAllowed` |
| `SPLIT` | 8 campos | 10 campos | `splits[]` calculado, `settlementMode` |

O `qrCode` não existe nas outras variantes, e é por isso que o cliente não consegue tratar as três respostas como um tipo só.

## Massa de teste dentro da documentação

A página `/docs` traz um painel que gera **CPF, CNPJ numérico e CNPJ alfanumérico** com dígitos verificadores válidos, com clique para copiar e alternância entre forma mascarada e crua. Não há endpoint para isso: o documento OpenAPI não tem scripting próprio, então o gerador roda no navegador e é injetado via `SwaggerIndexTransformer`, o ponto de extensão suportado do springdoc.

O CNPJ alfanumérico segue a regra de 2026: valor do caractere igual ao código ASCII menos 48 sobre doze posições alfanuméricas, módulo 11 com pesos ciclando de 2 a 9 da direita para a esquerda, resto menor que 2 produzindo zero. Sob essa regra, um CNPJ numérico produz os mesmos dígitos verificadores do algoritmo clássico — é assim que o formato mantém compatibilidade retroativa.

O script se autoverifica ao carregar contra três vetores publicados e exibe aviso no painel se algum falhar, para que um gerador quebrado não distribua documentos inválidos em silêncio.

> Os valores têm dígito verificador válido e passam pelas restrições de formato desta API. Não identificam ninguém e não estão registrados em nenhum órgão.

## Tratamento de erros

Toda falha responde `application/problem+json` no formato da **RFC 9457**, com um único schema `Problem` em todas as operações. Documentar formato de erro diferente por rota é a forma mais rápida de tornar um documento não confiável.

```json
{
  "type": "https://docs.best-documentation-api.dev/problems/validation-failed",
  "title": "Falha de validação",
  "status": 422,
  "detail": "O corpo da requisição violou 2 restrição(ões).",
  "instance": "/v1/boletos",
  "code": "VALIDATION_FAILED",
  "traceId": "01JC8Z5W9QK3MV2T7X4YB6NDHA",
  "timestamp": "2026-10-30T21:14:05.182-03:00",
  "violations": [
    { "pointer": "/pixKey", "constraint": "NotBlank", "message": "não pode estar em branco" }
  ]
}
```

Os cinco membros da RFC vêm primeiro; `code`, `traceId`, `timestamp` e `violations` são extensões, que a especificação permite. Violações são localizadas por **JSON Pointer (RFC 6901)**, índices de array incluídos, e **todas** são reportadas de uma vez — quem está corrigindo um formulário quer saber de todos os campos numa ida só.

Exceções carregam um código e os argumentos de interpolação, nunca uma frase pronta: só o handler sabe o idioma de quem chamou.

## Estrutura

```
com.clau.best.documentation.api
├── config      definição OpenAPI, i18n, propriedades tipadas, transformers
├── shared
│   ├── api     nomes de header que fazem parte do contrato
│   ├── contract tipos reutilizados por $ref (Money, TaxIdentifier)
│   ├── doc     meta-anotações de documentação
│   ├── error   modelo RFC 9457 e handler global
│   ├── i18n    idiomas suportados
│   └── openapi walker do documento e customizer de união discriminada
├── system      liveness e descoberta de idiomas
├── payment     meios de pagamento polimórficos
└── boleto      emissão com requisição e resposta polimórficas
```

## Convenções de contrato

- **`operationId` sempre explícito.** É o nome que geradores de client dão ao método; deixar o springdoc derivar do método Java significa que renomear um método privado quebra todo SDK gerado.
- **Tags declaradas à mão**, com `auto-tag-classes: false`. Tags derivadas de nome de classe publicam `order-controller` como agrupamento público, vazam estrutura interna e não são traduzíveis. Tag sem endpoint não é declarada.
- **Enums como componente** via `@Schema(enumAsRef = true)`. Inlinados, a lista de valores é duplicada em todo schema que os referencia, a descrição do enum é descartada, e geradores emitem um tipo anônimo por ocorrência.
- **Segredos com `@JsonProperty(access = WRITE_ONLY)`**, não `@Schema(writeOnly = true)`. O segundo apenas escreve `writeOnly` no documento enquanto o Jackson continua serializando o valor — o contrato prometeria que o número do cartão nunca volta enquanto o serviço o devolvia em toda resposta.
- **Nulabilidade por JSpecify `@Nullable`**, que o springdoc lê para emitir a união de tipos do 3.1 (`type: [boolean, "null"]`) em vez da keyword `nullable`, removida na versão.
- **Metadados de ambiente em configuração**, não em anotação. Anotação não é sobrescrevível por profile, e é assim que um documento acaba anunciando `localhost` em produção.

## Testes

```
./mvnw test
```

Três tipos de teste, que pegam falhas diferentes:

**Asserções sobre o documento** — versão, dialeto JSON Schema, aplicação das meta-anotações, enums como componente, formato da união em cada ponto de uso.

**Invariantes que falham o build:**
- nenhuma chave de mensagem não traduzida vaza para o documento, nos três idiomas;
- nenhuma referência `$ref` aponta para schema inexistente.

A primeira pegou uma lacuna real: `@ExampleObject` não passava pela resolução de chaves. A segunda pega a classe de defeito que produz um documento que lê corretamente e é inútil para qualquer gerador.

**Asserções de runtime sobre HTTP real**, não `MockMvc`, porque negociação de locale, `Content-Language` e o payload de erro vivem fora do handler. Essa metade se pagou: o documento gerado estava perfeito enquanto todo `POST` respondia `400`, porque o discriminator estava sendo repassado ao construtor da subclasse, que não o declara.

## Decisões de ferramenta

**Maven fixado em 3.9.16.** O wrapper vinha com 3.10.0, que o importador de Maven do IntelliJ não consegue executar: o `maven3-server` dele é compilado contra a linha 3.9 e falha ao instanciar os componentes Plexus legados, abortando com `No implementation for ArtifactHandlerManager was bound`. O import então parava na etapa rasa de preimport e produzia um módulo sem nenhuma dependência, com `LANGUAGE_LEVEL` em `JDK_1_5` e `${project.build.directory}` sem interpolar — todo import quebrado na IDE enquanto a linha de comando compilava limpo.

**Sem comentários no código.** O código se explica por nomes; o contexto que vale registrar vai na mensagem de commit ou na descrição do PR. A documentação da API vive nas chaves `doc.*` e nos bundles de i18n, que são dado de contrato, não comentário.

## Licença

O contrato da API declara **Apache-2.0** em `api.license`, que é o valor publicado no campo `info.license` do documento OpenAPI. O repositório ainda não tem arquivo `LICENSE` — sem ele, essa declaração vale para o contrato, não como concessão de licença do código.

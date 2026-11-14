# Participa

Aplicação web para participação anônima dos estudantes durante a aula (dúvidas, apoios e termômetro de compreensão). Ver `docs/plano-de-desenvolvimento.md` para o plano completo.

## Versões usadas

- Java 21
- Spring Boot 4.1.2 (versão padrão oferecida pelo start.spring.io em 13/11/2026)
- Maven (via wrapper, `./mvnw`)
- PostgreSQL

## Como preparar o ambiente local

1. Instalar o PostgreSQL e criar dois bancos:

   ```sql
   CREATE DATABASE participa;
   CREATE DATABASE participa_test;
   ```

2. Copiar os dois arquivos de exemplo de configuração e preencher a senha do seu PostgreSQL em cada um:

   ```bash
   cp src/main/resources/application-local.properties.exemplo src/main/resources/application-local.properties
   cp src/main/resources/application-test.properties.exemplo src/main/resources/application-test.properties
   ```

   Esses dois arquivos (`application-local.properties` e `application-test.properties`) não vão para o Git — estão no `.gitignore`, porque guardam a senha do seu banco local.

## Como rodar

```bash
./mvnw spring-boot:run
```

Abre em `http://localhost:8080` e mostra a página inicial (`index.html`).

## Como testar

```bash
./mvnw test
```

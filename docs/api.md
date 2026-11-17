# Contrato da API — Participa

**Status: RASCUNHO até os outros dois integrantes lerem e aprovarem** (critério de pronto da linha 2). Depois de aprovado, fica congelado (combinado 1 da seção 5): mudanças só por PR que altere este arquivo, com aviso aos outros dois.

Convenções: corpo e resposta em JSON; datas e horas em ISO 8601 com fuso (`2026-11-12T19:12:00-03:00`); erros sempre `{"erro": "mensagem para o usuário"}`; escrita exige o cabeçalho `X-XSRF-TOKEN`; sem aceite do termo, toda rota `/api/**` (exceto `/api/auth/**`, `/api/eu` e `/api/eu/aceite-termo`) devolve 403 `{"erro": "aceite-pendente"}`.

## 1. DTOs

| DTO | Campos |
|---|---|
| UsuarioDTO | id, nome, email, perfil (ESTUDANTE/PROFESSOR/MONITOR), aceiteTermo, orientacaoVista |
| TurmaDTO | id, nome, codigo, fimSemestre, estudantes (só professor) |
| AulaDTO | id, turmaId, numero, titulo, data, situacao (ABERTA/POS_AULA/ENCERRADA), prazoPosAula, resumoEnviadoEm |
| DuvidaDTO | id, texto, anonima, autorNome (nulo se anônima, para qualquer perfil), minha, apoios, apoiadaPorMim, respondida, temaId, posAula, status (PUBLICADA/PENDENTE/OCULTA), termoRetido (só moderação), criadaEm |
| TermometroDTO | id, numero, abertoEm, duracaoSegundos, aberto, minhaResposta (só estudante), resultado (só professor/monitor) |
| EstadoAulaDTO | aula, duvidas, termometro, servidorAgora |

Visibilidade de DuvidaDTO: estudante recebe PUBLICADA e as próprias PENDENTE; professor e monitor recebem PUBLICADA na rota de estado e as demais nas rotas de moderação; OCULTA só na moderação.

### Exemplos

```json
// UsuarioDTO
{"id":3,"nome":"Marina Souza","email":"marina@alunos.utfpr.edu.br","perfil":"ESTUDANTE","aceiteTermo":true,"orientacaoVista":false}

// TurmaDTO
{"id":1,"nome":"Estruturas de Dados · ES41","codigo":"K7M2QX","fimSemestre":"2026-12-18","estudantes":3}

// AulaDTO
{"id":12,"turmaId":1,"numero":12,"titulo":"Árvores AVL","data":"2026-11-12","situacao":"ABERTA","prazoPosAula":null,"resumoEnviadoEm":null}

// DuvidaDTO (anônima, de outra pessoa)
{"id":41,"texto":"Não entendi a rotação dupla.","anonima":true,"autorNome":null,"minha":false,"apoios":7,"apoiadaPorMim":false,"respondida":false,"temaId":null,"posAula":false,"status":"PUBLICADA","termoRetido":null,"criadaEm":"2026-11-12T19:12:00-03:00"}

// TermometroDTO (visão do professor)
{"id":5,"numero":2,"abertoEm":"2026-11-12T19:30:00-03:00","duracaoSegundos":60,"aberto":true,"minhaResposta":null,
 "resultado":{"entendi":12,"parcial":6,"naoEntendi":3,"respostas":21,"matriculados":30}}

// TermometroDTO (visão do estudante)
{"id":5,"numero":2,"abertoEm":"2026-11-12T19:30:00-03:00","duracaoSegundos":60,"aberto":true,"minhaResposta":"PARCIAL","resultado":null}

// EstadoAulaDTO
{"aula":{"id":12,"turmaId":1,"numero":12,"titulo":"Árvores AVL","data":"2026-11-12","situacao":"ABERTA","prazoPosAula":null,"resumoEnviadoEm":null},
 "duvidas":[{"id":41,"texto":"Não entendi a rotação dupla.","anonima":true,"autorNome":null,"minha":false,"apoios":7,"apoiadaPorMim":false,"respondida":false,"temaId":null,"posAula":false,"status":"PUBLICADA","termoRetido":null,"criadaEm":"2026-11-12T19:12:00-03:00"}],
 "termometro":null,
 "servidorAgora":"2026-11-12T19:31:07-03:00"}

// Participação (professor) — só totais da aula
{"duvidas":14,"apoios":38,"respostasTermometro":21}

// Histórico (estudante, uma turma)
{"duvidasEnviadas":2,"apoiosDados":5,"termometrosRespondidos":3,
 "duvidas":[{"texto":"Como balancear depois de remover?","anonima":true,"aulaNumero":12,"criadaEm":"2026-11-12T19:12:00-03:00","apoios":4}]}

// Painel por tema (ver nota sobre ItemPainelDTO abaixo)
[{"temaId":1,"nome":"Rotações","duvidas":2,"apoios":5,"itens":[{"id":41,"texto":"Não entendi a rotação dupla.","anonima":true,"autorNome":null,"apoios":3,"respondida":false,"posAula":false,"temaId":1,"criadaEm":"2026-11-12T19:12:00-03:00"}]},
 {"temaId":null,"nome":"Sem tema","duvidas":0,"apoios":0,"itens":[]}]

// Resumo
{"tipo":"FINAL","prazo":"2026-11-14T23:59:00-03:00","geradoEm":"2026-11-15T00:00:12-03:00",
 "totais":{"duvidas":14,"apoios":38,"posAula":3},"temas":["(mesmo formato do painel)"],"termometros":["(lista de TermometroDTO com resultado)"]}
```

## 2. Rotas

| Método | Rota | Perfil | Corpo | Resposta | Branch |
|---|---|---|---|---|---|
| POST | /api/auth/cadastro | livre | {nome,email,senha,perfil} | 201 UsuarioDTO · 409 e-mail usado · 400 domínio inválido | 4 |
| POST | /api/auth/login | livre | {email,senha} | 200 UsuarioDTO + cookie · 401 | 4 |
| POST | /api/auth/sair | logado | — | 204 | 4 |
| GET | /api/eu | logado | — | UsuarioDTO | 4 |
| GET | /api/eu/turmas | logado | — | [TurmaDTO] | 4 |
| GET | /api/eu/aula-atual | logado | — | AulaDTO · 204 | 4 |
| POST | /api/turmas/entrar | ESTUDANTE | {codigo} | TurmaDTO · 404 "Código não encontrado. Confira com o professor." | 4 |
| GET | /api/professor/turmas | PROFESSOR | — | [TurmaDTO] | 4 |
| POST | /api/professor/turmas | PROFESSOR | {nome,fimSemestre} | 201 TurmaDTO | 4 |
| GET | /api/professor/turmas/{id}/aulas | PROFESSOR | — | [AulaDTO] | 4 |
| POST | /api/professor/turmas/{id}/aulas | PROFESSOR | {titulo} | 201 AulaDTO · 409 já há aula aberta | 4 |
| PATCH | /api/professor/aulas/{id}/encerrar | PROFESSOR | {opcao:"24"\|"48"\|"proxima"} | AulaDTO (POS_AULA) | 4 |
| POST | /api/eu/aceite-termo | logado | — | 204 | 8 |
| POST | /api/eu/orientacao-vista | logado | — | 204 | 13 |
| GET | /api/aulas/{id}/estado | matriculado ou dono | — | EstadoAulaDTO + ETag · 304 | 7 |
| POST | /api/aulas/{id}/duvidas | ESTUDANTE | {texto,anonima} | 201 DuvidaDTO · 400 · 409 envio encerrado | 7 |
| POST | /api/duvidas/{id}/apoio | ESTUDANTE | — | {apoios,apoiadaPorMim} · 409 própria dúvida | 9 |
| DELETE | /api/duvidas/{id}/apoio | ESTUDANTE | — | {apoios,apoiadaPorMim} | 9 |
| POST | /api/professor/aulas/{id}/termometros | PROFESSOR | {duracaoSegundos} (15–600) | 201 TermometroDTO · 409 já há aberto | 6 |
| PATCH | /api/professor/termometros/{id}/encerrar | PROFESSOR | — | TermometroDTO | 6 |
| POST | /api/termometros/{id}/respostas | ESTUDANTE | {opcao} (ENTENDI/PARCIAL/NAO_ENTENDI) | 204 · 409 já respondeu · 409 encerrado | 6 |
| GET | /api/professor/aulas/{id}/termometros | PROFESSOR | — | [TermometroDTO com resultado] | 6 |
| GET | /api/professor/aulas/{id}/participacao | PROFESSOR | — | {duvidas,apoios,respostasTermometro} | 12 |
| GET | /api/eu/turmas/{id}/historico | logado | — | {duvidasEnviadas,apoiosDados,termometrosRespondidos,duvidas:[{texto,anonima,aulaNumero,criadaEm,apoios}]} | 12 |
| DELETE | /api/eu/turmas/{id}/contribuicoes | logado | — | {duvidasApagadas} | 8 |
| PATCH | /api/professor/duvidas/{id}/respondida | PROFESSOR | {respondida} | DuvidaDTO | 17 |
| GET/POST | /api/professor/aulas/{id}/temas | PROFESSOR | {nome} | [{id,nome}] / 201 {id,nome} | 19 |
| PATCH | /api/professor/duvidas/{id}/tema | PROFESSOR | {temaId\|null} | DuvidaDTO | 19 |
| GET | /api/professor/aulas/{id}/painel | PROFESSOR | — | [{temaId,nome,duvidas,apoios,itens:[DuvidaDTO]}], "Sem tema" por último | 19 |
| GET | /api/professor/aulas/{id}/resumo | PROFESSOR | — | {tipo:PREVIA\|FINAL,prazo,geradoEm,totais:{duvidas,apoios,posAula},temas,termometros} | 22 |
| GET | /api/moderacao/aulas/{id}/pendentes | PROFESSOR/MONITOR | — | [DuvidaDTO com termoRetido] | 18 |
| GET | /api/moderacao/aulas/{id}/publicadas | PROFESSOR/MONITOR | — | {duvidas,ocultadas} | 18 |
| PATCH | /api/moderacao/duvidas/{id}/publicar | PROFESSOR/MONITOR | — | DuvidaDTO | 18 |
| PATCH | /api/moderacao/duvidas/{id}/ocultar | PROFESSOR/MONITOR | — | DuvidaDTO | 18 |
| GET/POST | /api/moderacao/termos | PROFESSOR/MONITOR | {termo} | [{id,termo}] / 201 | 18 |
| DELETE | /api/moderacao/termos/{id} | PROFESSOR/MONITOR | — | 204 | 18 |
| POST | /api/professor/turmas/{id}/monitores | PROFESSOR | {email} | 201 · 404 usuário não encontrado | 18 |
| DELETE | /api/professor/turmas/{id}/monitores/{usuarioId} | PROFESSOR | — | 204 | 18 |

## 3. Rota de estado da aula

`GET /api/aulas/{id}/estado` é consultada a cada 3 s. O `ShallowEtagHeaderFilter` (só em `/api/aulas/*/estado`) responde 304 sem corpo quando nada mudou. O cliente calcula a contagem regressiva do termômetro com `servidorAgora`.

## 4. Notas de implementação (trilha B)

- **ItemPainelDTO**: o painel, o resumo e `PATCH /api/professor/duvidas/{id}/tema` devolvem um subconjunto do DuvidaDTO (`id, texto, anonima, autorNome, apoios, respondida, posAula, temaId, criadaEm`). `autorNome` é nulo quando `anonima` é verdadeiro. Se a branch 7 já tiver o `DuvidaMapper`, trocar por ele e devolver DuvidaDTO inteiro.
- `POST /api/professor/aulas/{id}/termometros` aceita corpo vazio (duração padrão de 60 s).
- Aula: o `resumoEnviadoEm` do AulaDTO vem de `resumo_aula.email_enviado_em` (a branch 4 precisa fazer o join quando a V9 existir, ou a branch 22 ajusta o mapeamento).

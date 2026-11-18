# Handoff: Participa (sistema de dúvidas e participação em aula)

## Visão geral
Aplicação web para estudantes mandarem dúvidas durante a aula (anônimas por padrão), apoiarem as dúvidas dos colegas e responderem a um termômetro de compreensão. O professor acompanha tudo ao vivo, organiza as dúvidas por tema, modera e recebe um resumo depois da aula. São três perfis: **ESTUDANTE**, **PROFESSOR** e **MONITOR**.

Este documento acompanha o `Plano_de_Desenvolvimento.md` (branches, rotas, modelo de dados). Para cada tela, ele diz **como ela deve ficar e como deve se comportar**. Quando houver conflito de regra de negócio, vale o plano. Quando houver conflito de visual ou de texto, vale este documento.

## Sobre os arquivos de design
Os arquivos em `prototipo/` são **referências de design feitas em HTML**: protótipos que mostram o visual e o comportamento esperados. Não são código de produção. A tarefa é **recriar essas telas no ambiente do projeto**. A stack proposta no plano é Spring Boot servindo HTML, CSS e JavaScript sem framework. Os dados do protótipo são simulados no próprio navegador e precisam ser trocados pelas rotas `/api/...` do plano.

O arquivo `prototipo/_ds/.../styles.css` **pode ser usado direto** em `src/main/resources/static/css/`, porque é CSS puro, com tokens e classes, sem build. Recomendo usá-lo como a folha de estilo base do projeto.

Para abrir o protótipo, sirva a pasta `prototipo/` por HTTP (por exemplo, com `npx serve prototipo` ou `python -m http.server`) e abra `Participa.dc.html`. No topo da tela dá para trocar o perfil (Estudante, Professor, Monitor) e simular a queda de conexão.

## Fidelidade
**Alta fidelidade.** Cores, tipografia, espaçamento, textos e interações são os finais. Recrie fiel ao protótipo usando as classes do `styles.css`.

---

## Design tokens (Nocturne)
Todos estão em `:root` no `styles.css`. Não use hex soltos no código: sempre `var(--...)`.

**Cores**
| Token | Valor | Uso |
|---|---|---|
| `--color-bg` | `#161826` | Fundo da página |
| `--color-surface` | `#232532` | Cartões, campos, diálogos |
| `--color-text` | `#e9e9ed` | Texto principal |
| `--color-accent` | `#9184d9` | Contorno de botão primário, ícones, foco |
| `--color-accent-300` | `#d2cefd` | Texto em destaque (contador, prazo, mensagens). O accent puro não passa AA em texto pequeno. |
| `--color-accent-600` | `#796cbf` | Barra “Entendi em parte” |
| `--color-accent-800` | `#423a6a` | Fundo de `.tag-accent` |
| `--color-accent-900` | `#2b2741` | Fundo do aviso de anonimato e do gradiente do topo |
| `--color-accent-100` | `#f5f4ff` | Texto sobre accent-900 |
| `--color-neutral-300/400/500` | `#cfd3e5` / `#b2b6ca` / `#9397ab` | Texto secundário, metadados, barra “Não entendi” |
| `--color-neutral-800/900` | `#3f424d` / `#292b31` | Fundo de `.tag-neutral`, trilho das barras |
| `--color-divider` | `#e9e9ed` a 16% | Bordas de campos e botões secundários, linhas |
| Texto atenuado (`.text-muted`) | `--color-text` a 55% | Descrições |

**Espaçamento** (densidade 0,7×): `--space-1` 2.8px · `--space-2` 5.6px · `--space-3` 8.4px · `--space-4` 11.2px · `--space-6` 16.8px · `--space-8` 22.4px

**Raios:** `--radius-sm` 4px · `--radius-md` 8px · `--radius-lg` 14px (diálogo)

**Sombras (elevação):**
- `--shadow-sm`: `0 0 0 1px #3f424d`, usada em cartões comuns e caixas de aviso
- `--shadow-md`: `0 0 0 1px #595d6c, 0 6px 18px rgba(0,0,0,.55)`, usada no cartão do termômetro e nos toasts
- `--shadow-lg`: `0 0 0 1px #9397ab, 0 16px 40px rgba(0,0,0,.65)`, usada em diálogos

**Tipografia:** Inter (Google Fonts, pesos 400, 500 e 600) para tudo. Os títulos usam peso **500** e nunca passam disso.
| Elemento | Tamanho | Observação |
|---|---|---|
| h2 | 32px | Título de telas de entrada (login, termo, orientação) |
| h3 | 25px | Título de tela interna |
| h4 | 20px | Título de seção |
| h6 | 13px | Sobretítulo em CAIXA ALTA, espaçamento entre letras de 0.08em |
| corpo | 15px, altura de linha 1.55 | |
| botão e campo | 14px | |
| metadado | 12px | |
| tag | 11px | |
| `.card-title` | 17px, peso 500 | |
Letter-spacing dos títulos: -0.015em. Números que mudam (contadores, tempo, código) usam `font-variant-numeric: tabular-nums`.

**Ícones:** Phosphor Icons (`@phosphor-icons/web@2.1.1`, classes `ph ph-*`). Usados: chats-circle, chalkboard-teacher, clock-counter-clockwise, key, question, sign-out, users-three, broadcast, kanban, shield-check, file-text, paper-plane-tilt, mask-happy, identification-badge, hand-heart (com `ph-fill` quando apoiada), thermometer-simple, check-circle, circle-half, x-circle, wifi-slash, lock-simple, database, chart-bar, trash, eye, eye-slash, check, x, plus, play, projector-screen, printer, envelope-simple, hourglass-medium, arrow-right.

**Assinaturas visuais do Nocturne (manter):**
- O botão primário é **só contorno** (borda accent de 1px, fundo transparente). Nunca preencha o botão.
- As linhas divisórias entre itens de lista somem nas pontas: `linear-gradient(to right, transparent, var(--color-divider) 48px, var(--color-divider) calc(100% - 48px), transparent) no-repeat bottom / 100% 1px`.
- O fundo da página tem um brilho suave no canto superior esquerdo: `radial-gradient(1200px 500px at 0% -10%, var(--color-accent-900), transparent 70%), var(--color-bg)`.
- Layout alinhado à esquerda e assimétrico, com espaço livre à direita.
- Foco pelo teclado: `outline: 2px solid var(--color-accent); outline-offset: 2px` (já no CSS).
- Elemento desabilitado: opacidade de 45% (já no CSS).

## Classes do styles.css usadas
`.btn` + `.btn-primary`, `.btn-secondary`, `.btn-ghost` e `.btn-block` · `.tag` + `.tag-accent`, `.tag-neutral` e `.tag-outline` · `.field > label` · `.input` (também em `textarea` e `select`) · `.seg` + `.seg-opt` (segmentado sobre `input[type=radio]` nativo) · `.card` + `.card-kicker`, `.card-title`, `.card-meta` e `.elev-sm/md` · `.table` · `.dialog-backdrop` + `.dialog`, `.dialog-title`, `.dialog-body` e `.dialog-actions` · `.text-muted`

---

## Estrutura geral (shell)
- **Cabeçalho** (`padding: 11.2px 16.8px`, flex, com quebra de linha): à esquerda, uma marca (quadrado de 28px com raio de 8px, contorno interno accent de 1.5px e o ícone `chats-circle`), o texto “Participa” em 17px e peso 500, e uma `.tag-neutral` com a turma atual. À direita, o menu do usuário. O seletor de perfil e o botão Online/Offline do protótipo são **só para demonstração**: não implemente.
- **Corpo:** flex em linha com `gap: 16.8px` e `padding: 0 16.8px 22.4px`.
  - **Menu lateral** com 210px de largura, `position: sticky`, botões em coluna (ícone de 17px e rótulo de 14px, `padding: 8px 10px`). O item atual leva `aria-current="page"`, cor `--color-accent-300` e fundo com o accent a 12%. Itens com contagem (Moderação) mostram uma `.tag-accent` com o número.
  - **Conteúdo** com `max-width: 1040px`.
- **Celular (até cerca de 720px):** o corpo vira coluna e o menu vira uma linha horizontal com rolagem no topo. Áreas de toque com **no mínimo 44px** de altura (os botões principais já têm `min-height: 44px`).
- **Menu por perfil:**
  - Estudante: Aula ao vivo · Meu histórico · Entrar em turma · Ajuda · Sair
  - Professor: Turmas e aulas · Aula ao vivo · Painel por tema · Moderação · Resumo
  - Monitor: Moderação
- **Toast:** fixo no canto inferior esquerdo (24px), com `max-width: 440px`, `role="status"`, fundo surface, `--shadow-md` e ícone `check-circle` accent. Some depois de cerca de 4s.

---

## Telas: Estudante

### 1. Login e cadastro (base-acesso)
- Coluna de `max-width: 400px` com `padding-top: 22.4px` e `gap: 16.8px`. **Sem menu lateral** nesta tela nem nas telas 2 e 3.
- h6 accent “Base de acesso”. h2 “Participe da aula sem se expor.” Abaixo, o texto em `.text-muted`: “Funciona no navegador do celular ou do computador. Nada para instalar.”
- `.seg` com “Entrar” e “Criar conta”.
- Campos: Nome completo (só no cadastro), E-mail institucional, Senha. No cadastro, Perfil (`.seg` com Estudante e Professor). O perfil MONITOR é atribuído pelo professor e não aparece no cadastro.
- `.btn-primary.btn-block` com `min-height: 44px`: “Entrar” ou “Criar conta”.
- Depois de entrar: se `aceite_termo_em` for nulo, vai para o Termo. Se `orientacao_vista_em` for nulo, vai para a Orientação. Senão, vai para Entrar na turma ou para a última aula.

### 2. Termo de uso e privacidade (US13)
- Coluna de `max-width: 620px`. h6 “Primeiro acesso · 1 de 2”. h2 “Termo de uso e privacidade”.
- Quatro blocos em grid `28px 1fr`, cada um com um ícone accent de 20px, um título com peso 500 e um texto muted de 14px:
  1. **O que guardamos.** Nome, e-mail institucional, suas dúvidas, apoios e respostas ao termômetro.
  2. **Como funciona o anonimato.** Em dúvida anônima, nenhuma tela mostra seu nome, e-mail ou RA, nem ao professor. O autor fica registrado no banco de dados para você ver seu histórico, não apoiar duas vezes e excluir suas contribuições. Quem administra o banco consegue vê-lo.
  3. **Sem ranques.** O professor vê apenas números da turma inteira. Ninguém vê quantas vezes você participou.
  4. **Por quanto tempo.** No fim do semestre ({fim_semestre}), dúvidas, apoios e respostas são apagados e ficam só os totais. Você pode excluir suas contribuições antes, em Meu histórico.
- Caixa de seleção “Li e entendi o termo” (18px, `accent-color` accent). O botão “Aceito” fica **desabilitado** até a caixa ser marcada. Ao aceitar, grava `aceite_termo_em`.

### 3. Orientação, primeiro acesso em 3 passos (US06)
- h6 “Como funciona · N de 3” e, à direita, 3 traços de progresso de 22×3px (accent quando já percorridos, `--color-neutral-800` quando não).
- Cada passo tem um ícone de 36px, um h2 e um texto muted de 16px:
  1. `paper-plane-tilt`, “Mande sua dúvida na hora em que ela surgir.” Texto: “Escreva até 500 caracteres. Ela aparece na lista da aula em até 3 segundos. Se um colega já perguntou o que você queria, toque em Apoiar.”
  2. `mask-happy`, “Anônima por padrão.” Texto: “A opção ‘Enviar como anônimo’ já vem marcada. Acima do botão de envio, um aviso diz sempre como a dúvida vai sair: anônima ou com seu nome. Você pode mudar até enviar.”
  3. `eye`, “Quem vê o quê.” Em seguida, uma `.table`:
     | Informação | Quem vê |
     |---|---|
     | Seu nome numa dúvida anônima | Ninguém |
     | Seu nome numa dúvida identificada | Turma e professor |
     | Quem apoiou uma dúvida | Ninguém, só o total |
     | Sua resposta ao termômetro | Ninguém, professor vê percentuais |
     | Seu histórico de participação | Só você |
- Botões: primário “Próximo” (no último passo, “Começar”), secundário “Voltar” (a partir do passo 2) e ghost “Pular”, alinhado à direita.
- Ao concluir ou pular, grava `orientacao_vista_em`. Pelo item **Ajuda** do menu, a orientação reabre a qualquer momento. Nesse caso o botão final vira “Voltar para a aula”.

### 4. Entrar na turma (base-acesso, US15)
- Coluna de `max-width: 440px`. h6 “Entrar na turma”. h2 “Digite o código que o professor mostrou.” Abaixo, o texto: “São 6 letras e números. Também dá para abrir o endereço da tela do professor, como participa.app/entrar?codigo=K7M2QX.”
- Campo “Código da turma”: 28px, `letter-spacing: 0.3em`, caixa alta, `min-height: 56px`, `maxlength=6` e `autocomplete=off`. A mensagem de erro fica abaixo, em 13px, na cor accent-300, com `aria-live="polite"`.
  - Menos de 6 caracteres: “O código tem 6 caracteres.”
  - Código inexistente: “Código não encontrado. Confira com o professor.”
- Botão primário em bloco “Entrar na aula”, que chama `POST /api/turmas/entrar`. Se a URL tiver `?codigo=XXXXXX`, o campo já vem preenchido.

### 5. Aula ao vivo do estudante (US01, US03, US04, US05, US07, US09, US16)
Coluna com `gap: 16.8px`, nesta ordem:

**a) Cabeçalho:** h6 muted “Aula 12 · 04/11”, h3 com o título da aula e, à direita, `.tag-outline` com a situação: “Aula aberta”, “Pós-aula até {prazo}” ou “Encerrada”.

**b) Aviso de sem conexão (US16)**, só quando offline: uma faixa com `--shadow-sm`, raio de 8px, `padding: 8.4px 11.2px`, o ícone `wifi-slash` accent e o texto “Sem conexão. Dúvidas enviadas ficam salvas neste aparelho e seguem quando a conexão voltar.”, com `role="status"`.

**c) Cartão do termômetro (US04)**, visível enquanto houver um termômetro aberto: `.card.elev-md` com fundo `linear-gradient(120deg, var(--color-accent-900), var(--color-surface) 60%)`.
- Linha com o ícone `thermometer-simple`, o título “O professor quer saber: você está acompanhando?” e, à direita, a contagem regressiva `m:ss` em 20px, na cor accent-300.
- Três botões secundários em grid `repeat(auto-fit, minmax(130px, 1fr))`, com `min-height: 48px`: “Entendi” (`check-circle`), “Entendi em parte” (`circle-half`) e “Não entendi” (`x-circle`). O texto do rótulo sempre aparece, então a informação nunca depende só da cor.
- Depois de responder, os botões dão lugar a: “Resposta registrada: **{opção}**. O professor vê só os percentuais da turma.” (`role="status"`). Uma resposta por estudante. Depois do prazo, o servidor recusa.

**d) Cartão de envio (US01, US07, US05)**: `.card.elev-sm`.
- Rótulo “Sua dúvida” (`.card-title`, como `<label for>`).
- Se houver prazo pós-aula aberto, aparece antes do campo, em accent-300: “A aula terminou. Você pode enviar dúvidas até {prazo}.”
- `textarea.input`: `min-height: 96px`, 15px, `maxlength=500` e placeholder “Escreva aqui. Ex.: por que a rotação dupla é necessária?”.
- Linha com a caixa “Enviar como anônimo”, **marcada por padrão**, com área de toque de 44px. À direita, o contador “N / 500” em 12px com `tabular-nums`, ligado ao campo por `aria-describedby`.
- **Aviso de anonimato (US07), sempre visível acima do botão:** caixa com fundo `--color-accent-900`, texto `--color-accent-100`, raio de 8px, `padding: 8.4px 11.2px`, ícone de 20px e `aria-live="polite"`.
  - Anônima: ícone `mask-happy`. “Sua dúvida será enviada como **ANÔNIMA**. Ninguém verá seu nome, nem o professor.”
  - Identificada: ícone `identification-badge`. “Sua dúvida será enviada como **IDENTIFICADA**. A turma e o professor verão seu nome: {nome}.”
  - A palavra ANÔNIMA ou IDENTIFICADA vai em peso 600, com espaçamento entre letras de 0.04em. A mudança é dita por texto e ícone, nunca só por cor.
- Botão primário “Enviar dúvida” (`paper-plane-tilt`), desabilitado se o texto estiver vazio. Depois de enviar, limpa o campo e **volta a caixa de anônimo para marcada**.
- Toast depois do envio:
  - Publicada: “Enviada. Sua dúvida já está na lista da aula.”
  - Retida pelo filtro (US14): “Recebida. Ela passa pela moderação antes de aparecer para a turma.”
  - Offline (US16): “Sem conexão. A dúvida ficou salva no aparelho e segue quando a conexão voltar.” Guarde a dúvida em `localStorage` e reenvie quando o evento `online` disparar. Ao reconectar, o toast diz “N dúvida(s) enviada(s) ao reconectar.”
- Aula encerrada e fora do prazo: o cartão mostra só o ícone `lock-simple` e “O envio de dúvidas desta aula terminou em {prazo}.”

**e) Lista “Dúvidas da aula” (US03, US09)**
- Título h4 e, à direita, em 12px muted: “Mais apoiadas primeiro · respondidas no fim · atualiza a cada 3 s”.
- **Ordem:** primeiro as não respondidas, depois as respondidas. Dentro de cada grupo, mais apoios primeiro, e no empate, as mais antigas primeiro.
- **Item:** flex com `gap: 11.2px`, `padding: 11.2px 0` e linha inferior que some nas pontas.
  - Texto em 15px com `text-wrap: pretty`.
  - Linha de metadados em 12px na cor neutral-400:
    - Autor: “Anônima”, ou o nome se for identificada. Nas dúvidas do próprio estudante, vai uma `.tag-accent` “Enviada como anônima” ou “Enviada com seu nome”.
    - “· HH:MM”.
    - Tags conforme o caso: `.tag-neutral` “Respondida”, `.tag-neutral` “Pós-aula”, `.tag-outline` “Aguardando conexão” (na fila offline) e `.tag-outline` “Em revisão pela moderação” (só o autor vê a própria dúvida pendente).
  - Dúvida respondida aparece com opacidade de 60%.
  - **Botão de apoio** à direita, com `min-width: 84px`, `min-height: 44px` e o ícone `hand-heart` seguido do total:
    - Sem apoio: `.btn-secondary` com `aria-pressed="false"`.
    - Apoiada: `.btn-primary` com fundo accent a 14%, ícone `ph-fill` e `aria-pressed="true"`.
    - `aria-label`: “Apoiar esta dúvida. N apoios” ou “Desfazer apoio. N apoios”.
    - Desabilitado na própria dúvida e nas que estão na fila offline.
    - Clicar de novo desfaz o apoio (`POST` ou `DELETE /api/duvidas/{id}/apoio`). Nunca mostra quem apoiou.
- A lista tem `aria-live="polite"`. A atualização é por polling a cada 3s (US16): use `If-None-Match`/ETag ou `?desde=` para trazer só o que mudou, e **pause quando a aba estiver oculta** (`document.visibilityState`).

### 6. Meu histórico (US02, US13)
- `max-width: 720px`. h6 muted “Só você vê esta tela”. h3 “Meu histórico”.
- Três cartões `.card.elev-sm` em grid `auto-fit, minmax(150px, 1fr)`, cada um com `.card-kicker` e o número em 32px e peso 500: “Dúvidas enviadas”, “Apoios dados”, “Termômetros respondidos”.
- Lista das próprias dúvidas: o texto, uma `.tag-accent` com o modo de envio e “Aula N · HH:MM · N apoios”. Sem nenhuma: “Nenhuma contribuição registrada.”
- Caixa com `--shadow-sm`: título “Excluir minhas contribuições”, o texto “Apaga suas dúvidas, apoios e respostas ao termômetro desta turma. Os totais da turma diminuem junto. Não dá para desfazer.” e um botão secundário com `trash`.
  - O botão abre um `.dialog` com `role="alertdialog"`: título “Excluir suas contribuições?”, o texto “Serão apagadas N dúvidas suas. Os apoios e respostas também são removidos. Não dá para desfazer.” e os botões Cancelar (secundário) e Excluir (primário).
  - Depois de confirmar, o toast diz “Suas contribuições foram excluídas.”

---

## Telas: Professor

### 7. Turmas e aulas (base-acesso)
- h6 “Professor”, h3 “Turmas e aulas” e, à direita, o botão primário “Criar turma” (`plus`).
- Cartões de turma em grid `auto-fit, minmax(260px, 1fr)`:
  - `.card-kicker` “N estudantes · até DD/MM” e `.card-title` com o nome.
  - “Código” em 11px muted e, ao lado, o código em 26px com `letter-spacing: 0.18em`, `tabular-nums` e cor accent-300.
  - `.card-meta` com a URL `participa.app/entrar?codigo=XXXXXX`.
- h4 “Aulas · {turma}” e uma `.table` com as colunas Aula, Data, Situação (tag) e Ação (alinhada à direita). As ações mudam conforme a situação:
  - ABERTA: “Encerrar aula”.
  - Pós-aula: nada (o protótipo mostra “Simular fim do prazo” só para demonstração).
  - ENCERRADA: “Ver resumo”.
  - Aulas antigas: “Resumo enviado em DD/MM”.
- Botão ghost “Abrir nova aula” (`play`), desabilitado enquanto houver uma aula aberta.
- **Diálogo “Criar turma”:** campos Nome da turma e Fim do semestre (`type=date`), mais a nota “Um código de 6 caracteres é gerado para os estudantes entrarem. No fim do semestre, os dados individuais são apagados e ficam só os totais.” O código usa o alfabeto **sem caracteres ambíguos**: `ABCDEFGHJKLMNPQRSTUVWXYZ23456789`.
- **Diálogo “Encerrar a Aula N” (US05):**
  - Texto: “Os estudantes ainda podem enviar dúvidas até o prazo abaixo. Elas aparecem marcadas como pós-aula. Quando o prazo terminar, o resumo é gerado e você recebe o aviso no e-mail institucional.”
  - `.seg` “Prazo para dúvidas pós-aula” com 24 horas, 48 horas (padrão) e Até a próxima aula. Abaixo, em accent-300, “Até {data às hora}”.
  - Depois de encerrar, o toast diz “Aula encerrada. Dúvidas pós-aula abertas até {prazo}.”

### 8. Aula ao vivo do professor (US04, US08, US09, US02)
- Cabeçalho: h6 “Aula 12 · 04/11 · código K7M2QX”, h3 com o título, a tag de situação e o botão “Encerrar aula”.
- Dois cartões em grid `auto-fit, minmax(min(100%, 320px), 1fr)`:
  - **Termômetro (US04, US08):**
    - Antes do primeiro termômetro, o texto muted: “Pergunte à turma, em um toque, se o conteúdo está claro. As respostas são anônimas e o resultado sai em percentuais.”
    - Campo “Duração (s)” (number, de 15 a 600, passo 15, padrão 60) e o botão primário “Abrir termômetro” (`play`), desabilitado fora de aula aberta.
    - Aberto: a contagem regressiva e o botão “Encerrar agora”.
    - **Resultado:** três barras horizontais com o **rótulo em texto e o percentual** sempre visíveis. Linha de rótulo em 14px com o percentual em peso 500. Trilho de 12px de altura, raio de 6px e cor neutral-900. Preenchimento com `transition: width .4s`, nas cores Entendi = accent-300, Entendi em parte = accent-600 e Não entendi = neutral-500.
    - `.card-meta`: “N de M estudantes responderam”, com “· encerrado” quando terminar.
    - Botão ghost “Modo projeção” (`projector-screen`).
  - **Participação da turma (US02):** três números em 30px e peso 500 (dúvidas, apoios, respostas ao termômetro) e o `.card-meta` com `lock-simple`: “Números da turma inteira. Nenhum dado por estudante.” **Não pode haver nenhuma lista ou contagem por estudante.**
- Lista “Dúvidas · N” com o botão ghost “Organizar por tema →”. Item: à esquerda, uma coluna de 44px com o total de apoios (20px, peso 500) e “apoios” (10px). No centro, o texto e os metadados: autor (“Anônima” ou o nome), hora, `.tag-neutral` do tema e `.tag-outline` “Pós-aula”. À direita:
  - Não respondida: botão secundário “Marcar respondida”.
  - Respondida: ghost “✓ Respondida”, que desfaz ao clicar.
  - A ordem é a mesma da tela do estudante.

### 9. Modo projeção (US08)
- Sobreposição fixa em tela cheia (`inset: 0`) com `role="dialog"` e `aria-modal`, `padding: 6vw` e fundo `radial-gradient(900px 500px at 0% 0%, var(--color-accent-900), transparent 70%), var(--color-bg)`.
- Em cima: “Termômetro · {aula}” em 20px muted, a pergunta “Como está a compreensão da turma?” em 52px e peso 500 e, à direita, a contagem em 56px (ou “Encerrado”).
- Três barras com rótulo de 36px, percentual em peso 600, trilho de 36px de altura e raio de 10px.
- Embaixo: a contagem de respostas e o botão “Sair do modo projeção (Esc)”. A tecla **Esc** fecha.

### 10. Painel por tema (US10)
- h6 com a aula, h3 “Painel por tema” e, à direita, o campo “Novo tema” (180px, placeholder “Ex.: Remoção”) com o botão “Criar”.
- Colunas em grid `auto-fit, minmax(min(100%, 250px), 1fr)`, uma por tema, mais a coluna “Sem tema”. Cada coluna tem fundo surface a 55%, `--shadow-sm`, raio de 8px e `padding: 8.4px`.
  - Cabeçalho: o nome em 15px e peso 500 e, em 12px muted, “N dúvidas · N apoios”.
  - Cartões `.card`: o texto em 14px, `hand-heart` com o total, as tags Pós-aula e Respondida e um **`<select>` para mover a dúvida de tema** (`aria-label` “Mover dúvida para outro tema”). O select é o jeito acessível pelo teclado (US11). Arrastar e soltar pode entrar depois, mas o select precisa continuar.
  - Coluna vazia: “Nenhuma dúvida neste tema.”
  - Ao mover, o toast diz “Dúvida movida para {tema}.”

### 11. Resumo da aula (US12)
- `max-width: 760px`. h6 “Resumo para a próxima aula”, h3 com a aula e o botão secundário “Imprimir” (`printer`), que chama `window.print()`. No `@media print`, esconda o cabeçalho, o menu e os botões.
- Faixa de status com fundo accent-900:
  - Antes do prazo: ícone `hourglass-medium` e “Prévia. O resumo final é gerado automaticamente quando o prazo pós-aula terminar ({prazo}), e você recebe o aviso por e-mail.”
  - Depois do prazo: ícone `envelope-simple` e “Gerado ao fim do prazo ({prazo}). O aviso foi enviado ao seu e-mail institucional só com o link deste resumo.” O e-mail não leva o conteúdo das dúvidas.
- Três números: dúvidas, apoios e enviadas após a aula.
- Um bloco por tema com h4, “N dúvidas · N apoios” e linhas em grid `44px 1fr` (apoios em accent-300 e o texto com “· respondida” e “· pós-aula” em muted).
- “Termômetros”: um bloco por termômetro, com o título “Termômetro N · HH:MM · N respostas” e as barras em grid `140px 1fr 48px` (rótulo, barra de 10px e percentual).

---

## Tela: Moderação (Professor e Monitor, US14)
- `max-width: 860px`. h6 com o perfil e h3 “Moderação do canal anônimo”. Texto: “Dúvidas com termos da lista ficam retidas aqui e não aparecem para a turma. O autor de dúvida anônima nunca é exibido.”
- **“Retidas pelo filtro · N”**: cartões `.card.elev-sm` com o texto, o autor (“Anônima” ou o nome), a hora e uma `.tag-accent` “Termo retido: {termo}”. Botões “Publicar” (primário, `check`) e “Ocultar” (secundário, `eye-slash`). Fila vazia: “Fila vazia. Nada aguardando revisão.”
- **“Publicadas”**: `.table` com as colunas Dúvida, Autor e Ação (ghost “Ocultar”). Abaixo, “N dúvidas ocultadas nesta aula.”
- **“Termos filtrados”**: `.tag-neutral` com um botão `x` para remover (`aria-label` “Remover termo X”) e o campo “Adicionar termo” com o botão secundário “Adicionar”. A comparação ignora maiúsculas e minúsculas.
- O item “Moderação” do menu mostra uma `.tag-accent` com o número de pendentes.

---

## Interações e comportamento
- **Status da dúvida:** `PUBLICADA` (a turma vê), `PENDENTE` (só o autor e a moderação veem) e `OCULTA` (ninguém vê na lista). O filtro age no `POST`.
- **Situação da aula:** `ABERTA` → (encerrar) → pós-aula, ainda aceitando envio até `prazo_pos_aula`, com `pos_aula = true` → `ENCERRADA` (um `@Scheduled` gera o `resumo_aula` e manda o e-mail).
- **Termômetro:** um aberto por vez por aula. A contagem regressiva no cliente usa `aberto_em + duracao_segundos` do servidor, nunca só o relógio local. O resultado é atualizado pelo polling.
- **Anonimato:** os DTOs de lista nunca trazem `autor_id`, nome, e-mail ou RA quando `anonima = true`, inclusive para o professor e o monitor. O cliente só sabe que a dúvida é sua por um campo booleano `minha`.
- **Teclado e leitor de tela (US11):** tudo é alcançável com Tab. Os diálogos prendem o foco e fecham com Esc. Use `aria-current` no menu, `aria-pressed` no apoio, `aria-live="polite"` na lista, no aviso de anonimato e nas mensagens de erro, e `role="status"` nos toasts. Nenhuma informação depende só da cor: as barras têm rótulo e percentual, e o modo de envio aparece por texto e ícone.
- **Contraste (AA):** texto pequeno em destaque usa `--color-accent-300`, nunca `--color-accent`. O accent puro fica para bordas, ícones e texto grande.
- **Transições:** só a largura das barras (0.4s). Nada mais é animado.
- **Validação:** dúvida com 1 a 500 caracteres (com trim), código com exatamente 6 caracteres, duração do termômetro de 15 a 600 segundos e e-mail institucional obrigatório no cadastro.

## Estado no cliente (sugestão em JS puro)
`usuario {id, nome, perfil, aceiteTermo, orientacaoVista}` · `turmaAtual` · `aulaAtual {id, situacao, prazoPosAula}` · `duvidas[] {id, texto, anonima, autorNome|null, minha, apoios, apoiadaPorMim, respondida, temaId, posAula, status, criadaEm}` · `termometroAtivo {id, abertoEm, duracao, minhaResposta, totais{entendi, parcial, naoEntendi}}` · `temas[]` · `filaOffline[]` (em `localStorage`) · `rascunho {texto, anonima: true}`.

## Telas × histórias do plano
| Tela | Histórias | Rotas principais |
|---|---|---|
| Login e cadastro, entrar na turma | base-acesso, US15 | `POST /api/auth/cadastro`, `POST /api/turmas/entrar` |
| Termo | US13 | aceite em `usuario.aceite_termo_em` |
| Orientação | US06 | `usuario.orientacao_vista_em` |
| Aula do estudante | US01, US03, US04, US05, US07, US09, US16 | `GET/POST /api/aulas/{id}/duvidas`, `POST/DELETE /api/duvidas/{id}/apoio`, `POST /api/termometros/{id}/respostas` |
| Meu histórico | US02, US13 | `GET /api/eu/historico`, exclusão das contribuições |
| Turmas e aulas | base-acesso, US05 | `POST /api/turmas`, `POST /api/turmas/{id}/aulas`, `PATCH /api/aulas/{id}` |
| Aula ao vivo do professor e projeção | US04, US08, US09, US02 | `POST /api/aulas/{id}/termometros`, `GET /api/turmas/{id}/participacao` |
| Painel por tema | US10 | `/api/professor/aulas/{id}/temas`, `PATCH /api/duvidas/{id}/tema`, `GET .../painel` |
| Resumo | US12 | `resumo_aula` |
| Moderação | US14 | `termo_ofensivo`, alteração de status da dúvida |
| Todas | US11 | — |

## Arquivos
- `prototipo/Participa.dc.html`: o protótipo completo, com todas as telas e estados. Os dados de exemplo estão no início do bloco `<script>` (`D0`).
- `prototipo/support.js`: o runtime que o protótipo precisa para abrir. **Não faz parte do produto.**
- `prototipo/_ds/nocturne-.../styles.css`: **a folha de estilo do sistema**, que pode ser reaproveitada.
- `prototipo/_ds/nocturne-.../_ds_bundle.js`: só para o protótipo.

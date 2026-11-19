import { api } from "/js/api.js";

const $ = (seletor, raiz = document) => raiz.querySelector(seletor);

function elemento(tag, classe = "", texto = "") {
  const no = document.createElement(tag);
  if (classe) no.className = classe;
  if (texto !== "") no.textContent = texto;
  return no;
}

function dataPt(valor) {
  if (!valor) return "—";
  const data = new Date(`${valor.slice(0, 10)}T12:00:00`);
  return new Intl.DateTimeFormat("pt-BR").format(data);
}

function situacaoTexto(situacao) {
  return ({ ABERTA: "Aula aberta", POS_AULA: "Pós-aula", ENCERRADA: "Encerrada" })[situacao] || situacao;
}

function tagSituacao(situacao) {
  const tag = elemento("span", `tag ${situacao === "ABERTA" ? "tag-accent" : "tag-outline"}`, situacaoTexto(situacao));
  return tag;
}

function campo({ id, rotulo, tipo = "text", obrigatorio = true, minimo, maximo, valor }) {
  const grupo = elemento("div", "field");
  const label = elemento("label", "", rotulo);
  label.htmlFor = id;
  const input = elemento("input", "input");
  input.id = id;
  input.name = id;
  input.type = tipo;
  input.required = obrigatorio;
  if (minimo !== undefined) input.min = minimo;
  if (maximo !== undefined) input.max = maximo;
  if (valor !== undefined) input.value = valor;
  grupo.append(label, input);
  return { grupo, input };
}

function dataFimPrazo(opcao, agora = new Date()) {
  const data = new Date(agora);
  if (opcao === "24") data.setHours(data.getHours() + 24);
  else if (opcao === "48") data.setHours(data.getHours() + 48);
  else {
    data.setDate(data.getDate() + 7);
    data.setHours(23, 59, 0, 0);
  }
  return data;
}

function formatoPrazo(data) {
  return new Intl.DateTimeFormat("pt-BR", { dateStyle: "short", timeStyle: "short" }).format(data);
}

function dialogoFormulario({ titulo, rotuloEnviar, aoEnviar, conteudo }) {
  const erro = elemento("p", "form-error");
  erro.setAttribute("role", "alert");
  erro.hidden = true;
  conteudo.append(erro);
  const dialogo = window.ParticipaUI.abrirDialogo({
    titulo,
    conteudo,
    acoes: [
      { rotulo: "Cancelar", classe: "btn-secondary", aoClicar: ({ fechar }) => fechar() },
      { rotulo: rotuloEnviar, classe: "btn-primary", aoClicar: () => conteudo.requestSubmit() }
    ]
  });
  conteudo.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    erro.hidden = true;
    try {
      await aoEnviar(new FormData(conteudo), dialogo.fechar);
    } catch (falha) {
      erro.textContent = falha.message || "Não foi possível concluir a ação.";
      erro.hidden = false;
    }
  });
}

async function iniciar() {
  await window.ParticipaShell.pronto;
  const main = $(".app-content");
  const conteudo = elemento("div", "teacher-page");
  main.replaceChildren(conteudo);

  const topo = elemento("header", "teacher-titlebar");
  const blocoTitulo = elemento("div");
  const tituloPagina = elemento("h1", "", "Turmas e aulas");
  tituloPagina.id = "titulo-pagina";
  blocoTitulo.append(elemento("h6", "teacher-kicker app-page-kicker", "Professor"), tituloPagina);
  const botaoCriar = elemento("button", "btn btn-primary teacher-title-action", "Criar turma");
  botaoCriar.type = "button";
  botaoCriar.prepend(window.ParticipaIcones.criar("plus"));
  topo.append(blocoTitulo, botaoCriar);

  const mensagem = elemento("p", "teacher-message form-error");
  mensagem.setAttribute("role", "alert");
  mensagem.hidden = true;
  const grade = elemento("section", "teacher-turma-grid");
  grade.setAttribute("aria-label", "Turmas do professor");
  const secaoAulas = elemento("section", "teacher-lessons");
  conteudo.append(topo, mensagem, grade, secaoAulas);

  let turmas = [];
  let aulas = [];
  let turmaSelecionada = null;
  const parametroTurma = new URLSearchParams(location.search).get("turmaId");

  function exibirMensagem(texto = "") {
    mensagem.textContent = texto;
    mensagem.hidden = !texto;
  }

  async function carregarAulas(turma) {
    turmaSelecionada = turma;
    try {
      aulas = await api("GET", `/api/professor/turmas/${encodeURIComponent(turma.id)}/aulas`);
      exibirMensagem();
      renderizarTurmas();
      renderizarAulas();
    } catch (falha) {
      aulas = [];
      renderizarTurmas();
      renderizarAulas();
      exibirMensagem(falha.message);
    }
  }

  function renderizarTurmas() {
    grade.replaceChildren();
    if (turmas.length === 0) {
      grade.append(elemento("p", "teacher-empty", "Você ainda não criou uma turma. Use “Criar turma” para começar."));
      return;
    }

    for (const turma of turmas) {
      const card = elemento("article", "card teacher-turma-card");
      card.setAttribute("aria-current", String(turmaSelecionada?.id === turma.id));
      const head = elemento("div", "teacher-turma-head");
      const textoHead = elemento("div");
      textoHead.append(elemento("p", "teacher-turma-kicker", `${turma.estudantes ?? 0} estudantes · até ${dataPt(turma.fimSemestre)}`));
      textoHead.append(elemento("h2", "card-title", turma.nome));
      head.append(textoHead);

      const codigoLabel = elemento("p", "teacher-code-label", "Código");
      const codigo = elemento("strong", "teacher-code", turma.codigo);
      const link = elemento("a", "teacher-card-link card-meta", `participa.app/entrar.html?codigo=${turma.codigo}`);
      link.href = `/entrar.html?codigo=${encodeURIComponent(turma.codigo)}`;
      link.setAttribute("aria-label", `Link para estudantes entrarem na turma ${turma.nome}`);
      const selecionar = elemento("button", "btn btn-secondary teacher-select", turmaSelecionada?.id === turma.id ? "Turma selecionada" : "Ver aulas");
      selecionar.type = "button";
      selecionar.setAttribute("aria-expanded", String(turmaSelecionada?.id === turma.id));
      selecionar.addEventListener("click", () => carregarAulas(turma));
      card.append(head, codigoLabel, codigo, link, selecionar);
      grade.append(card);
    }
  }

  function renderizarAulas() {
    secaoAulas.replaceChildren();
    if (!turmaSelecionada) return;

    const titulo = elemento("h2", "", `Aulas · ${turmaSelecionada.nome}`);
    const barra = elemento("div", "teacher-titlebar");
    barra.append(titulo);
    const existeAulaAberta = aulas.some((aula) => aula.situacao === "ABERTA");
    const abrir = elemento("button", "btn btn-ghost", "Abrir nova aula");
    abrir.type = "button";
    abrir.disabled = existeAulaAberta;
    abrir.prepend(window.ParticipaIcones.criar("play"));
    abrir.addEventListener("click", abrirDialogoNovaAula);
    barra.append(abrir);
    secaoAulas.append(barra);

    if (aulas.length === 0) {
      secaoAulas.append(elemento("p", "teacher-empty", "Esta turma ainda não tem aulas."));
      return;
    }

    const wrap = elemento("div", "teacher-table-wrap");
    const tabela = elemento("table", "table teacher-table");
    const thead = document.createElement("thead");
    const linhaCabecalho = document.createElement("tr");
    ["Aula", "Data", "Situação", "Ação"].forEach((nome) => linhaCabecalho.append(elemento("th", "", nome)));
    thead.append(linhaCabecalho);
    const tbody = document.createElement("tbody");

    for (const aula of aulas) {
      const linha = document.createElement("tr");
      linha.append(elemento("td", "", `Aula ${aula.numero} · ${aula.titulo}`));
      linha.append(elemento("td", "", dataPt(aula.data)));
      const celulaSituacao = document.createElement("td");
      celulaSituacao.append(tagSituacao(aula.situacao));
      linha.append(celulaSituacao);
      const celulaAcao = document.createElement("td");
      const acoes = elemento("div", "teacher-row-actions");

      if (aula.situacao === "ABERTA") {
        const verAula = elemento("a", "btn btn-ghost", "Aula ao vivo");
        verAula.href = `/professor/aula.html?turmaId=${encodeURIComponent(turmaSelecionada.id)}&aulaId=${encodeURIComponent(aula.id)}`;
        const encerrar = elemento("button", "btn btn-secondary", "Encerrar aula");
        encerrar.type = "button";
        encerrar.addEventListener("click", () => abrirDialogoEncerrar(aula));
        acoes.append(verAula, encerrar);
      } else if (aula.situacao === "ENCERRADA") {
        const resumo = elemento("a", "btn btn-ghost", aula.resumoEnviadoEm ? `Resumo enviado em ${dataPt(aula.resumoEnviadoEm)}` : "Ver resumo");
        resumo.href = `/pages/professor/resumo.html?aulaId=${encodeURIComponent(aula.id)}`;
        acoes.append(resumo);
      }

      celulaAcao.append(acoes);
      linha.append(celulaAcao);
      tbody.append(linha);
    }

    tabela.append(thead, tbody);
    wrap.append(tabela);
    secaoAulas.append(wrap);
  }

  function abrirDialogoCriarTurma() {
    const form = elemento("form", "teacher-dialog-form");
    const nome = campo({ id: "nome-turma", rotulo: "Nome da turma", maximo: 120 });
    const fim = campo({ id: "fim-semestre", rotulo: "Fim do semestre", tipo: "date" });
    const nota = elemento("p", "teacher-dialog-note text-muted", "Um código de 6 caracteres é gerado para os estudantes entrarem. No fim do semestre, os dados individuais são apagados e ficam só os totais.");
    form.append(nome.grupo, fim.grupo, nota);
    dialogoFormulario({
      titulo: "Criar turma",
      rotuloEnviar: "Criar turma",
      conteudo: form,
      aoEnviar: async (dados, fechar) => {
        const novaTurma = await api("POST", "/api/professor/turmas", {
          nome: String(dados.get("nome-turma")).trim(),
          fimSemestre: dados.get("fim-semestre")
        });
        fechar();
        window.ParticipaUI.mostrarToast("Turma criada.");
        await carregarTurmas(novaTurma.id);
      }
    });
  }

  function abrirDialogoNovaAula() {
    if (!turmaSelecionada) return;
    const form = elemento("form", "teacher-dialog-form");
    const tituloAula = campo({ id: "titulo-aula", rotulo: "Título da aula", maximo: 160 });
    form.append(tituloAula.grupo);
    dialogoFormulario({
      titulo: "Abrir nova aula",
      rotuloEnviar: "Abrir aula",
      conteudo: form,
      aoEnviar: async (dados, fechar) => {
        await api("POST", `/api/professor/turmas/${encodeURIComponent(turmaSelecionada.id)}/aulas`, {
          titulo: String(dados.get("titulo-aula")).trim()
        });
        fechar();
        window.ParticipaUI.mostrarToast("Aula aberta.");
        await carregarAulas(turmaSelecionada);
      }
    });
  }

  function abrirDialogoEncerrar(aula) {
    const form = elemento("form", "teacher-dialog-form teacher-dialog");
    const texto = elemento("p", "teacher-dialog-note text-muted", "Os estudantes ainda podem enviar dúvidas até o prazo abaixo. Elas aparecem marcadas como pós-aula. Quando o prazo terminar, o resumo é gerado e você recebe o aviso no e-mail institucional.");
    const grupo = elemento("fieldset", "access-profile");
    const legenda = elemento("legend", "", "Prazo para dúvidas pós-aula");
    const opcoes = elemento("div", "seg");
    const previsao = elemento("p", "teacher-date-preview");
    const radioGroup = `prazo-${aula.id}`;
    const alternativas = [
      { valor: "24", rotulo: "24 horas" },
      { valor: "48", rotulo: "48 horas", marcado: true },
      { valor: "proxima", rotulo: "Até a próxima aula" }
    ];
    for (const alternativa of alternativas) {
      const label = elemento("label", "seg-opt");
      const radio = document.createElement("input");
      radio.type = "radio";
      radio.name = "opcao";
      radio.value = alternativa.valor;
      radio.checked = Boolean(alternativa.marcado);
      const rotulo = elemento("span", "", alternativa.rotulo);
      label.append(radio, rotulo);
      opcoes.append(label);
      radio.addEventListener("change", atualizarPrevisao);
    }
    function atualizarPrevisao() {
      const opcao = form.querySelector(`input[name='opcao']:checked`)?.value || "48";
      previsao.textContent = `Até ${formatoPrazo(dataFimPrazo(opcao))}`;
    }
    grupo.append(legenda, opcoes);
    form.append(texto, grupo, previsao);
    atualizarPrevisao();
    dialogoFormulario({
      titulo: `Encerrar a Aula ${aula.numero}`,
      rotuloEnviar: "Encerrar aula",
      conteudo: form,
      aoEnviar: async (dados, fechar) => {
        const resultado = await api("PATCH", `/api/professor/aulas/${encodeURIComponent(aula.id)}/encerrar`, {
          opcao: dados.get("opcao")
        });
        fechar();
        const prazo = resultado.prazoPosAula ? formatoPrazo(new Date(resultado.prazoPosAula)) : previsao.textContent.replace(/^Até /, "");
        window.ParticipaUI.mostrarToast(`Aula encerrada. Dúvidas pós-aula abertas até ${prazo}.`);
        await carregarAulas(turmaSelecionada);
      }
    });
  }

  async function carregarTurmas(idInicial) {
    exibirMensagem();
    turmas = await api("GET", "/api/professor/turmas");
    renderizarTurmas();
    const id = idInicial || parametroTurma;
    turmaSelecionada = turmas.find((turma) => String(turma.id) === String(id)) || turmas[0] || null;
    if (turmaSelecionada) await carregarAulas(turmaSelecionada);
    else {
      aulas = [];
      renderizarAulas();
    }
  }

  botaoCriar.addEventListener("click", abrirDialogoCriarTurma);
  try {
    await carregarTurmas();
  } catch (falha) {
    exibirMensagem(falha.message || "Não foi possível carregar suas turmas.");
  }
}

function iniciarAoCarregar() {
  iniciar().catch((falha) => {
    const main = $(".app-content");
    if (main) main.textContent = falha.message || "Não foi possível carregar a tela.";
  });
}

if (document.readyState === "loading") document.addEventListener("DOMContentLoaded", iniciarAoCarregar, { once: true });
else iniciarAoCarregar();

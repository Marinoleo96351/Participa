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
  return new Intl.DateTimeFormat("pt-BR").format(new Date(`${valor.slice(0, 10)}T12:00:00`));
}

function formatoPrazo(valor) {
  return new Intl.DateTimeFormat("pt-BR", { dateStyle: "short", timeStyle: "short" }).format(new Date(valor));
}

async function localizarAula() {
  const parametros = new URLSearchParams(location.search);
  const aulaId = parametros.get("aulaId");
  const turmaId = parametros.get("turmaId");
  const turmas = await api("GET", "/api/professor/turmas");
  const candidatas = turmaId ? turmas.filter((turma) => String(turma.id) === turmaId) : turmas;
  for (const turma of candidatas) {
    const aulas = await api("GET", `/api/professor/turmas/${encodeURIComponent(turma.id)}/aulas`);
    const aula = aulaId
      ? aulas.find((item) => String(item.id) === aulaId)
      : aulas.find((item) => item.situacao === "ABERTA");
    if (aula) return { turma, aula };
  }
  return null;
}

function abrirDialogoEncerrar(aula, aoSalvar) {
  const form = elemento("form", "teacher-dialog-form teacher-dialog");
  const texto = elemento("p", "teacher-dialog-note text-muted", "Os estudantes ainda podem enviar dúvidas até o prazo abaixo. Elas aparecem marcadas como pós-aula. Quando o prazo terminar, o resumo é gerado e você recebe o aviso no e-mail institucional.");
  const grupo = elemento("fieldset", "access-profile");
  const legenda = elemento("legend", "", "Prazo para dúvidas pós-aula");
  const opcoes = elemento("div", "seg");
  const previsao = elemento("p", "teacher-date-preview");
  for (const opcao of [
    { valor: "24", rotulo: "24 horas" },
    { valor: "48", rotulo: "48 horas", marcado: true },
    { valor: "proxima", rotulo: "Até a próxima aula" }
  ]) {
    const label = elemento("label", "seg-opt");
    const radio = document.createElement("input");
    radio.type = "radio";
    radio.name = "opcao";
    radio.value = opcao.valor;
    radio.checked = Boolean(opcao.marcado);
    label.append(radio, elemento("span", "", opcao.rotulo));
    opcoes.append(label);
    radio.addEventListener("change", atualizarPrevisao);
  }

  function atualizarPrevisao() {
    const opcao = form.querySelector("input[name='opcao']:checked")?.value || "48";
    const data = new Date();
    if (opcao === "24") data.setHours(data.getHours() + 24);
    else if (opcao === "48") data.setHours(data.getHours() + 48);
    else {
      data.setDate(data.getDate() + 7);
      data.setHours(23, 59, 0, 0);
    }
    previsao.textContent = `Até ${formatoPrazo(data.toISOString())}`;
  }

  grupo.append(legenda, opcoes);
  form.append(texto, grupo, previsao);
  const erro = elemento("p", "form-error");
  erro.setAttribute("role", "alert");
  erro.hidden = true;
  form.append(erro);
  const dialogo = window.ParticipaUI.abrirDialogo({
    titulo: `Encerrar a Aula ${aula.numero}`,
    conteudo: form,
    acoes: [
      { rotulo: "Cancelar", classe: "btn-secondary", aoClicar: ({ fechar }) => fechar() },
      { rotulo: "Encerrar aula", classe: "btn-primary", aoClicar: () => form.requestSubmit() }
    ]
  });
  form.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    erro.hidden = true;
    try {
      const resultado = await api("PATCH", `/api/professor/aulas/${encodeURIComponent(aula.id)}/encerrar`, {
        opcao: new FormData(form).get("opcao")
      });
      dialogo.fechar();
      window.ParticipaUI.mostrarToast(`Aula encerrada. Dúvidas pós-aula abertas até ${formatoPrazo(resultado.prazoPosAula)}.`);
      aoSalvar(resultado);
    } catch (falha) {
      erro.textContent = falha.message || "Não foi possível encerrar a aula.";
      erro.hidden = false;
    }
  });
  atualizarPrevisao();
}

async function iniciar() {
  await window.ParticipaShell.pronto;
  const main = $(".app-content");
  const pagina = elemento("div", "teacher-page");
  main.replaceChildren(pagina);

  const aviso = elemento("p", "teacher-message form-error");
  aviso.setAttribute("role", "alert");
  aviso.hidden = true;
  pagina.append(aviso);

  try {
    const local = await localizarAula();
    if (!local) {
      const tituloVazio = elemento("h1", "teacher-aula-title", "Aula ao vivo");
      tituloVazio.id = "titulo-pagina";
      const vazio = elemento("section", "teacher-empty");
      vazio.append(elemento("p", "", "Nenhuma aula aberta foi encontrada."));
      const voltar = elemento("a", "btn btn-secondary", "Voltar para Turmas e aulas");
      voltar.href = "/professor/turmas.html";
      vazio.append(voltar);
      pagina.append(tituloVazio, vazio);
      return;
    }

    let { aula } = local;
    const cabecalho = elemento("header", "teacher-aula-head");
    const tituloBloco = elemento("div", "teacher-aula-title");
    const kicker = elemento("h6", "teacher-aula-meta", `Aula ${aula.numero} · ${dataPt(aula.data)} · código ${local.turma.codigo}`);
    const titulo = elemento("h1", "", aula.titulo);
    titulo.id = "titulo-pagina";
    tituloBloco.append(kicker, titulo);
    const acoes = elemento("div", "teacher-aula-actions");
    const rotuloSituacao = aula.situacao === "ABERTA"
      ? "Aula aberta"
      : aula.situacao === "POS_AULA"
        ? `Pós-aula até ${aula.prazoPosAula ? formatoPrazo(aula.prazoPosAula) : "prazo não informado"}`
        : "Encerrada";
    const situacao = elemento("span", `tag ${aula.situacao === "ABERTA" ? "tag-accent" : "tag-outline"}`, rotuloSituacao);
    acoes.append(situacao);
    if (aula.situacao === "ABERTA") {
      const encerrar = elemento("button", "btn btn-primary teacher-title-action", "Encerrar aula");
      encerrar.type = "button";
      encerrar.addEventListener("click", () => abrirDialogoEncerrar(aula, (atualizada) => {
        aula = atualizada;
        situacao.className = "tag tag-outline";
        situacao.textContent = `Pós-aula até ${atualizada.prazoPosAula ? formatoPrazo(atualizada.prazoPosAula) : "prazo não informado"}`;
        encerrar.remove();
      }));
      acoes.append(encerrar);
    }
    cabecalho.append(tituloBloco, acoes);
    const reservada = elemento("section", "app-empty-block teacher-aula-placeholder");
    reservada.setAttribute("aria-label", "Área da aula ao vivo, desenvolvida em outra etapa");
    pagina.append(cabecalho, reservada);
    window.ParticipaAulaAtual = aula;
    document.dispatchEvent(new CustomEvent("participa:aula-carregada", { detail: { aula } }));
  } catch (falha) {
    aviso.textContent = falha.message || "Não foi possível carregar os dados da aula.";
    aviso.hidden = false;
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
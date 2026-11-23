import { api } from "/js/api.js";

const OPCOES = [
  { valor: "ENTENDI", rotulo: "Entendi", icone: "check-circle" },
  { valor: "PARCIAL", rotulo: "Entendi em parte", icone: "circle-half" },
  { valor: "NAO_ENTENDI", rotulo: "Não entendi", icone: "x-circle" }
];

function elemento(tag, classe = "", texto = "") {
  const no = document.createElement(tag);
  if (classe) no.className = classe;
  if (texto !== "") no.textContent = texto;
  return no;
}

function formatarContagem(segundos) {
  const total = Math.max(0, Math.ceil(segundos));
  const minutos = Math.floor(total / 60);
  const resto = String(total % 60).padStart(2, "0");
  return `${minutos}:${resto}`;
}

let secao;
let aulaId = null;
let termometro = null;
let diferencaRelogio = 0;
let chaveRenderizada = null;
let temporizador = null;

function restanteSegundos() {
  const fim = Date.parse(termometro.abertoEm) + termometro.duracaoSegundos * 1000;
  return (fim - (Date.now() + diferencaRelogio)) / 1000;
}

function pararContagem() {
  if (temporizador !== null) {
    window.clearInterval(temporizador);
    temporizador = null;
  }
}

function esconder() {
  pararContagem();
  secao.hidden = true;
  secao.replaceChildren();
  chaveRenderizada = null;
}

function montar() {
  const cartao = elemento("div", "card elev-md");
  cartao.style.background = "linear-gradient(120deg, var(--color-accent-900), var(--color-surface) 60%)";

  const linha = elemento("div");
  linha.style.display = "flex";
  linha.style.alignItems = "center";
  linha.style.gap = "12px";
  linha.append(window.ParticipaIcones.criar("thermometer-simple"));
  const titulo = elemento("h2", "card-title", "O professor quer saber: você está acompanhando?");
  titulo.style.flex = "1";
  const contagem = elemento("span", "", formatarContagem(restanteSegundos()));
  contagem.id = "termometro-contagem";
  contagem.style.fontSize = "20px";
  contagem.style.color = "var(--color-accent-300)";
  contagem.style.fontVariantNumeric = "tabular-nums";
  linha.append(titulo, contagem);
  cartao.append(linha);

  if (termometro.minhaResposta) {
    const opcao = OPCOES.find((o) => o.valor === termometro.minhaResposta);
    const mensagem = elemento("p", "text-muted");
    mensagem.setAttribute("role", "status");
    mensagem.append("Resposta registrada: ");
    mensagem.append(elemento("strong", "", opcao ? opcao.rotulo : ""));
    mensagem.append(". O professor vê só os percentuais da turma.");
    cartao.append(mensagem);
  } else {
    const grade = elemento("div");
    grade.style.display = "grid";
    grade.style.gridTemplateColumns = "repeat(auto-fit, minmax(130px, 1fr))";
    grade.style.gap = "8px";
    grade.style.marginTop = "12px";
    for (const opcao of OPCOES) {
      const botao = elemento("button", "btn btn-secondary");
      botao.type = "button";
      botao.style.minHeight = "48px";
      botao.append(window.ParticipaIcones.criar(opcao.icone), elemento("span", "", opcao.rotulo));
      botao.addEventListener("click", () => responder(botao, grade, opcao.valor));
      grade.append(botao);
    }
    cartao.append(grade);
  }

  secao.replaceChildren(cartao);
  secao.hidden = false;
}

async function responder(botao, grade, valor) {
  grade.querySelectorAll("button").forEach((b) => { b.disabled = true; });
  try {
    await api("POST", `/api/termometros/${encodeURIComponent(termometro.id)}/respostas`, { opcao: valor });
    termometro = { ...termometro, minhaResposta: valor };
    desenhar();
  } catch (falha) {
    window.ParticipaUI.mostrarToast(falha.message || "Não foi possível registrar sua resposta.");
    grade.querySelectorAll("button").forEach((b) => { b.disabled = false; });
  }
}

function desenhar() {
  if (!termometro || !termometro.aberto || restanteSegundos() <= 0) {
    esconder();
    return;
  }
  const chave = `${termometro.id}:${termometro.minhaResposta ?? ""}`;
  if (chave !== chaveRenderizada) {
    chaveRenderizada = chave;
    montar();
  }
  if (temporizador === null) {
    temporizador = window.setInterval(() => {
      if (restanteSegundos() <= 0) {
        esconder();
        return;
      }
      const alvo = document.getElementById("termometro-contagem");
      if (alvo) alvo.textContent = formatarContagem(restanteSegundos());
    }, 1000);
  }
}

async function atualizar() {
  if (aulaId === null) {
    const aula = await api("GET", "/api/eu/aula-atual");
    if (!aula) return;
    aulaId = aula.id;
  }
  const estado = await api("GET", `/api/aulas/${encodeURIComponent(aulaId)}/estado`);
  diferencaRelogio = Date.parse(estado.servidorAgora) - Date.now();
  termometro = estado.termometro;
  desenhar();
}

async function iniciar() {
  await window.ParticipaShell.pronto;
  const main = document.querySelector(".app-content");
  secao = elemento("section");
  secao.id = "termometro";
  secao.hidden = true;
  secao.setAttribute("aria-label", "Termômetro de compreensão");
  main.append(secao);

  const polling = window.ParticipaPolling.criar(atualizar, {
    intervaloMs: 3000,
    aoFalhar: (erro) => console.error(erro)
  });
  polling.iniciar({ imediato: true });
}

iniciar();
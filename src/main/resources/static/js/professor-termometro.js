import { api } from "/js/api.js";

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

function percentual(parte, total) {
  return total > 0 ? Math.round((parte * 100) / total) : 0;
}

let iniciado = false;
let aulaId = null;
let termometro = null;
let aulaAberta = true;
let diferencaRelogio = 0;
let temporizador = null;

// Pedaços da tela que são atualizados sem refazer o cartão
// (assim o professor não perde o número digitado a cada 3 s).
let tituloCartao;
let areaAbrir;
let campoDuracao;
let areaAberto;
let textoContagem;
let areaResultado;
let textoRespondidos;
let linhas;
let mensagemErro;

function restanteSegundos() {
  const fim = Date.parse(termometro.abertoEm) + termometro.duracaoSegundos * 1000;
  return (fim - (Date.now() + diferencaRelogio)) / 1000;
}

function estaAtivo() {
  return Boolean(termometro && termometro.aberto && restanteSegundos() > 0);
}

function mostrarErro(texto) {
  mensagemErro.textContent = texto;
  mensagemErro.hidden = !texto;
}

function desenhar() {
  const ativo = estaAtivo();

  areaAbrir.hidden = ativo || !aulaAberta;
  areaAberto.hidden = !ativo;
  areaResultado.hidden = !termometro;

  if (!termometro) {
    tituloCartao.textContent = "Termômetro";
    return;
  }

  tituloCartao.textContent = `Termômetro nº ${termometro.numero}`;
  if (ativo) textoContagem.textContent = `Aberto · restam ${formatarContagem(restanteSegundos())}`;

  const r = termometro.resultado;
  if (!r) return;

  textoRespondidos.textContent =
    `${r.respostas} de ${r.matriculados} estudantes responderam${ativo ? "" : " · encerrado"}`;

  const dados = [
    ["Entendi", r.entendi],
    ["Entendi em parte", r.parcial],
    ["Não entendi", r.naoEntendi]
  ];
  linhas.replaceChildren(
    ...dados.map(([rotulo, valor]) => {
      const li = elemento("li", "", `${rotulo}: ${valor} (${percentual(valor, r.respostas)}%)`);
      li.style.fontVariantNumeric = "tabular-nums";
      return li;
    })
  );
}

async function atualizar() {
  const estado = await api("GET", `/api/aulas/${encodeURIComponent(aulaId)}/estado`);
  diferencaRelogio = Date.parse(estado.servidorAgora) - Date.now();
  termometro = estado.termometro;
  aulaAberta = estado.aula.situacao === "ABERTA";
  desenhar();
}

async function abrirTermometro(botao) {
  mostrarErro("");
  const duracao = Number(campoDuracao.value);
  if (!Number.isInteger(duracao) || duracao < 15 || duracao > 600) {
    mostrarErro("A duração deve ficar entre 15 e 600 segundos.");
    return;
  }
  botao.disabled = true;
  try {
    await api("POST", `/api/professor/aulas/${encodeURIComponent(aulaId)}/termometros`, {
      duracaoSegundos: duracao
    });
    await atualizar();
  } catch (falha) {
    mostrarErro(falha.message || "Não foi possível abrir o termômetro.");
  } finally {
    botao.disabled = false;
  }
}

async function encerrarAgora(botao) {
  mostrarErro("");
  botao.disabled = true;
  try {
    await api("PATCH", `/api/professor/termometros/${encodeURIComponent(termometro.id)}/encerrar`);
    await atualizar();
  } catch (falha) {
    mostrarErro(falha.message || "Não foi possível encerrar o termômetro.");
  } finally {
    botao.disabled = false;
  }
}

function montar() {
  const cartao = elemento("section", "card elev-md");
  cartao.id = "termometro";
  cartao.setAttribute("aria-label", "Termômetro de compreensão");
  cartao.style.marginBottom = "16px";

  const cabecalho = elemento("div");
  cabecalho.style.display = "flex";
  cabecalho.style.alignItems = "center";
  cabecalho.style.gap = "12px";
  tituloCartao = elemento("h2", "card-title", "Termômetro");
  cabecalho.append(window.ParticipaIcones.criar("thermometer-simple"), tituloCartao);

  // Abrir um termômetro novo
  areaAbrir = elemento("div");
  areaAbrir.style.display = "flex";
  areaAbrir.style.alignItems = "flex-end";
  areaAbrir.style.gap = "12px";
  areaAbrir.style.flexWrap = "wrap";
  areaAbrir.style.marginTop = "12px";
  const campo = elemento("div", "field");
  const rotulo = elemento("label", "", "Duração (segundos)");
  rotulo.htmlFor = "termometro-duracao";
  campoDuracao = elemento("input", "input");
  campoDuracao.id = "termometro-duracao";
  campoDuracao.type = "number";
  campoDuracao.min = "15";
  campoDuracao.max = "600";
  campoDuracao.step = "15";
  campoDuracao.value = "60";
  campo.append(rotulo, campoDuracao);
  const botaoAbrir = elemento("button", "btn btn-primary", "Abrir termômetro");
  botaoAbrir.type = "button";
  botaoAbrir.addEventListener("click", () => abrirTermometro(botaoAbrir));
  areaAbrir.append(campo, botaoAbrir);

  // Termômetro aberto: contagem e botão de encerrar
  areaAberto = elemento("div");
  areaAberto.style.display = "flex";
  areaAberto.style.alignItems = "center";
  areaAberto.style.gap = "12px";
  areaAberto.style.flexWrap = "wrap";
  areaAberto.style.marginTop = "12px";
  areaAberto.hidden = true;
  textoContagem = elemento("span", "tag tag-accent");
  textoContagem.style.fontVariantNumeric = "tabular-nums";
  const botaoEncerrar = elemento("button", "btn btn-secondary", "Encerrar agora");
  botaoEncerrar.type = "button";
  botaoEncerrar.addEventListener("click", () => encerrarAgora(botaoEncerrar));
  areaAberto.append(textoContagem, botaoEncerrar);

  // Resultado em números (as barras vêm na US08)
  areaResultado = elemento("div");
  areaResultado.style.marginTop = "12px";
  areaResultado.hidden = true;
  textoRespondidos = elemento("p", "text-muted");
  textoRespondidos.setAttribute("aria-live", "polite");
  linhas = elemento("ul");
  linhas.style.margin = "8px 0 0";
  linhas.style.paddingLeft = "20px";
  areaResultado.append(textoRespondidos, linhas);

  mensagemErro = elemento("p", "form-error");
  mensagemErro.setAttribute("role", "alert");
  mensagemErro.hidden = true;

  cartao.append(cabecalho, areaAbrir, areaAberto, areaResultado, mensagemErro);

  const reservada = document.querySelector(".teacher-aula-placeholder");
  if (reservada) reservada.before(cartao);
  else (document.querySelector(".teacher-page") || document.querySelector(".app-content")).append(cartao);
}

function iniciar(aula) {
  if (iniciado) return;
  iniciado = true;
  aulaId = aula.id;
  aulaAberta = aula.situacao === "ABERTA";
  montar();

  // Contagem regressiva que anda de 1 em 1 segundo
  temporizador = window.setInterval(() => {
    if (termometro) desenhar();
  }, 1000);

  const polling = window.ParticipaPolling.criar(atualizar, {
    intervaloMs: 3000,
    aoFalhar: (erro) => console.error(erro)
  });
  polling.iniciar({ imediato: true });
}

// O professor-aula.js avisa quando a aula foi carregada.
document.addEventListener("participa:aula-carregada", (evento) => iniciar(evento.detail.aula));
if (window.ParticipaAulaAtual) iniciar(window.ParticipaAulaAtual);
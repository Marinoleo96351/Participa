import { api } from "/js/api.js";

function elemento(tag, classe = "", texto = "") {
  const no = document.createElement(tag);
  if (classe) no.className = classe;
  if (texto !== "") no.textContent = texto;
  return no;
}

function formatoPrazo(valor) {
  return new Intl.DateTimeFormat("pt-BR", { dateStyle: "short", timeStyle: "short" }).format(new Date(valor));
}

function formatoHora(valor) {
  return new Intl.DateTimeFormat("pt-BR", { hour: "2-digit", minute: "2-digit" }).format(new Date(valor));
}

let aulaId = null;
let usuarioNome = "";
let diferencaRelogio = 0;
let chaveRenderizada = null;
let duvidasAtuais = [];

let secaoBanner;
let secaoDuvidas;
let formEnvio;
let campoTexto;
let campoAnonimo;
let contador;
let avisoAnonimato;
let avisoPrazo;
let areaEnvio;
let listaEl;
let botaoEnviar;

function chaveFila() {
  return `participa.fila-duvidas.${aulaId}`;
}

function lerFila() {
  try {
    const bruto = window.localStorage.getItem(chaveFila());
    return bruto ? JSON.parse(bruto) : [];
  } catch {
    return [];
  }
}

function salvarFila(fila) {
  try {
    window.localStorage.setItem(chaveFila(), JSON.stringify(fila));
  } catch {
    // armazenamento indisponível: a fila fica só em memória até a próxima renderização.
  }
}

function enfileirar(texto, anonima) {
  const fila = lerFila();
  fila.push({ idLocal: `local-${Date.now()}-${Math.random().toString(36).slice(2)}`, texto, anonima, criadaEm: new Date().toISOString() });
  salvarFila(fila);
}

async function esvaziarFila() {
  let fila = lerFila();
  if (fila.length === 0) return;
  let enviadas = 0;
  while (fila.length > 0) {
    const item = fila[0];
    try {
      await api("POST", `/api/aulas/${encodeURIComponent(aulaId)}/duvidas`, { texto: item.texto, anonima: item.anonima });
      fila = fila.slice(1);
      salvarFila(fila);
      enviadas += 1;
    } catch (falha) {
      console.error(falha);
      break;
    }
  }
  if (enviadas > 0) {
    window.ParticipaUI.mostrarToast(`${enviadas} dúvida${enviadas > 1 ? "s" : ""} enviada${enviadas > 1 ? "s" : ""} ao reconectar.`);
    desenharLista();
  }
}

function atualizarAvisoAnonimato() {
  avisoAnonimato.replaceChildren();
  const texto = elemento("span");
  if (campoAnonimo.checked) {
    avisoAnonimato.append(window.ParticipaIcones.criar("mask-happy"), texto);
    texto.append("Sua dúvida será enviada como ", elemento("strong", "", "ANÔNIMA"), ". Ninguém verá seu nome, nem o professor.");
  } else {
    avisoAnonimato.append(window.ParticipaIcones.criar("identification-badge"), texto);
    texto.append("Sua dúvida será enviada como ", elemento("strong", "", "IDENTIFICADA"), `. A turma e o professor verão seu nome: ${usuarioNome}.`);
  }
}

function atualizarContador() {
  contador.textContent = `${campoTexto.value.length} / 500`;
  botaoEnviar.disabled = campoTexto.value.trim().length === 0;
}

function montarCartaoEnvio() {
  const cartao = elemento("div", "card elev-sm duvidas-card");
  const rotulo = elemento("label", "card-title", "Sua dúvida");
  rotulo.htmlFor = "duvida-texto";
  avisoPrazo = elemento("p", "duvidas-prazo-aviso");
  avisoPrazo.hidden = true;

  formEnvio = elemento("form", "duvidas-field");
  campoTexto = document.createElement("textarea");
  campoTexto.id = "duvida-texto";
  campoTexto.className = "input";
  campoTexto.maxLength = 500;
  campoTexto.placeholder = "Escreva aqui. Ex.: por que a rotação dupla é necessária?";
  campoTexto.setAttribute("aria-describedby", "duvida-contador");
  campoTexto.addEventListener("input", atualizarContador);

  const linhaAnonimo = elemento("div", "duvidas-anonimo-linha");
  const opcaoAnonimo = elemento("label", "duvidas-anonimo-opcao");
  campoAnonimo = document.createElement("input");
  campoAnonimo.type = "checkbox";
  campoAnonimo.checked = true;
  campoAnonimo.addEventListener("change", atualizarAvisoAnonimato);
  opcaoAnonimo.append(campoAnonimo, elemento("span", "", "Enviar como anônimo"));
  contador = elemento("span", "duvidas-contador", "0 / 500");
  contador.id = "duvida-contador";
  linhaAnonimo.append(opcaoAnonimo, contador);

  avisoAnonimato = elemento("div", "duvidas-aviso-anonimato");
  avisoAnonimato.setAttribute("aria-live", "polite");

  botaoEnviar = elemento("button", "btn btn-primary btn-block", "");
  botaoEnviar.type = "submit";
  botaoEnviar.disabled = true;
  botaoEnviar.append(window.ParticipaIcones.criar("paper-plane-tilt"), elemento("span", "", "Enviar dúvida"));

  formEnvio.append(campoTexto, linhaAnonimo, avisoAnonimato, botaoEnviar);
  formEnvio.addEventListener("submit", enviar);

  areaEnvio = elemento("div");
  areaEnvio.append(avisoPrazo, formEnvio);

  const encerrada = elemento("div", "duvidas-encerrada");
  encerrada.hidden = true;
  encerrada.append(window.ParticipaIcones.criar("lock-simple"), elemento("p", "", ""));

  cartao.append(rotulo, areaEnvio, encerrada);
  cartao._encerrada = encerrada;
  return cartao;
}

async function enviar(evento) {
  evento.preventDefault();
  const texto = campoTexto.value.trim();
  if (!texto) return;
  const anonima = campoAnonimo.checked;
  botaoEnviar.disabled = true;

  if (!navigator.onLine) {
    enfileirar(texto, anonima);
    limparFormulario();
    window.ParticipaUI.mostrarToast("Sem conexão. A dúvida ficou salva no aparelho e segue quando a conexão voltar.");
    desenharLista();
    return;
  }

  try {
    const resultado = await api("POST", `/api/aulas/${encodeURIComponent(aulaId)}/duvidas`, { texto, anonima });
    limparFormulario();
    window.ParticipaUI.mostrarToast(resultado.status === "PENDENTE"
      ? "Recebida. Ela passa pela moderação antes de aparecer para a turma."
      : "Enviada. Sua dúvida já está na lista da aula.");
    void atualizar();
  } catch (falha) {
    if (falha.status === undefined) {
      enfileirar(texto, anonima);
      limparFormulario();
      window.ParticipaUI.mostrarToast("Sem conexão. A dúvida ficou salva no aparelho e segue quando a conexão voltar.");
      desenharLista();
    } else {
      window.ParticipaUI.mostrarToast(falha.message || "Não foi possível enviar sua dúvida.");
      atualizarContador();
    }
  }
}

function limparFormulario() {
  campoTexto.value = "";
  campoAnonimo.checked = true;
  atualizarContador();
  atualizarAvisoAnonimato();
}

function montarListaRaiz() {
  const raiz = elemento("div", "duvidas-lista-wrap");
  const cabecalho = elemento("div", "duvidas-lista-head");
  cabecalho.append(elemento("h4", "", "Dúvidas da aula"));
  cabecalho.append(elemento("span", "text-muted", "Mais apoiadas primeiro · respondidas no fim · atualiza a cada 3 s"));
  listaEl = document.createElement("ul");
  listaEl.className = "duvidas-lista";
  listaEl.setAttribute("aria-live", "polite");
  raiz.append(cabecalho, listaEl);
  return raiz;
}

async function alternarApoio(duvida, botao) {
  botao.disabled = true;
  try {
    const resultado = duvida.apoiadaPorMim
      ? await api("DELETE", `/api/duvidas/${encodeURIComponent(duvida.id)}/apoio`)
      : await api("POST", `/api/duvidas/${encodeURIComponent(duvida.id)}/apoio`);
    duvida.apoios = resultado.apoios;
    duvida.apoiadaPorMim = resultado.apoiadaPorMim;
    chaveRenderizada = null;
    desenharLista();
  } catch (falha) {
    window.ParticipaUI.mostrarToast(falha.message || "Não foi possível registrar o apoio.");
    botao.disabled = false;
  }
}

function montarItem(duvida, { local = false } = {}) {
  const item = elemento("li", `duvidas-item${duvida.respondida ? " duvidas-item--respondida" : ""}`);
  const corpo = elemento("div", "duvidas-item-corpo");
  corpo.append(elemento("p", "duvidas-item-texto", duvida.texto));

  const meta = elemento("div", "duvidas-item-meta");
  meta.append(elemento("span", "", duvida.anonima ? "Anônima" : duvida.autorNome || "Identificada"));
  if (duvida.minha) {
    meta.append(elemento("span", "tag tag-accent", duvida.anonima ? "Enviada como anônima" : "Enviada com seu nome"));
  }
  meta.append(elemento("span", "", `· ${formatoHora(duvida.criadaEm)}`));
  if (duvida.respondida) meta.append(elemento("span", "tag tag-neutral", "Respondida"));
  if (duvida.posAula) meta.append(elemento("span", "tag tag-neutral", "Pós-aula"));
  if (local) meta.append(elemento("span", "tag tag-outline", "Aguardando conexão"));
  else if (duvida.status === "PENDENTE") meta.append(elemento("span", "tag tag-outline", "Em revisão pela moderação"));
  corpo.append(meta);

  const botaoApoio = elemento("button", `btn duvidas-item-apoio ${duvida.apoiadaPorMim ? "btn-primary" : "btn-secondary"}`);
  botaoApoio.type = "button";
  botaoApoio.setAttribute("aria-pressed", String(Boolean(duvida.apoiadaPorMim)));
  botaoApoio.setAttribute("aria-label", `${duvida.apoiadaPorMim ? "Desfazer apoio" : "Apoiar esta dúvida"}. ${duvida.apoios} apoios`);
  botaoApoio.disabled = local || duvida.minha;
  botaoApoio.append(window.ParticipaIcones.criar("hand-heart", { preenchido: Boolean(duvida.apoiadaPorMim) }), elemento("span", "", String(duvida.apoios)));
  if (!botaoApoio.disabled) botaoApoio.addEventListener("click", () => alternarApoio(duvida, botaoApoio));

  item.append(corpo, botaoApoio);
  return item;
}

function desenharLista() {
  const fila = lerFila();
  listaEl.replaceChildren();
  if (fila.length === 0 && duvidasAtuais.length === 0) {
    listaEl.append(elemento("li", "duvidas-vazia", "Nenhuma dúvida ainda."));
  }
  for (const item of fila) {
    listaEl.append(montarItem({ texto: item.texto, anonima: item.anonima, minha: true, criadaEm: item.criadaEm, respondida: false, posAula: false, apoios: 0, apoiadaPorMim: false }, { local: true }));
  }
  for (const duvida of duvidasAtuais) {
    listaEl.append(montarItem(duvida));
  }
}

function atualizarBannerOffline() {
  secaoBanner.hidden = navigator.onLine;
}

function atualizarEstadoEnvio(aula) {
  const cartao = formEnvio.closest(".duvidas-card");
  const agora = Date.now() + diferencaRelogio;
  const foraDoPrazo = aula.situacao === "ENCERRADA"
    || (aula.situacao === "POS_AULA" && aula.prazoPosAula && agora > Date.parse(aula.prazoPosAula));

  if (foraDoPrazo) {
    areaEnvio.hidden = true;
    cartao._encerrada.hidden = false;
    cartao._encerrada.querySelector("p").textContent = aula.prazoPosAula
      ? `O envio de dúvidas desta aula terminou em ${formatoPrazo(aula.prazoPosAula)}.`
      : "O envio de dúvidas para esta aula está encerrado.";
    return;
  }

  areaEnvio.hidden = false;
  cartao._encerrada.hidden = true;
  if (aula.situacao === "POS_AULA" && aula.prazoPosAula) {
    avisoPrazo.hidden = false;
    avisoPrazo.textContent = `A aula terminou. Você pode enviar dúvidas até ${formatoPrazo(aula.prazoPosAula)}.`;
  } else {
    avisoPrazo.hidden = true;
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
  duvidasAtuais = estado.duvidas;
  atualizarEstadoEnvio(estado.aula);

  const chave = JSON.stringify(estado.duvidas) + lerFila().length;
  if (chave !== chaveRenderizada) {
    chaveRenderizada = chave;
    desenharLista();
  }

  if (navigator.onLine) void esvaziarFila();
}

async function iniciar() {
  await window.ParticipaShell.pronto;
  const usuario = await api("GET", "/api/eu");
  usuarioNome = usuario.nome;

  const main = document.querySelector(".app-content");

  secaoBanner = elemento("div", "duvidas-banner");
  secaoBanner.setAttribute("role", "status");
  secaoBanner.append(window.ParticipaIcones.criar("wifi-slash"), elemento("span", "", "Sem conexão. Dúvidas enviadas ficam salvas neste aparelho e seguem quando a conexão voltar."));
  main.prepend(secaoBanner);
  atualizarBannerOffline();
  window.addEventListener("online", atualizarBannerOffline);
  window.addEventListener("offline", atualizarBannerOffline);

  secaoDuvidas = elemento("section", "duvidas-raiz");
  secaoDuvidas.id = "duvidas";
  secaoDuvidas.setAttribute("aria-label", "Dúvidas da aula");
  secaoDuvidas.append(montarCartaoEnvio(), montarListaRaiz());
  main.append(secaoDuvidas);

  atualizarAvisoAnonimato();
  atualizarContador();

  const polling = window.ParticipaPolling.criar(atualizar, {
    intervaloMs: 3000,
    aoFalhar: (erro) => console.error(erro)
  });
  polling.iniciar({ imediato: true });
}

iniciar();

(() => {
  "use strict";

  const seletorFoco = 'a[href], button:not([disabled]), input:not([disabled]), select:not([disabled]), textarea:not([disabled]), [tabindex]:not([tabindex="-1"])';
  let temporizadorToast = null;
  let sequenciaDialogo = 0;

  function mostrarToast(mensagem, duracaoMs = 4000) {
    document.querySelector(".app-toast")?.remove();
    if (temporizadorToast !== null) window.clearTimeout(temporizadorToast);

    const toast = document.createElement("div");
    toast.className = "app-toast";
    toast.setAttribute("role", "status");
    toast.setAttribute("aria-live", "polite");
    if (window.ParticipaIcones) toast.append(window.ParticipaIcones.criar("check-circle"));
    const texto = document.createElement("span");
    texto.textContent = String(mensagem);
    toast.append(texto);
    document.body.append(toast);
    temporizadorToast = window.setTimeout(() => {
      toast.remove();
      temporizadorToast = null;
    }, Math.max(1000, duracaoMs));
    return toast;
  }

  function abrirDialogo({ titulo, conteudo = "", acoes = [] } = {}) {
    if (!titulo) throw new TypeError("O diálogo precisa de um título.");
    const focoAnterior = document.activeElement;
    const numero = ++sequenciaDialogo;
    const tituloId = `dialogo-titulo-${numero}`;
    const corpoId = `dialogo-corpo-${numero}`;

    const backdrop = document.createElement("div");
    backdrop.className = "dialog-backdrop";
    const dialogo = document.createElement("section");
    dialogo.className = "dialog";
    dialogo.setAttribute("role", "dialog");
    dialogo.setAttribute("aria-modal", "true");
    dialogo.setAttribute("aria-labelledby", tituloId);
    dialogo.setAttribute("aria-describedby", corpoId);
    dialogo.tabIndex = -1;

    const tituloEl = document.createElement("h2");
    tituloEl.className = "dialog-title";
    tituloEl.id = tituloId;
    tituloEl.textContent = String(titulo);
    const corpo = document.createElement("div");
    corpo.className = "dialog-body";
    corpo.id = corpoId;
    if (conteudo instanceof Node) corpo.append(conteudo);
    else corpo.textContent = String(conteudo);

    const areaAcoes = document.createElement("div");
    areaAcoes.className = "dialog-actions";
    for (const acao of acoes) {
      const botao = document.createElement("button");
      botao.type = "button";
      botao.className = `btn ${acao.classe || "btn-secondary"}`;
      botao.textContent = String(acao.rotulo || "OK");
      botao.addEventListener("click", () => acao.aoClicar?.({ fechar }));
      areaAcoes.append(botao);
    }

    dialogo.append(tituloEl, corpo);
    if (acoes.length) dialogo.append(areaAcoes);
    backdrop.append(dialogo);

    function fechar() {
      dialogo.removeEventListener("keydown", aoTeclar);
      backdrop.remove();
      if (focoAnterior instanceof HTMLElement && focoAnterior.isConnected) focoAnterior.focus();
    }

    function aoTeclar(evento) {
      if (evento.key === "Escape") {
        evento.preventDefault();
        fechar();
        return;
      }
      if (evento.key !== "Tab") return;
      const focaveis = [...dialogo.querySelectorAll(seletorFoco)].filter((item) => item.offsetParent !== null);
      if (!focaveis.length) {
        evento.preventDefault();
        dialogo.focus();
      } else if (evento.shiftKey && (document.activeElement === focaveis[0] || document.activeElement === dialogo)) {
        evento.preventDefault();
        focaveis.at(-1).focus();
      } else if (!evento.shiftKey && document.activeElement === focaveis.at(-1)) {
        evento.preventDefault();
        focaveis[0].focus();
      }
    }

    dialogo.addEventListener("keydown", aoTeclar);
    document.body.append(backdrop);
    requestAnimationFrame(() => (dialogo.querySelector(seletorFoco) || dialogo).focus());
    return Object.freeze({ elemento: dialogo, fechar });
  }

  window.ParticipaUI = Object.freeze({ mostrarToast, abrirDialogo });
})();

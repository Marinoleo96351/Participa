(() => {
  "use strict";

  function criar(tarefa, opcoes = {}) {
    if (typeof tarefa !== "function") throw new TypeError("O polling precisa receber uma função.");
    const intervaloMs = Number.isFinite(opcoes.intervaloMs) ? Math.max(250, opcoes.intervaloMs) : 3000;
    const aoFalhar = typeof opcoes.aoFalhar === "function" ? opcoes.aoFalhar : console.error;
    let temporizador = null;
    let executando = false;

    async function executar() {
      if (executando || document.hidden || !navigator.onLine) return;
      executando = true;
      try {
        await tarefa();
      } catch (erro) {
        aoFalhar(erro);
      } finally {
        executando = false;
      }
    }

    function iniciar({ imediato = false } = {}) {
      if (temporizador !== null) return;
      if (imediato) void executar();
      temporizador = window.setInterval(executar, intervaloMs);
      window.addEventListener("online", executar);
    }

    function parar() {
      if (temporizador === null) return;
      window.clearInterval(temporizador);
      temporizador = null;
      window.removeEventListener("online", executar);
    }

    return Object.freeze({ iniciar, parar, executar, ativo: () => temporizador !== null });
  }

  window.ParticipaPolling = Object.freeze({ criar });
})();

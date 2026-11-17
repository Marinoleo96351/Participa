(() => {
  "use strict";

  async function requisitar(caminho, opcoes = {}) {
    const url = new URL(caminho, window.location.origin);
    if (url.origin !== window.location.origin || !url.pathname.startsWith("/api/")) {
      throw new Error("A API do Participa só pode ser chamada no mesmo servidor.");
    }

    const cabecalhos = new Headers(opcoes.headers || {});
    cabecalhos.set("Accept", "application/json");
    let corpo = opcoes.body;
    if (corpo !== undefined && typeof corpo !== "string" && !(corpo instanceof FormData)) {
      cabecalhos.set("Content-Type", "application/json");
      corpo = JSON.stringify(corpo);
    }

    const resposta = await fetch(`${url.pathname}${url.search}`, {
      ...opcoes,
      body: corpo,
      headers: cabecalhos,
      credentials: "same-origin"
    });

    if (resposta.status === 204) return null;
    const tipo = resposta.headers.get("content-type") || "";
    const dados = tipo.includes("application/json") ? await resposta.json() : await resposta.text();
    if (!resposta.ok) {
      const erro = new Error(`A solicitação falhou (${resposta.status}).`);
      erro.status = resposta.status;
      erro.dados = dados;
      throw erro;
    }
    return dados;
  }

  window.ParticipaAPI = Object.freeze({ requisitar });
})();

import { api } from "/js/api.js";
import { abrirDestinoDoUsuario } from "/js/fluxo-acesso.js";

const botao = document.querySelector("#continuar-provisorio");
const erro = document.querySelector("#erro-provisorio");
const etapa = document.body.dataset.etapa;

try {
  const usuario = await api("GET", "/api/eu", undefined, { redirecionar401: false });
  if (etapa === "orientacao" && !sessionStorage.getItem("participa.termo-provisorio") && !usuario.aceiteTermo) {
    window.location.replace("/termo.html");
  }

  botao?.addEventListener("click", async () => {
    botao.disabled = true;
    if (etapa === "termo") {
      sessionStorage.setItem("participa.termo-provisorio", "1");
      window.location.assign("/orientacao.html");
      return;
    }
    sessionStorage.removeItem("participa.termo-provisorio");
    sessionStorage.setItem("participa.etapas-provisorias-vistas", "1");
    await abrirDestinoDoUsuario(usuario, { pularPaginasProvisorias: true });
  });
} catch (falha) {
  if (falha.status === 401) window.location.replace("/login.html");
  else if (erro) {
    erro.textContent = falha.message || "Não foi possível continuar.";
    erro.hidden = false;
  }
}

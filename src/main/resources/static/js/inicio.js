import { api } from "/js/api.js";
import { abrirDestinoDoUsuario } from "/js/fluxo-acesso.js";

const estado = document.querySelector("#estado-inicio");

try {
  const usuario = await api("GET", "/api/eu", undefined, { redirecionar401: false });
  await abrirDestinoDoUsuario(usuario);
} catch (falha) {
  if (falha.status === 401) {
    window.location.replace("/login.html");
  } else if (estado) {
    estado.textContent = falha.message || "Não foi possível abrir o Participa. Atualize a página para tentar novamente.";
    estado.hidden = false;
  }
}

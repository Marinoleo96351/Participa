import { api } from "/js/api.js";

const chaveDestino = "participa.destinoAposLogin";

function destinoSalvo() {
  const destino = sessionStorage.getItem(chaveDestino);
  if (!destino || !destino.startsWith("/") || destino.startsWith("//")) return null;
  return destino;
}

export async function abrirDestinoDoUsuario(usuario, { pularPaginasProvisorias = false } = {}) {
  if (!pularPaginasProvisorias && !usuario.aceiteTermo) {
    window.location.assign("/termo.html");
    return;
  }
  if (!pularPaginasProvisorias && !usuario.orientacaoVista) {
    window.location.assign("/orientacao.html");
    return;
  }

  const destino = destinoSalvo();
  if (destino) {
    sessionStorage.removeItem(chaveDestino);
    window.location.assign(destino);
    return;
  }

  if (usuario.perfil === "PROFESSOR") {
    window.location.assign("/professor/turmas.html");
    return;
  }
  if (usuario.perfil === "MONITOR") {
    window.location.assign("/moderacao.html");
    return;
  }

  const aula = await api("GET", "/api/eu/aula-atual");
  if (aula) {
    window.location.assign(`/aula.html?aulaId=${encodeURIComponent(aula.id)}`);
  } else {
    window.location.assign("/entrar.html");
  }
}

export async function sincronizarUsuarioLogado() {
  await window.ParticipaAPI.atualizarTokenCsrf();
  return api("GET", "/api/eu");
}

export function salvarDestinoAposLogin(destino) {
  if (destino?.startsWith("/") && !destino.startsWith("//")) {
    sessionStorage.setItem(chaveDestino, destino);
  }
}

export function aoCarregar(callback) {
  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", callback, { once: true });
  } else {
    callback();
  }
}

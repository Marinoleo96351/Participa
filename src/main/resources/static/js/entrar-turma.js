import { api } from "/js/api.js";
import { abrirDestinoDoUsuario, salvarDestinoAposLogin, aoCarregar } from "/js/fluxo-acesso.js";

aoCarregar(async () => {
  const formulario = document.querySelector("#form-entrar-turma");
  const campo = document.querySelector("#codigo-turma");
  const mensagem = document.querySelector("#erro-codigo");
  const status = document.querySelector("#status-entrada");
  const botao = document.querySelector("#enviar-codigo");
  const destino = `${window.location.pathname}${window.location.search}`;
  const parametro = new URLSearchParams(window.location.search).get("codigo");
  if (parametro) campo.value = parametro.toUpperCase().replace(/[^A-Z0-9]/g, "").slice(0, 6);

  function mostrarErro(texto = "") {
    mensagem.textContent = texto;
    mensagem.hidden = !texto;
  }

  campo.addEventListener("input", () => {
    campo.value = campo.value.toUpperCase().replace(/[^A-Z0-9]/g, "").slice(0, 6);
    mostrarErro();
  });

  try {
    const usuario = await api("GET", "/api/eu", undefined, { redirecionar401: false });
    if (!usuario.aceiteTermo || !usuario.orientacaoVista) {
      salvarDestinoAposLogin(destino);
      await abrirDestinoDoUsuario(usuario);
      return;
    }
  } catch (falha) {
    if (falha.status === 401) {
      salvarDestinoAposLogin(destino);
      window.location.assign("/login.html");
      return;
    }
    mostrarErro(falha.message);
  }

  formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    mostrarErro();
    status.textContent = "";
    const codigo = campo.value.trim().toUpperCase();
    if (codigo.length !== 6) {
      mostrarErro("O código tem 6 caracteres.");
      campo.focus();
      return;
    }

    botao.disabled = true;
    botao.textContent = "Entrando na turma…";
    try {
      const turma = await api("POST", "/api/turmas/entrar", { codigo });
      const aula = await api("GET", "/api/eu/aula-atual");
      if (aula && aula.turmaId === turma.id) {
        sessionStorage.removeItem("participa.destinoAposLogin");
        window.location.assign(`/aula.html?aulaId=${encodeURIComponent(aula.id)}`);
        return;
      }
      status.textContent = `Você entrou em ${turma.nome}. A aula aparecerá aqui quando o professor abrir uma.`;
    } catch (falha) {
      mostrarErro(falha.message || "Não foi possível entrar na turma.");
    } finally {
      botao.disabled = false;
      botao.textContent = "Entrar na aula";
    }
  });
});

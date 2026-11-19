import { api } from "/js/api.js";
import { abrirDestinoDoUsuario, aoCarregar, sincronizarUsuarioLogado } from "/js/fluxo-acesso.js";

aoCarregar(() => {
  const formulario = document.querySelector("#form-acesso");
  if (!formulario) return;

  const campoNome = document.querySelector("#nome");
  const campoSenha = document.querySelector("#senha");
  const nomeField = document.querySelector("#campo-nome");
  const perfilField = document.querySelector("#campo-perfil");
  const erro = document.querySelector("#erro-acesso");
  const botaoEnviar = document.querySelector("#enviar-acesso");
  const modoRadios = [...document.querySelectorAll("input[name='modo']")];

  function mostrarErro(mensagem = "") {
    erro.textContent = mensagem;
    erro.hidden = !mensagem;
  }

  function atualizarModo() {
    const cadastro = document.querySelector("input[name='modo']:checked").value === "cadastro";
    nomeField.hidden = !cadastro;
    perfilField.hidden = !cadastro;
    campoNome.required = cadastro;
    campoSenha.autocomplete = cadastro ? "new-password" : "current-password";
    botaoEnviar.textContent = cadastro ? "Criar conta" : "Entrar";
    mostrarErro();
  }

  modoRadios.forEach((radio) => radio.addEventListener("change", atualizarModo));
  formulario.addEventListener("submit", async (evento) => {
    evento.preventDefault();
    mostrarErro();
    if (!formulario.reportValidity()) return;

    const valores = new FormData(formulario);
    const email = String(valores.get("email")).trim();
    const senha = String(valores.get("senha"));
    const cadastro = valores.get("modo") === "cadastro";
    botaoEnviar.disabled = true;
    botaoEnviar.textContent = cadastro ? "Criando conta…" : "Entrando…";

    try {
      if (cadastro) {
        await api("POST", "/api/auth/cadastro", {
          nome: String(valores.get("nome")).trim(),
          email,
          senha,
          perfil: valores.get("perfil")
        }, { redirecionar401: false });
      }

      await api("POST", "/api/auth/login", { email, senha }, { redirecionar401: false });
      const usuario = await sincronizarUsuarioLogado();
      await abrirDestinoDoUsuario(usuario);
    } catch (falha) {
      mostrarErro(falha.message || "Não foi possível concluir o acesso. Tente novamente.");
      botaoEnviar.disabled = false;
      botaoEnviar.textContent = cadastro ? "Criar conta" : "Entrar";
    }
  });

  atualizarModo();
  api("GET", "/api/eu", undefined, { redirecionar401: false })
    .then((usuario) => abrirDestinoDoUsuario(usuario))
    .catch((falha) => {
      if (falha.status !== 401) mostrarErro(falha.message);
    });
});

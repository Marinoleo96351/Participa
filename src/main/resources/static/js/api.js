const apiBase = window.location.origin;
const rotasDeAutenticacao = new Set(["/api/auth/login", "/api/auth/cadastro"]);

function lerCookie(nome) {
  const prefixo = `${nome}=`;
  const parte = document.cookie.split(";").map((item) => item.trim()).find((item) => item.startsWith(prefixo));
  return parte ? decodeURIComponent(parte.slice(prefixo.length)) : null;
}

async function prepararTokenCsrf({ atualizar = false } = {}) {
  let token = atualizar ? null : lerCookie("XSRF-TOKEN");
  if (!token) {
    await fetch("/api/eu", { credentials: "same-origin", headers: { Accept: "application/json" } });
    token = lerCookie("XSRF-TOKEN");
  }
  return token;
}

export async function api(metodo, caminho, corpo, opcoes = {}) {
  const url = new URL(caminho, apiBase);
  if (url.origin !== apiBase || !url.pathname.startsWith("/api/")) {
    throw new Error("A API do Participa só pode ser chamada no mesmo servidor.");
  }

  const verbo = String(metodo || "GET").toUpperCase();
  const { redirecionar401, ...opcoesFetch } = opcoes;
  const cabecalhos = new Headers(opcoes.headers || {});
  cabecalhos.set("Accept", "application/json");
  const configuracao = {
    credentials: "same-origin",
    ...opcoesFetch,
    method: verbo,
    headers: cabecalhos
  };

  if (corpo !== undefined) {
    cabecalhos.set("Content-Type", "application/json");
    configuracao.body = JSON.stringify(corpo);
  }

  if (!["GET", "HEAD", "OPTIONS"].includes(verbo)) {
    const token = await prepararTokenCsrf();
    if (!token) throw new Error("Não foi possível obter o token de segurança. Atualize a página e tente novamente.");
    cabecalhos.set("X-XSRF-TOKEN", token);
  }

  const resposta = await fetch(`${url.pathname}${url.search}`, configuracao);
  if (resposta.status === 204) return null;

  const tipo = resposta.headers.get("content-type") || "";
  const dados = tipo.includes("application/json") ? await resposta.json() : await resposta.text();
  if (!resposta.ok) {
    const mensagem = typeof dados === "object" && dados !== null && dados.erro
      ? dados.erro
      : `A solicitação falhou (${resposta.status}).`;
    const erro = new Error(mensagem);
    erro.status = resposta.status;
    erro.dados = dados;

    if (resposta.status === 401 && redirecionar401 !== false && !rotasDeAutenticacao.has(url.pathname)) {
      window.location.assign("/login.html");
    }
    if (resposta.status === 403 && dados?.erro === "aceite-pendente") {
      window.location.assign("/termo.html");
    }
    throw erro;
  }
  return dados;
}

export function requisitar(caminho, opcoes = {}) {
  const { method = "GET", body, redirecionar401, headers, ...resto } = opcoes;
  return api(method, caminho, body, { ...resto, headers, redirecionar401 });
}

window.ParticipaAPI = Object.freeze({ api, requisitar, atualizarTokenCsrf: () => prepararTokenCsrf({ atualizar: true }) });

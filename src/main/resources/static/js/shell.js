(() => {
  "use strict";

  const menus = {
    estudante: [
      { pagina: "aula", rotulo: "Aula ao vivo", icone: "broadcast", href: "/pages/estudante/aula.html" },
      { pagina: "historico", rotulo: "Meu histórico", icone: "clock-counter-clockwise", href: "/pages/estudante/historico.html" },
      { pagina: "entrar-turma", rotulo: "Entrar em turma", icone: "users-three", href: "/pages/estudante/entrar-turma.html" },
      { pagina: "ajuda", rotulo: "Ajuda", icone: "question", href: "/pages/estudante/ajuda.html" },
      { pagina: "sair", rotulo: "Sair", icone: "sign-out", href: "/pages/acesso.html" }
    ],
    professor: [
      { pagina: "turmas", rotulo: "Turmas e aulas", icone: "chalkboard-teacher", href: "/pages/professor/turmas.html" },
      { pagina: "aula", rotulo: "Aula ao vivo", icone: "broadcast", href: "/pages/professor/aula.html" },
      { pagina: "painel", rotulo: "Painel por tema", icone: "kanban", href: "/pages/professor/painel.html" },
      { pagina: "moderacao", rotulo: "Moderação", icone: "shield-check", href: "/pages/professor/moderacao.html" },
      { pagina: "resumo", rotulo: "Resumo", icone: "file-text", href: "/pages/professor/resumo.html" }
    ],
    monitor: [
      { pagina: "moderacao", rotulo: "Moderação", icone: "shield-check", href: "/pages/monitor/moderacao.html" }
    ]
  };

  const nomesPerfil = { estudante: "Estudante", professor: "Professor", monitor: "Monitor" };

  function elemento(tag, classe, texto) {
    const node = document.createElement(tag);
    if (classe) node.className = classe;
    if (texto !== undefined) node.textContent = texto;
    return node;
  }

  function linkMenu(item, paginaAtual) {
    const link = elemento("a", "app-nav-link");
    link.href = item.href;
    if (item.pagina === paginaAtual) link.setAttribute("aria-current", "page");
    link.append(window.ParticipaIcones.criar(item.icone, { classe: "app-nav-icon" }));
    link.append(elemento("span", "", item.rotulo));
    return link;
  }

  function renderizar(raiz) {
    const perfil = raiz.dataset.profile || "estudante";
    const paginaAtual = raiz.dataset.page || "aula";
    const menu = menus[perfil];
    if (!menu) throw new Error(`Perfil de shell desconhecido: ${perfil}`);
    const pagina = menu.find((item) => item.pagina === paginaAtual);
    if (!pagina) throw new Error(`Página ${paginaAtual} não pertence ao perfil ${perfil}.`);

    raiz.className = "app-root";
    raiz.replaceChildren();

    const cabecalho = elemento("header", "app-header");
    const marca = elemento("a", "app-brand");
    marca.href = "/";
    marca.setAttribute("aria-label", "Participa — início");
    const marcaIcone = elemento("span", "app-brand-mark");
    marcaIcone.append(window.ParticipaIcones.criar("chats-circle"));
    marca.append(marcaIcone, elemento("span", "", "Participa"));
    const turma = elemento("span", "tag tag-neutral app-header-class", "Turma não selecionada");
    const perfilLabel = elemento("span", "app-user-label", nomesPerfil[perfil]);
    cabecalho.append(marca, turma, perfilLabel);

    const layout = elemento("div", "app-layout");
    const aside = elemento("aside", "app-sidebar");
    const nav = elemento("nav", "app-nav");
    nav.setAttribute("aria-label", "Navegação principal");
    menu.forEach((item) => nav.append(linkMenu(item, paginaAtual)));
    aside.append(nav);

    const main = elemento("main", "app-content");
    main.id = "conteudo-principal";
    main.tabIndex = -1;
    main.setAttribute("aria-labelledby", "titulo-pagina");
    main.append(elemento("p", "app-page-kicker", `Área do ${perfil}`));
    const titulo = elemento("h1", "app-page-title", pagina.rotulo);
    titulo.id = "titulo-pagina";
    main.append(titulo);
    const bloco = elemento("section", "app-empty-block");
    bloco.setAttribute("aria-label", "Área reservada para a etapa de desenvolvimento desta tela.");
    main.append(bloco);

    layout.append(aside, main);
    raiz.append(cabecalho, layout);
    document.title = `${pagina.rotulo} — Participa`;
  }

  function iniciar() {
    document.querySelectorAll("[data-participa-shell]").forEach(renderizar);
  }

  window.ParticipaShell = Object.freeze({ iniciar, renderizar, menus });
  iniciar();
})();

(() => {
  "use strict";

  const svgNamespace = "http://www.w3.org/2000/svg";
  const nomes = new Set([
    "chats-circle", "chalkboard-teacher", "clock-counter-clockwise", "key",
    "question", "sign-out", "users-three", "broadcast", "kanban",
    "shield-check", "file-text", "paper-plane-tilt", "mask-happy",
    "identification-badge", "hand-heart", "hand-heart-fill",
    "thermometer-simple", "check-circle", "circle-half", "x-circle",
    "wifi-slash", "lock-simple", "database", "chart-bar", "trash", "eye",
    "eye-slash", "check", "x", "plus", "play", "projector-screen",
    "printer", "envelope-simple", "hourglass-medium", "arrow-right"
  ]);

  function criar(nome, opcoes = {}) {
    const id = opcoes.preenchido && nome === "hand-heart" ? "hand-heart-fill" : nome;
    if (!nomes.has(id)) throw new Error(`Ícone não incluído no sprite local: ${id}`);

    const svg = document.createElementNS(svgNamespace, "svg");
    svg.setAttribute("class", ["icon", opcoes.classe].filter(Boolean).join(" "));
    svg.setAttribute("viewBox", "0 0 256 256");
    svg.setAttribute("focusable", "false");

    if (opcoes.rotulo) {
      svg.setAttribute("role", "img");
      svg.setAttribute("aria-label", opcoes.rotulo);
    } else {
      svg.setAttribute("aria-hidden", "true");
    }

    const use = document.createElementNS(svgNamespace, "use");
    use.setAttribute("href", `/icons/icons.svg#${id}`);
    svg.append(use);
    return svg;
  }

  window.ParticipaIcones = Object.freeze({ criar, nomes: Object.freeze([...nomes]) });
})();

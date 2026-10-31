(function () {
  "use strict";

  var DIGITS = "0123456789";
  var ALPHANUMERIC = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  var ASCII_OFFSET = 48;
  var COMPANY_BRANCH = "0001";

  function characterValue(character) {
    return character.charCodeAt(0) - ASCII_OFFSET;
  }

  function checkDigitFromSum(sum) {
    var remainder = sum % 11;
    return remainder < 2 ? "0" : String(11 - remainder);
  }

  function descendingWeightedDigit(base) {
    var sum = 0;
    for (var index = 0; index < base.length; index++) {
      sum += characterValue(base.charAt(index)) * (base.length + 1 - index);
    }
    return checkDigitFromSum(sum);
  }

  function cyclingWeightedDigit(base) {
    var sum = 0;
    for (var index = 0; index < base.length; index++) {
      var weight = ((base.length - 1 - index) % 8) + 2;
      sum += characterValue(base.charAt(index)) * weight;
    }
    return checkDigitFromSum(sum);
  }

  function withCheckDigits(base, digitOf) {
    var withFirst = base + digitOf(base);
    return withFirst + digitOf(withFirst);
  }

  function randomFrom(alphabet, length) {
    var out = "";
    for (var index = 0; index < length; index++) {
      out += alphabet.charAt(Math.floor(Math.random() * alphabet.length));
    }
    return out;
  }

  function generateCpf() {
    return withCheckDigits(randomFrom(DIGITS, 9), descendingWeightedDigit);
  }

  function generateNumericCnpj() {
    return withCheckDigits(randomFrom(DIGITS, 8) + COMPANY_BRANCH, cyclingWeightedDigit);
  }

  function generateAlphanumericCnpj() {
    return withCheckDigits(randomFrom(ALPHANUMERIC, 8) + COMPANY_BRANCH, cyclingWeightedDigit);
  }

  function maskCpf(value) {
    return value.replace(/^(.{3})(.{3})(.{3})(.{2})$/, "$1.$2.$3-$4");
  }

  function maskCnpj(value) {
    return value.replace(/^(.{2})(.{3})(.{3})(.{4})(.{2})$/, "$1.$2.$3/$4-$5");
  }

  var GENERATORS = [
    { key: "cpf", generate: generateCpf, mask: maskCpf },
    { key: "cnpj", generate: generateNumericCnpj, mask: maskCnpj },
    { key: "cnpjAlphanumeric", generate: generateAlphanumericCnpj, mask: maskCnpj }
  ];

  var SELF_TEST = [
    { base: "12ABC34501DE", digitOf: cyclingWeightedDigit, expected: "12ABC34501DE35" },
    { base: "112223330001", digitOf: cyclingWeightedDigit, expected: "11222333000181" },
    { base: "529982247", digitOf: descendingWeightedDigit, expected: "52998224725" }
  ];

  function selfTestFailures() {
    return SELF_TEST
      .map(function (vector) {
        var actual = withCheckDigits(vector.base, vector.digitOf);
        return actual === vector.expected ? null : vector.expected + " != " + actual;
      })
      .filter(function (failure) {
        return failure !== null;
      });
  }

  function labels() {
    return (window.swaggerTestDataLabels || {});
  }

  function label(key, fallback) {
    return labels()[key] || fallback;
  }

  function copy(text, button) {
    var restore = button.textContent;
    var done = function () {
      button.textContent = label("copied", "copied");
      window.setTimeout(function () {
        button.textContent = restore;
      }, 1200);
    };
    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(text).then(done, done);
      return;
    }
    var holder = document.createElement("textarea");
    holder.value = text;
    document.body.appendChild(holder);
    holder.select();
    document.execCommand("copy");
    document.body.removeChild(holder);
    done();
  }

  function buildRow(generator, state) {
    var row = document.createElement("div");
    row.className = "swagger-test-data__row";

    var name = document.createElement("span");
    name.className = "swagger-test-data__name";
    name.textContent = label(generator.key, generator.key);

    var value = document.createElement("code");
    value.className = "swagger-test-data__value";

    var copyButton = document.createElement("button");
    copyButton.type = "button";
    copyButton.className = "swagger-test-data__button";
    copyButton.textContent = label("copy", "copy");

    var render = function () {
      var raw = generator.generate();
      value.dataset.raw = raw;
      value.textContent = state.masked ? generator.mask(raw) : raw;
    };

    copyButton.addEventListener("click", function () {
      copy(state.masked ? generator.mask(value.dataset.raw) : value.dataset.raw, copyButton);
    });

    value.addEventListener("click", function () {
      state.masked = !state.masked;
      state.onToggle();
    });
    value.title = label("toggleHint", "click to switch between masked and raw");

    row.appendChild(name);
    row.appendChild(value);
    row.appendChild(copyButton);

    return { element: row, render: render };
  }

  function buildPanel() {
    var panel = document.createElement("section");
    panel.className = "swagger-test-data";

    var heading = document.createElement("header");
    heading.className = "swagger-test-data__heading";

    var title = document.createElement("h2");
    title.textContent = label("title", "Test data");

    var regenerate = document.createElement("button");
    regenerate.type = "button";
    regenerate.className = "swagger-test-data__button swagger-test-data__button--primary";
    regenerate.textContent = label("regenerate", "Generate new");

    heading.appendChild(title);
    heading.appendChild(regenerate);
    panel.appendChild(heading);

    var state = { masked: true, onToggle: function () {} };
    var rows = GENERATORS.map(function (generator) {
      var row = buildRow(generator, state);
      panel.appendChild(row.element);
      return row;
    });

    var renderAll = function () {
      rows.forEach(function (row) {
        row.render();
      });
    };
    state.onToggle = renderAll;
    regenerate.addEventListener("click", renderAll);
    renderAll();

    var failures = selfTestFailures();
    if (failures.length > 0) {
      var warning = document.createElement("p");
      warning.className = "swagger-test-data__warning";
      warning.textContent = label("selfTestFailed", "Check-digit self-test failed") + ": " + failures.join("; ");
      panel.appendChild(warning);
    }

    var note = document.createElement("p");
    note.className = "swagger-test-data__note";
    note.textContent = label("note", "Structurally valid, not registered with any authority.");
    panel.appendChild(note);

    return panel;
  }

  function styles() {
    return [
      ".swagger-test-data{font-family:sans-serif;max-width:1460px;margin:16px auto 0;padding:14px 20px;",
      "background:#fff;border:1px solid #d9dde3;border-radius:6px;box-sizing:border-box}",
      ".swagger-test-data__heading{display:flex;align-items:center;justify-content:space-between;",
      "gap:12px;flex-wrap:wrap;margin-bottom:10px}",
      ".swagger-test-data h2{font-size:15px;margin:0;color:#3b4151;font-weight:700}",
      ".swagger-test-data__row{display:flex;align-items:center;gap:12px;flex-wrap:wrap;padding:5px 0}",
      ".swagger-test-data__name{min-width:170px;font-size:13px;color:#3b4151}",
      ".swagger-test-data__value{font-family:monospace;font-size:14px;background:#f3f4f6;color:#1f2933;",
      "border:1px solid #e1e4e8;border-radius:4px;padding:4px 10px;cursor:pointer;user-select:all}",
      ".swagger-test-data__button{font-size:12px;padding:4px 12px;border:1px solid #c3c9d1;",
      "border-radius:4px;background:#fff;color:#3b4151;cursor:pointer}",
      ".swagger-test-data__button:hover{background:#f3f4f6}",
      ".swagger-test-data__button--primary{border-color:#4990e2;color:#4990e2;font-weight:600}",
      ".swagger-test-data__note{font-size:11px;color:#6b7280;margin:10px 0 0}",
      ".swagger-test-data__warning{font-size:12px;color:#b91c1c;margin:10px 0 0;font-weight:600}",
      "@media (prefers-color-scheme:dark){",
      ".swagger-test-data{background:#1f2430;border-color:#3a4050}",
      ".swagger-test-data h2,.swagger-test-data__name{color:#e6e9ef}",
      ".swagger-test-data__value{background:#141821;color:#e6e9ef;border-color:#3a4050}",
      ".swagger-test-data__button{background:#1f2430;color:#e6e9ef;border-color:#3a4050}}"
    ].join("");
  }

  function install() {
    if (document.querySelector(".swagger-test-data")) {
      return;
    }
    var anchor = document.querySelector("#swagger-ui");
    if (!anchor) {
      return;
    }
    var style = document.createElement("style");
    style.textContent = styles();
    document.head.appendChild(style);
    anchor.parentNode.insertBefore(buildPanel(), anchor);
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", install);
  } else {
    install();
  }
})();

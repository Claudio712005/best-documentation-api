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

  var DOCUMENT_KINDS = {
    cpf: { generate: generateCpf, mask: maskCpf, keys: ["cpf"] },
    cnpj: { generate: generateNumericCnpj, mask: maskCnpj, keys: ["cnpj"] },
    cnpjAlphanumeric: { generate: generateAlphanumericCnpj, mask: maskCnpj, keys: ["cnpj"] }
  };

  var TAX_ID_KEYS = ["taxId", "taxIdentifier", "cpf", "cnpj", "document"];

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
    return window.swaggerTestDataLabels || {};
  }

  function label(key, fallback) {
    return labels()[key] || fallback;
  }

  var captured = {};
  var captureSubscribers = [];

  function publishCapture(name, value) {
    if (captured[name] === value) {
      return;
    }
    captured[name] = value;
    captureSubscribers.forEach(function (subscriber) {
      subscriber(name, value);
    });
  }

  function onCaptureChange(subscriber) {
    captureSubscribers.push(subscriber);
  }

  function readJsonPath(body, expression) {
    if (!expression || expression.charAt(0) !== "$") {
      return undefined;
    }
    var segments = expression.replace(/^\$\.?/, "").split(".").filter(Boolean);
    var cursor = body;
    for (var index = 0; index < segments.length; index++) {
      if (cursor === null || typeof cursor !== "object") {
        return undefined;
      }
      cursor = cursor[segments[index]];
    }
    return cursor === undefined || cursor === null ? undefined : String(cursor);
  }

  function extensionOf(system, path, method, name) {
    try {
      var value = system.specSelectors.specJson().getIn(["paths", path, method, name]);
      return value && value.toJS ? value.toJS() : value || null;
    } catch (error) {
      return null;
    }
  }

  function parsedResponseBody(response) {
    var text = response.get("text");
    if (typeof text !== "string" || text.length === 0) {
      return null;
    }
    try {
      return JSON.parse(text);
    } catch (error) {
      return null;
    }
  }

  function harvestCaptures(system) {
    var spec = system.specSelectors.specJson();
    var paths = spec.getIn(["paths"]);
    if (!paths || !paths.forEach) {
      return;
    }
    paths.forEach(function (operations, path) {
      operations.forEach(function (operation, method) {
        if (!operation || !operation.getIn) {
          return;
        }
        var captures = extensionOf(system, path, method, "x-captures");
        if (!captures) {
          return;
        }
        var response = system.specSelectors.responseFor(path, method);
        if (!response) {
          return;
        }
        var status = Number(response.get("status"));
        if (!(status >= 200 && status < 300)) {
          return;
        }
        var body = parsedResponseBody(response);
        if (body === null) {
          return;
        }
        Object.keys(captures).forEach(function (name) {
          var value = readJsonPath(body, captures[name]);
          if (value !== undefined) {
            publishCapture(name, value);
          }
        });
      });
    });
  }

  function replaceTaxIdentifiers(body, kind) {
    var replaced = 0;
    var walk = function (node) {
      if (Array.isArray(node)) {
        node.forEach(walk);
        return;
      }
      if (node === null || typeof node !== "object") {
        return;
      }
      Object.keys(node).forEach(function (key) {
        var child = node[key];
        var isTaxIdHolder = TAX_ID_KEYS.indexOf(key) >= 0;
        if (isTaxIdHolder && typeof child === "string") {
          node[key] = kind.generate();
          replaced++;
          return;
        }
        if (isTaxIdHolder && child !== null && typeof child === "object" && typeof child.value === "string") {
          child.value = kind.generate();
          replaced++;
          return;
        }
        walk(child);
      });
    };
    walk(body);
    return replaced;
  }

  function copyToClipboard(text) {
    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(text);
      return;
    }
    var holder = document.createElement("textarea");
    holder.value = text;
    document.body.appendChild(holder);
    holder.select();
    document.execCommand("copy");
    document.body.removeChild(holder);
  }

  function styles() {
    return [
      ".test-data-actions{display:flex;align-items:center;gap:8px;flex-wrap:wrap;margin:6px 0 8px}",
      ".test-data-actions__label{font-size:11px;text-transform:uppercase;letter-spacing:.04em;color:#6b7280}",
      ".test-data-actions__button{font-size:11px;font-family:sans-serif;padding:3px 10px;border:1px solid #c3c9d1;",
      "border-radius:4px;background:#fff;color:#3b4151;cursor:pointer}",
      ".test-data-actions__button:hover{background:#f3f4f6;border-color:#4990e2;color:#4990e2}",
      ".test-data-actions__button[disabled]{opacity:.45;cursor:not-allowed}",
      ".test-data-actions__note{font-size:11px;color:#6b7280}",
      ".test-data-actions__warning{font-size:11px;color:#b91c1c;font-weight:600}",
      "@media (prefers-color-scheme:dark){",
      ".test-data-actions__button{background:#1f2430;color:#e6e9ef;border-color:#3a4050}",
      ".test-data-actions__label,.test-data-actions__note{color:#9aa4b2}}"
    ].join("");
  }

  function installStyles() {
    if (document.getElementById("test-data-actions-styles")) {
      return;
    }
    var style = document.createElement("style");
    style.id = "test-data-actions-styles";
    style.textContent = styles();
    document.head.appendChild(style);
  }

  function pathMethodOf(props) {
    if (Array.isArray(props.pathMethod)) {
      return props.pathMethod;
    }
    if (props.path && props.method) {
      return [props.path, props.method];
    }
    return null;
  }

  function buildPlugin(system) {
    var React = system.React;

    var button = function (text, title, onClick, disabled) {
      return React.createElement(
        "button",
        {
          type: "button",
          className: "test-data-actions__button",
          title: title,
          disabled: disabled === true,
          onClick: onClick
        },
        text);
    };

    var actionBar = function (children) {
      installStyles();
      return React.createElement("div", { className: "test-data-actions" }, children);
    };

    var ParameterRowWrapper = function (Original) {
      return function (props) {
        var original = React.createElement(Original, props);
        var pathMethod = pathMethodOf(props);
        var parameter = props.param;
        if (!pathMethod || !parameter || !parameter.get) {
          return original;
        }

        var parameterName = parameter.get("name");
        var parameterIn = parameter.get("in");
        var fills = extensionOf(system, pathMethod[0], pathMethod[1], "x-fills");
        if (!fills) {
          return original;
        }

        var captureName = Object.keys(fills).filter(function (name) {
          return fills[name] === parameterName;
        })[0];
        if (!captureName) {
          return original;
        }

        var value = captured[captureName];
        var apply = function () {
          system.specActions.changeParam(pathMethod, parameterName, parameterIn, value, false);
        };

        var actionRow = React.createElement(
          "tr",
          { className: "test-data-row", key: "test-data-row" },
          React.createElement("td", null),
          React.createElement(
            "td",
            null,
            actionBar([
              React.createElement(
                "span",
                { className: "test-data-actions__label", key: "label" },
                label("fillFromCapture", "captured id")),
              button(
                value ? value : label("noCapture", "nothing captured yet"),
                label("fillHint", "Use the identifier returned by the last successful creation"),
                apply,
                !value)
            ])));

        return React.createElement(
          React.Fragment,
          null,
          React.createElement(Original, Object.assign({ key: "parameter-row" }, props)),
          actionRow);
      };
    };

    var RequestBodyEditorWrapper = function (Original) {
      return function (props) {
        var original = React.createElement(Original, props);
        var pathMethod = pathMethodOf(props);
        if (!pathMethod) {
          return original;
        }

        var currentValue = function () {
          if (typeof props.value === "string" && props.value.length > 0) {
            return props.value;
          }
          try {
            return system.oas3Selectors.requestBodyValue(pathMethod[0], pathMethod[1]) || "";
          } catch (error) {
            return "";
          }
        };

        var write = function (text) {
          if (typeof props.onChange === "function") {
            props.onChange(text);
            return;
          }
          system.oas3Actions.setRequestBodyValue({ value: text, pathMethod: pathMethod });
        };

        var fillDocument = function (kindName) {
          var kind = DOCUMENT_KINDS[kindName];
          var raw = currentValue();
          var parsed = null;
          try {
            parsed = JSON.parse(typeof raw === "string" ? raw : String(raw));
          } catch (error) {
            parsed = null;
          }
          if (parsed === null) {
            copyToClipboard(kind.generate());
            return;
          }
          var replaced = replaceTaxIdentifiers(parsed, kind);
          if (replaced === 0) {
            copyToClipboard(kind.generate());
            return;
          }
          write(JSON.stringify(parsed, null, 2));
        };

        var failures = selfTestFailures();
        var children = [
          React.createElement(
            "span",
            { className: "test-data-actions__label", key: "label" },
            label("title", "Test data"))
        ];
        Object.keys(DOCUMENT_KINDS).forEach(function (kindName) {
          children.push(button(
            label(kindName, kindName),
            label("fillBodyHint", "Replace the tax identifiers in this body with freshly generated ones"),
            function () {
              fillDocument(kindName);
            }));
        });
        children.push(React.createElement(
          "span",
          { className: "test-data-actions__note", key: "note" },
          label("note", "")));
        if (failures.length > 0) {
          children.push(React.createElement(
            "span",
            { className: "test-data-actions__warning", key: "warning" },
            label("selfTestFailed", "Check-digit self-test failed") + ": " + failures.join("; ")));
        }

        return React.createElement(
          "div",
          { className: "test-data-body" },
          actionBar(children),
          original);
      };
    };

    return {
      wrapComponents: {
        parameterRow: ParameterRowWrapper,
        RequestBodyEditor: RequestBodyEditorWrapper
      },
      afterLoad: function (loaded) {
        var target = loaded || system;
        var store = target.getStore ? target.getStore() : null;
        if (!store || !store.subscribe) {
          return;
        }
        store.subscribe(function () {
          harvestCaptures(target);
        });
      }
    };
  }

  window.swaggerTestDataPlugin = buildPlugin;
  window.swaggerTestDataGenerators = {
    cpf: generateCpf,
    cnpj: generateNumericCnpj,
    cnpjAlphanumeric: generateAlphanumericCnpj,
    selfTestFailures: selfTestFailures
  };
})();

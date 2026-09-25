# wicket-console (library module)

The published artifact `ru.ydn.wicket.wicket-console:wicket-console` (jar only). Everything is in package
`ru.ydn.wicket.wicketconsole`. Root rules: [`../AGENTS.md`](../AGENTS.md); plan: [`../REFRESH_PLAN.md`](../REFRESH_PLAN.md).

## Classes

- Engine SPI: `IScriptEngine` (`getName`, `eval(command, IScriptContext)` → `ScriptResult`), `IScriptEngineFactory`
  (`createScriptEngine(name)`, `getSupportedEngines()`), `IScriptContext` (extra bindings per call, `Serializable`).
- `ScriptExecutor` — static list of factories (the built-in one wraps `javax.script.ScriptEngineManager`;
  `registerScriptEngineFactory` pushes to the front, so registered factories win), per-instance engine cache and history.
- `ScriptExecutorHolder` — application `MetaDataKey`; one `ScriptExecutor` per session id; drops it on session unbind.
- `EmbeddedScriptEngine` — JSR-223 adapter: one `ScriptContext` per engine instance with **engine-created bindings**
  (GraalJS keeps state only in its own bindings, plan D7), captures stdout/stderr, stores the last result as `$result`,
  tries `Java.asJSONCompatible` (Nashorn-only). For GraalJS (factory name `Graal.js`) it enables Java access through
  the `polyglot.js.allowHostAccess`/`allowHostClassLookup` bindings options, as Nashorn had it (plan D13).
- `ScriptResult` (script, engine, out, error, result model, start/finish/duration), `StorageModel` (keeps
  non-serializable results in a transient field).
- UI: `WicketConsolePanel` (form, engine selector, keep-script, history), `HistoryItemPanel` (+ "reuse" link),
  `ScriptResultRendererManager.INSTANCE` + `DefaultScriptResultRenderer`, `behavior/` (Ctrl+Enter submit,
  scroll-to-bottom, hide-if-empty). CSS: `src/main/resources/.../wicketconsole.css`; i18n: `WicketConsolePanel.properties`.
- `devutils/` — needs wicket-devutils (`provided`): `WicketConsoleDebugPanel`, `WicketConsolePage`.
- `Initializer` — registered in `META-INF/services/org.apache.wicket.IInitializer`.

## Dependencies

- Compile: wicket-core, wicket-extensions (not imported here; needed by wicket-devutils pages).
- `provided`: javax.servlet-api, wicket-devutils. Test: JUnit 4.
- Used directly but only transitive (via wicket-core): `com.github.openjson` (`EmbeddedScriptEngine`), `slf4j-api`.
- **No script engine and no logging binding** in compile/runtime scope — consumers bring them.

## Tests

- `TestWicketConsoleComponents` — 3 tests; `WicketTester` with a default `MockApplication`, so the
  `IInitializer` from `META-INF/services` installs `ScriptExecutorHolder`.

## Pitfalls

- Markup and resource files sit in `src/main/java` next to classes and in `src/main/resources`; both are copied.
- JVM-wide static registries (`ScriptExecutor`, `ScriptResultRendererManager.INSTANCE`): test order can matter.

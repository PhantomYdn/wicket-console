# wicket-console

An Ajax web console for Apache Wicket applications: type a script, pick a script engine, and get output, errors,
result and duration in a per-session history. Engines are pluggable (`IScriptEngineFactory`); by default every
`javax.script` (JSR-223) engine found on the classpath is offered. Also available as a Wicket DebugBar panel.
Used by [Orienteer](https://github.com/OrienteerBAP/Orienteer) (local checkout `~/Development/Orienteer`) and modernized
in lockstep with it. Dormant from Oct 2020 to Sep 2026. Repo: `PhantomYdn/wicket-console` (the owner's personal account).
**Read [`REFRESH_PLAN.md`](REFRESH_PLAN.md) before touching dependencies, build or infra; tick items off there in the same commit.**

## Contract with Orienteer — don't break it

- Coordinates `ru.ydn.wicket.wicket-console:wicket-console` (jar only, no test-jar), parent `wicket-console-parent`,
  version **`1.4-SNAPSHOT`** (= Stage A). Never change groupId/artifactId/version without the owner.
- Orienteer's `orienteer-devutils` (reaching users, logger-server, standalone) uses:
  - `ScriptExecutor.registerScriptEngineFactory(...)` for its own engines `ODBScriptEngine` ("SQL") and `ODBConsoleEngine`,
    implementing `IScriptEngine`/`IScriptEngineFactory` and filling `ScriptResult` (`setResult`, `setResultModel`, `setOut`, `setError`, `start`, `finish`);
  - `ScriptResultRendererManager.INSTANCE.registerRenderer(IScriptResultRenderer)`;
  - `new WicketConsolePanel(id, IScriptContext)` in a dashboard widget (bindings `db`, `orient`, `util` for JS);
  - `WicketConsoleDebugPanel.DEBUG_BAR_CONTRIB`, `WicketConsolePage` (mounted at `/wicket-console`, `setWicketConsolePageImplementation`);
  - `ScriptExecutorHolder.get().getScriptExecutor().executeWithoutHistory(...)` in its tests.
- Public API = public/protected members under `ru.ydn.wicket.wicketconsole`. Avoid signature changes; if one is
  unavoidable, list it in the plan's "Handoff to Orienteer".
- Stages: A → Orienteer P3 (JDK 21, Wicket 8.15, javax), B → P5 (Wicket 9), C → P6 (Wicket 10, jakarta).

## Stack (current → target)

| Area | Current | Target (see plan) |
|---|---|---|
| Java | `--release 21` (`maven.compiler.release`), built/tested on JDK 21 **and** 25 | stays 21 |
| Web | Wicket 8.15.0 (core, extensions; devutils `provided`), `javax.servlet-api` 3.1.0 `provided` | 9.x (B) → 10.x + jakarta Servlet 6 (C) |
| Scripts | `javax.script`, **no engine in the library** (plan D4); GraalJS gets Java access like Nashorn had (D13) | Orienteer brings GraalJS 25.0.4 |
| Tests | JUnit 4.13.2 + `WicketTester`; test-only JSR-223 engine `Calc` | Jupiter + vintage (B) |
| Other | openjson + SLF4J API (both via Wicket); no Lombok, Mockito, Guice | unchanged |

## Build status — read first

- **Stage A done locally (2026-09-25):** `./mvnw clean verify` green on Temurin 21 and 25 (library 7 tests, demo 4),
  no `--add-opens` needed; `./mvnw install` → jar + parent pom `1.4-SNAPSHOT`, class files major 65. Public API unchanged.
- **Versions (plan D8):** `master` = Stage A = `1.4-SNAPSHOT`; `1.4` is released after Orienteer P3 is green. Then
  B = `1.5-SNAPSHOT`, C = `2.0-SNAPSHOT`. **Never build or install Stage B/C work as `1.4-SNAPSHOT`.**
- Plugin versions live only in the root `<pluginManagement>`; enforcer requires Maven ≥ 3.9 and JDK ≥ 21, and bans
  logging bindings and script engines from the library's compile/runtime scope (D10).
- **Only the owner deploys, pushes or tags** — agents prepare and validate locally. OSSRH and jcenter/bintray are dead.
- JDK: Temurin 21.0.11 (default, `.sdkmanrc`) and 25.0.4 via SDKMAN (`~/.sdkman/candidates/java/25.0.4-tem`). JDK 27 is non-LTS: don't target it.

## Commands

```bash
./mvnw clean verify                                # build + tests (wrapper: Maven 3.9.16)
./mvnw install                                     # jar + parent pom into ~/.m2 for Orienteer
./mvnw -pl wicket-console test -Dtest=TestWicketConsoleComponents
./mvnw package jetty:run -pl wicket-console-demo -am -DskipTests         # demo on :8080 (GraalJS console), no install
JAVA_HOME=~/.sdkman/candidates/java/25.0.4-tem ./mvnw clean verify     # JDK 25 check
```

## Layout and key classes

- `wicket-console/` — the library (package `ru.ydn.wicket.wicketconsole`); has its own `AGENTS.md`.
  - `ScriptExecutor` (engine factories registry + per-session history), `ScriptExecutorHolder` (app metadata,
    executor per session id), `EmbeddedScriptEngine` (JSR-223 adapter), `ScriptResult`, `StorageModel`.
  - UI: `WicketConsolePanel`, `HistoryItemPanel`, `ScriptResultRendererManager` + `DefaultScriptResultRenderer`, `behavior/*`.
  - `devutils/` — `WicketConsoleDebugPanel` (DebugBar contributor), `WicketConsolePage` (extends `DevUtilsPage`).
  - `Initializer` (`META-INF/services/org.apache.wicket.IInitializer`) installs the holder and, if wicket-devutils is
    on the classpath, the DebugBar contributor.
- `wicket-console-demo/` — sample war (never published); has its own `AGENTS.md`.

## Conventions

- Tabs in Java/HTML/CSS; the poms mix tabs and spaces. Match the surrounding file. No reformat-only diffs.
- Markup (`.html`) lives next to its class in `src/main/java`; the pom copies non-Java files from there as resources.
- Logging: SLF4J API only. Never add a logging binding or logging config to the library — demo or tests only.
- Never add a script engine (GraalJS, Nashorn…) to the library's compile/runtime scope: consumers bring their own.
- Keep the library Wicket-only: no Guice, no servlet API beyond `provided`.

## Testing

- JUnit 4 + `WicketTester` (`TestWicketConsoleComponents`: execution, output/errors, state between commands,
  missing engine, default-engine fallback, page render, panel submit/clear via Ajax).
- Library tests must pass with **no JS engine** on the classpath: scripts use the test-only `calc.CalcScriptEngine`
  (registered via `src/test/resources/META-INF/services/javax.script.ScriptEngineFactory`), which, like GraalJS,
  keeps state only in bindings it created.
- Demo: `TestHomePage` (home page with panel + DebugBar) and `TestJavaScriptConsole` (real GraalJS 25.0.4).

## Pitfalls

- `ScriptExecutor` and `ScriptResultRendererManager` keep **static, JVM-wide** registries; tests share them.
- A missing engine yields a `ScriptResult` with an error (plan D5), never an exception; the panel preselects the first
  available engine when `"JavaScript"` is missing (D6). Keep both: JDK 15+ has no JS engine.
- `EmbeddedScriptEngine` must keep using `engine.createBindings()` (D7) and set GraalJS options before the first eval (D13).
- wicket-devutils is `provided` and optional: `Initializer` probes for `DebugBar` with `Class.forName`.
- Coupled libraries move together: Wicket ↔ servlet API ↔ jakarta ↔ Jetty (demo). Never bump one alone.

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
| Java | source/target 1.8 (compiler plugin 3.5.1) | `--release 21`, built/tested on JDK 21 **and** 25 |
| Web | Wicket 8.3.0 (core, extensions; devutils `provided`), `javax.servlet-api` 3.1.0 `provided` | 8.15 (A) → 9.x (B) → 10.x + jakarta Servlet 6 (C) |
| Scripts | `javax.script`; relied on the JDK's Nashorn (gone since JDK 15) | no JS engine in the library; Orienteer brings GraalJS |
| Tests | JUnit 4.12 + `WicketTester` | JUnit 4.13.2 (A) → Jupiter + vintage (B) |
| Other | openjson + SLF4J API (both via Wicket); no Lombok, Mockito, Guice | unchanged |

## Build status — read first

- Baseline 2026-09-25 on JDK 21 (`mvn clean verify`): compiles (source/target 8 warnings), but 2 of the 3 library tests
  fail with a `NullPointerException`: they evaluate `"JavaScript"`, and there is no JS engine on JDK 15+. Demo: 1 test, green.
- Publishing still points at OSSRH (oss.sonatype.org, dead since 2025) with nexus-staging; phase R replaces it with the
  Central Portal. **Only the owner deploys, pushes or tags** — agents prepare and validate locally.
- JDK: Temurin 21.0.11 (default) and 25.0.4 via SDKMAN (`~/.sdkman/candidates/java/25.0.4-tem`). JDK 27 is non-LTS: don't target it.

## Commands

```bash
mvn clean verify                                   # build + tests (becomes ./mvnw in Stage A)
mvn install                                        # jar + parent pom into ~/.m2 for Orienteer
mvn -pl wicket-console test -Dtest=TestWicketConsoleComponents
mvn install -DskipTests && mvn -pl wicket-console-demo jetty:run   # demo on :8080
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

- JUnit 4 + `WicketTester` (`TestWicketConsoleComponents`: execution, page render, panel submit/clear via Ajax).
- Tests must pass with **no JS engine** on the classpath (the JDK has none since 15).
- Demo: `TestHomePage` renders the home page with the panel and the DebugBar.

## Pitfalls

- `ScriptExecutor` and `ScriptResultRendererManager` keep **static, JVM-wide** registries; tests share them.
- `WicketConsolePanel` defaults to the `"JavaScript"` engine; without one, `ScriptExecutor.executeWithoutHistory`
  dereferences a null engine (fixed in Stage A, plan A2).
- wicket-devutils is `provided` and optional: `Initializer` probes for `DebugBar` with `Class.forName`.
- Coupled libraries move together: Wicket ↔ servlet API ↔ jakarta ↔ Jetty (demo). Never bump one alone.

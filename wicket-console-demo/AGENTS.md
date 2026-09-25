# wicket-console-demo

Sample war: `WicketApplication` with a `HomePage` that embeds `WicketConsolePanel` and the Wicket `DebugBar`
(which gets the console contributor). Root rules: [`../AGENTS.md`](../AGENTS.md); plan: [`../REFRESH_PLAN.md`](../REFRESH_PLAN.md).

- **Not published** and not used by Orienteer. It stays in the reactor while it builds (plan D9); parked only if it blocks a stage.
- **Run:** `./mvnw package jetty:run -pl wicket-console-demo -am -DskipTests` (no install needed; the parent and the
  library are skipped by the plugin) or `./mvnw -pl wicket-console-demo jetty:run` after `./mvnw install`.
  Jetty 9.4.58 plugin on :8080 (`-Djetty.http.port=…` to change); works on JDK 21 and 25.
  `src/test/java/.../Start.java` is an alternative embedded-Jetty launcher (port 8081) using the `provided` `jetty-all` uber jar.
- **Logging:** the only module with a logging binding: `log4j-slf4j-impl` 2.26.1 (`log4j.version`) and
  `src/main/resources/log4j2.xml`. Keep it here, never in the library.
- **JavaScript:** GraalJS 25.0.4 (`js-scriptengine` + `js` pom, `runtime`, `graalvm.version`) — the demo only; the
  library bans script engines (plan D4/D10). Same version as Orienteer. Truffle prints a "fallback runtime" warning on
  a stock JDK (silence with `-Dpolyglot.engine.WarnInterpreterOnly=false`).
- **Tests:** 3 — `TestHomePage` (renders `HomePage`), `TestJavaScriptConsole` (GraalJS via `ScriptExecutor` and the panel).

## Upgrade notes

- `src/test/resources/keystore` (Wicket quickstart, password `wicket`) is unused since the Jetty 7 connector config was
  removed in Stage A.
- Stage B: `Start.java` uses `org.apache.wicket.util.time.Duration` (removed in Wicket 9).
- Stage C: `web.xml` is Servlet 2.5 (`java.sun.com` namespace) → Jakarta EE 10; Jetty 9.4 plugin and `Start.java`
  (`jetty-all` no longer exists) → Jetty 12 ee10.

# wicket-console-demo

Sample war: `WicketApplication` with a `HomePage` that embeds `WicketConsolePanel` and the Wicket `DebugBar`
(which gets the console contributor). Root rules: [`../AGENTS.md`](../AGENTS.md); plan: [`../REFRESH_PLAN.md`](../REFRESH_PLAN.md).

- **Not published** and not used by Orienteer. It stays in the reactor while it builds (plan D9); parked only if it blocks a stage.
- **Run:** `mvn install -DskipTests` once, then `mvn -pl wicket-console-demo jetty:run` (Jetty 9.4 plugin, :8080).
  `src/test/java/.../Start.java` is an alternative embedded-Jetty launcher (port 8081) using the `provided` `jetty-all` uber jar.
- **Logging:** the only module with a logging binding (`slf4j-log4j12` + log4j 1.2.17, `src/main/resources/log4j.properties`).
- **Tests:** 1 (`TestHomePage`, WicketTester renders `HomePage`).
- A JavaScript engine is not on its classpath yet, so the console offers no engine on JDK 15+.

## Upgrade notes

- The Jetty plugin config uses Jetty 7 connector classes (`SelectChannelConnector`, `SslSocketConnector`) that the 9.4
  plugin ignores; `src/test/resources/keystore` is only referenced there.
- Stage B: `Start.java` uses `org.apache.wicket.util.time.Duration` (removed in Wicket 9).
- Stage C: `web.xml` is Servlet 2.5 (`java.sun.com` namespace) → Jakarta EE 10; Jetty 9.4 plugin and `Start.java`
  (`jetty-all` no longer exists) → Jetty 12 ee10.

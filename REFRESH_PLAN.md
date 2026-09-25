# wicket-console Refresh Plan

Living plan for bringing wicket-console from its 2020 state (Java 8, Wicket 8.3, javax, Nashorn-era `javax.script`) to
current, supported versions, in lockstep with [Orienteer's plan](https://github.com/OrienteerBAP/Orienteer/blob/master/REFRESH_PLAN.md)
(local: `~/Development/Orienteer/REFRESH_PLAN.md`). Reference implementation of the same process:
[wicket-orientdb](https://github.com/OrienteerBAP/wicket-orientdb) (local: `~/Development/wicket-orientdb`).
**Tick items off (`[ ]` → `[x]`) in the same commit that completes them**, and add a short note (date, deviations)
after the item when useful. Module details: each module's `AGENTS.md`.

Started: 2026-09-25. Versions below were checked on Maven Central on that date — re-check at the start of every stage
(`./mvnw versions:display-dependency-updates versions:display-plugin-updates`).

## Progress overview

| Phase | Goal | Depends on | Unblocks Orienteer | Status |
|---|---|---|---|---|
| P0 | AI initialization: AGENTS.md files + this plan | — | — | done |
| A | Stage A: JDK 21/25 build on the **same** framework majors (Wicket 8.15, javax), works without a JS engine, `./mvnw install` | P0 | P3 | in progress |
| B | Stage B: Wicket 9.x, still javax; JUnit 5 for the tests → `1.5` | A, `1.4` release (D8) | P5 | todo — not started |
| C | Stage C: Wicket 10.x, `jakarta.servlet` 6, `wicket-tester` → `2.0` | B (D8) | P6 | todo |
| R | Release & CI: GitHub Actions, Maven Central via the Central Portal: `1.4-SNAPSHOT` now, `1.4` after Orienteer P3 is green (D8/D11) | A | P3 "resolvable from a real repo" | todo |
| X | Cleanup, docs, known bugs & security | any time | — | todo |

## Guiding principles

1. **Never break the Orienteer contract** (below): coordinates, version `1.4-SNAPSHOT`/`1.4` (= Stage A; B = `1.5`, C = `2.0`, D8), public API.
2. **Keep the build green after every item.** One concern per commit; no big-bang upgrades.
3. **Same framework majors per stage:** A changes JDK/tooling only; B changes Wicket 8 → 9 only; C does jakarta + Wicket 10.
   Coupled libraries move together: Wicket ↔ servlet API ↔ jakarta ↔ Jetty (demo).
4. **Minimal, behavior-preserving code changes.** Any unavoidable public API change goes to "Handoff to Orienteer".
5. **No script engine and no logging binding in the library** (compile/runtime scope). Consumers bring their own.
6. **No reformat-only diffs;** match the surrounding indentation (tabs in Java/HTML/CSS, mixed in the poms).

## Decisions log

| # | Date | Decision | Status |
|---|---|---|---|
| D1 | 2026-09-25 | Target `--release 21`; verify on JDK 21 **and** 25 (Temurin 25.0.4 via SDKMAN). JDK 27 (non-LTS) is not targeted. Same as Orienteer D1. | accepted |
| D2 | 2026-09-25 | Coordinates stay `ru.ydn.wicket.wicket-console:wicket-console` (jar only) with parent `wicket-console-parent`; Stage A is `1.4-SNAPSHOT`. Later versions: D8. | accepted |
| D3 | 2026-09-25 | Stage A moves Wicket 8.3.0 → **8.15.0** (same major; what Orienteer uses). Not 8.19.0: Orienteer's `dependencyManagement` pins 8.15.0 and decides the runtime version anyway. | accepted |
| D4 | 2026-09-25 | **No JS engine in the library.** Nashorn left the JDK in 15; the library must work, and its tests must pass, with no JS engine at all. Library tests use a tiny JSR-223 engine that exists only in test sources. The demo may use GraalJS 25.0.4 (the version Orienteer uses, Orienteer D8). | accepted (owner, 2026-09-25) |
| D5 | 2026-09-25 | **Unknown/missing engine → error result.** `ScriptExecutor.executeWithoutHistory` (and so `execute`) returns a `ScriptResult` whose `error` says the engine is not available, instead of throwing a `NullPointerException`. No signature change. | accepted (owner, 2026-09-25) |
| D6 | 2026-09-25 | **Default engine fallback.** `WicketConsolePanel` keeps `"JavaScript"` as its default engine when one is available; otherwise it selects the first available engine (sorted, as shown in the selector). `ScriptExecutor.execute(command)` keeps `"JavaScript"`, now safe because of D5. | accepted (owner, 2026-09-25) |
| D7 | 2026-09-25 | **Engine-created bindings.** `EmbeddedScriptEngine` puts `engine.createBindings()` into its `ScriptContext` (standard JSR-223). GraalJS 25 (`GraalJSScriptEngine`) builds a new polyglot context on every `eval` when the engine-scope bindings are not its own, so variables and functions were lost between commands (the "contextual" feature). Nashorn and other engines behave the same as before. | accepted (owner, 2026-09-25) |
| D8 | 2026-09-25 | **Versioning** (Orienteer D9; the owner delegated it): (1) Stage A is published as `1.4-SNAPSHOT` to the Central Portal snapshots repository; (2) `1.4` is released from Stage A **only after Orienteer's P3 build is green against it** (fixes found there go into `1.4-SNAPSHOT` first); (3) after the release `master` moves to `1.5-SNAPSHOT` for Stage B (Wicket 9), released as `1.5`; Stage C (Wicket 10 + jakarta, incompatible for consumers) becomes `2.0-SNAPSHOT`, released as `2.0`; (4) Stage B/C work is **never** installed or published as `1.4-SNAPSHOT` — if B starts before `1.4` is released, it goes on a branch with `1.5-SNAPSHOT`. Versions are set with `./mvnw versions:set` across parent and modules. | decided (2026-09-25) |
| D9 | 2026-09-25 | The demo module stays in the reactor while it builds; if it blocks a stage it is parked (removed from `<modules>`, code kept). It is never published. | accepted (owner, 2026-09-25) |
| D10 | 2026-09-25 | The library module enforces its hygiene: `maven-enforcer-plugin` `bannedDependencies` fails the build if a logging binding or a script engine enters its compile or runtime scope (test scope is allowed). | accepted (owner, 2026-09-25), if the rule works cleanly |
| D11 | 2026-09-25 | **Publishing:** `ru.ydn` is verified on the Central Portal with SNAPSHOTs enabled (wicket-orientdb publishes there). Releases and snapshots go through `central-publishing-maven-plugin` (server id property `central.server.id`, default `central`). Publishing config (OSSRH `distributionManagement`, nexus-staging, release plugin) stays untouched in Stage A and is replaced in phase R. Every upload is run by the owner; agents only prepare and validate locally. | accepted |
| D12 | 2026-09-25 | Tests stay on JUnit 4.13.2 in Stage A; Mockito is not added (unused). Jupiter + vintage come in Stage B because Wicket 9 testers use JUnit 5 assertions. | accepted |

### Open questions (decide when the phase starts; record the answer above)

- [ ] **Demo after Stage C:** keep it (Jetty 12 ee10 port of plugin, `web.xml`, `Start.java`) or drop it? (C)
- [ ] **CSP (Stage B):** Wicket 9 enables a strict Content Security Policy. `WicketConsolePage.html` has an inline `<style>`
      and a `javascript:history.go(-1)` link; move the style into `wicketconsole.css` (or a separate resource) and replace
      the link? (B)
- [ ] **Known bugs in X** (session id before bind, non-thread-safe cache, engines never closed): fix in `1.4-SNAPSHOT`
      before the release, or in `1.5`? (X)

## Contract with Orienteer (from Orienteer's "External dependency requirements")

| Stage | Orienteer phase | Required from this library |
|---|---|---|
| A | P3 (Java 21, javax, Wicket 8) | JDK 21 build on Wicket 8; script engines work without Nashorn; release (or a published snapshot) |
| B | P5 (Wicket 9) | Wicket 9 port |
| C | P6 (Wicket 10, jakarta) | Wicket 10 / jakarta port |

Orienteer's `orienteer-devutils` consumes (7 files): `ScriptExecutor.registerScriptEngineFactory`, `IScriptEngine`,
`IScriptEngineFactory`, `IScriptContext`, `ScriptResult` (`setResult`, `setResultModel`, `setOut`, `setError`, `start`,
`finish`), `IScriptResultRenderer` + `ScriptResultRendererManager.INSTANCE.registerRenderer`, `WicketConsolePanel(id, IScriptContext)`,
`devutils.WicketConsoleDebugPanel.DEBUG_BAR_CONTRIB`, `devutils.WicketConsolePage` (+ `setWicketConsolePageImplementation`),
and in `TestModule` `ScriptExecutorHolder.get().getScriptExecutor().executeWithoutHistory(...)` with its "SQL" engine.

---

## P0 — AI initialization

- [x] Analyze the repository: modules, poms, plugins, JDK blockers, Stage B/C inventory, tests, branches — 2026-09-25
- [x] Root `AGENTS.md` (purpose, contract, stack, build status, commands, layout, conventions, testing, pitfalls) — 2026-09-25
- [x] `AGENTS.md` in `wicket-console/` and `wicket-console-demo/` — 2026-09-25
- [x] This plan with decisions log, contract, stages, appendices — 2026-09-25
- [x] Baseline build on JDK 21 (`mvn -B clean verify`, Maven 3.9.16): compiles with source/target 8 warnings; library
      3 run / **2 errors** (`NullPointerException` in `ScriptExecutor.executeWithoutHistory`: no `"JavaScript"` engine);
      demo 1 run / 0 failures — 2026-09-25

## A — Stage A: JDK 21/25 on the current stack (→ Orienteer P3)

Goal: `./mvnw clean verify` green on JDK 21 and 25 with `--release 21`, Wicket 8.15, javax, **no JS engine** in the
library or its tests; `./mvnw install` puts the jar and the parent pom for `1.4-SNAPSHOT` into `~/.m2`.

- [x] A1 Maven Wrapper 3.9.16 (`mvn wrapper:wrapper -Dmaven=3.9.16 -Dtype=only-script`): commit `mvnw`, `mvnw.cmd`,
      `.mvn/wrapper/maven-wrapper.properties`; `.sdkmanrc` (`java=21.0.11-tem`); minimal `.editorconfig` (UTF-8, LF,
      tabs for Java/HTML/CSS, nothing for the mixed poms; no trailing-whitespace or final-newline rules; **no reformat**)
      — 2026-09-25, wrapper plugin 3.3.4 (scripts identical to wicket-orientdb's). Measured: all 24 Java, 4 HTML and 2 CSS
      files indent with tabs (3 Java + 1 HTML also have some space-indented lines); all 4 poms mix both; 8 files lack a final newline
- [x] A2 Script engines without Nashorn: missing engine → error `ScriptResult` (D5); `WicketConsolePanel` default-engine
      fallback (D6); library tests use a test-only JSR-223 engine (`src/test`, registered through
      `META-INF/services/javax.script.ScriptEngineFactory`) instead of `"JavaScript"`; new tests for the missing engine and
      the fallback. First green `verify` on JDK 21 — 2026-09-25: `ScriptExecutor.executeWithoutHistory` returns an error
      result ("Script engine 'X' is not available. Available engines: […]", also for a `null` name; no result model, no
      duration); `WicketConsolePanel` preselects the first sorted engine when "JavaScript" is missing. Test engine
      `calc.CalcScriptEngine[Factory]` (name `Calc`: numbers, strings, variables, `+`/`*`, `print`, `fail`). Tests 3 → 6:
      the 2 JS tests now use `Calc` (same scripts); new `testOutputErrorsAndState` (stdout, error + output of a failing
      script, `$result`, variables across commands, `IScriptContext` bindings), `testMissingEngine`,
      `testDefaultEngineWithoutJavaScript` (`assumeFalse` if a JS engine is present). Probes: with the old
      `ScriptExecutor` 2 new tests fail with the NPE; with the old panel the fallback test fails. Library 6/0, demo 1/0
- [x] A3 Compiler: `maven-compiler-plugin` 3.5.1 → 3.16.0, `maven.compiler.release=21` replaces source/target 1.8
      (there is no `-parameters`; not added) — 2026-09-25: no source changes needed, 0 compiler warnings, class files
      major 65 (main and test); 6/0 + 1/0. The plugin moves into `pluginManagement` in A5
- [x] A4 Remove dead plugins: `maven-eclipse-plugin` (+ `wtp.version`), `cobertura-maven-plugin`, `coveralls-maven-plugin`.
      There are no `<repositories>` (nothing dead to remove); OSSRH `distributionManagement`/nexus-staging stay until R (D11)
      — 2026-09-25: none of them was bound to a phase, so the build output is unchanged (6/0 + 1/0). No m2e
      lifecycle-mapping or maven-bundle-plugin existed. `.travis.yml` still calls cobertura/coveralls: removed with Travis in R1
- [x] A5 Pin every build plugin once in root `<pluginManagement>` (Appendix B; wicket-orientdb's versions); release-profile
      source/javadoc/gpg pinned to current (javadoc 2.10.4 doesn't run on current JDKs); nexus-staging and the release
      plugin left for R. Add `maven-enforcer-plugin`: `requireMavenVersion [3.9,)`, `requireJavaVersion [21,)` — 2026-09-25:
      clean/resources 3.5.0, compiler 3.16.0 (config moved here), surefire 3.6.0, jar/war 3.5.1, install/deploy 3.2.0,
      site 3.22.0, enforcer 3.6.3, source 3.4.0, javadoc 3.12.0, gpg 3.2.8, release 2.5.3 (→ R), dependency 3.11.0,
      wrapper 3.3.4, versions 2.22.0, jetty `${jetty.version}` (the demo's version moved here). `versions:display-plugin-updates
      -Prelease`: "All plugins have a version specified"; only release, jetty (A9) and nexus-staging (the build extension
      keeps its version in the profile until R) remain outdated. Enforcer runs in every module (build-time only; consumers
      don't inherit this parent). 6/0 + 1/0 with surefire 3.6.0
- [x] A6 JUnit 4.12 → 4.13.2 (test scope; D12) — 2026-09-25: no test changes; 6/0 + 1/0
- [x] A7 Wicket 8.3.0 → 8.15.0 (core, extensions, devutils; D3); drop the redundant `${wicket.version}` on
      `wicket-extensions` in the module pom — 2026-09-25: no source changes; 6/0 + 1/0. Transitive: openjson 1.0.8 → 1.0.11,
      commons-fileupload 1.4 → 1.5, commons-io 2.5 → 2.6, commons-collections4 4.2 → 4.4, danekja 1.8.3 → 1.8.6;
      slf4j-api stays 1.7.25 (identical to what Orienteer already gets from Wicket 8.15)
- [x] A8 Library hygiene: servlet API and wicket-devutils `provided`; no logging binding or logging config in the jar
      (verify with `jar tf`); enforcer `bannedDependencies` for logging bindings and script engines in the library's
      compile/runtime scope (D10, verified with a probe); demo: log4j 1.2.17 + `slf4j-log4j12` (EOL, CVEs) →
      `log4j-slf4j-impl` 2.26.1 + `log4j2.xml` — 2026-09-25: the library was already clean (compile: wicket-core,
      wicket-extensions; `provided`: servlet API, wicket-devutils; JUnit `test`); jar = 29 classes + 4 markup/CSS files,
      2 `.properties`, `META-INF/services/org.apache.wicket.IInitializer`, no logging config. No hard pins reach consumers
      (versions come from the parent's `dependencyManagement`, which Orienteer doesn't inherit). Enforcer execution
      `enforce-library-hygiene` in the library pom bans SLF4J bindings, `log4j-slf4j(2)-impl`, `log4j-core`, logback,
      log4j 1.x, reload4j, GraalJS/polyglot, nashorn-core, rhino-engine in compile/runtime scope. Probes: `log4j-slf4j-impl`
      compile → fail, runtime → fail, test → pass; `js-scriptengine` compile → fail, `js` (pom) runtime → fail, test → pass.
      Demo war now ships log4j-api/core/slf4j-impl 2.26.1 instead of log4j 1.2.17 (same console pattern, root INFO;
      version in property `log4j.version`)
- [x] A9 Demo: Jetty 9.4.12 → 9.4.58 (`jetty-maven-plugin`, `provided` `jetty-all`); drop the Jetty 7 connector config the
      9.4 plugin ignores; `jetty:run` serves the home page on JDK 21 and 25 (ASM ≥ 9.8 for the plugin only if JDK 25 needs it)
      — 2026-09-25: `./mvnw package jetty:run -pl wicket-console-demo -am -DskipTests -Djetty.http.port=18089` (no install):
      on JDK 21.0.11 and 25.0.4 (Jetty banner `jvm …`) `GET /` → 302 → 200 with the panel and the DebugBar, and
      `WicketConsolePage` → 200 ("Wicket Version: 8.15.0"). No ASM override needed: the war has no Java 25 class files
      (re-checked in A10 with GraalJS). `Start.java` compiles against 9.4.58. The now-unused quickstart `keystore` is kept (X)
- [x] A10 GraalJS: `EmbeddedScriptEngine` uses engine-created bindings (D7) with a library regression test (the test
      engine keeps state only in its own bindings, like GraalJS) — 2026-09-25: `ctx.setBindings(engine.createBindings(),
      ENGINE_SCOPE)` in the constructor (skipped if an engine returns `null`; OrientDB's `OSQLScriptEngine` returns a
      `SimpleBindings`). `CalcScriptEngine` now mimics `GraalJSScriptEngine` (foreign bindings → fresh copy per eval);
      new `testEngineStateSurvivesBetweenCommands`. Before the fix it and `testOutputErrorsAndState` fail
      ("counter is not defined", `x` lost); after: 7/0 + 1/0. Nashorn (which stored its global inside any bindings) and
      the `$result`/`IScriptContext` bindings behave as before
- [x] A10 Demo gets GraalJS 25.0.4 (runtime) and a JS smoke test (arithmetic, state across commands, output capture,
      error on a bad script, Java objects bound via `IScriptContext`, panel default engine) — 2026-09-25: `js-scriptengine`
      + `js` (pom) 25.0.4 `runtime` in the demo only (`graalvm.version`, managed in the root pom; the library's tree has
      no `org.graalvm` artifact, and D10 would fail it). New `TestJavaScriptConsole` (2 tests): `ScriptExecutor` with
      `"JavaScript"` (arithmetic, `var`/`function`/`$result` across commands, `print`/`console.log` captured, a
      `ReferenceError` lands in `error`, `IScriptContext` values) and the panel on `HomePage` (default engine stays
      `"JavaScript"`, Ajax submit, result rendered). Demo 3/0, library 7/0. `jetty:run` on JDK 25.0.4 with GraalJS in the
      war: `GET /` 200, "JavaScript" preselected; the GraalJS jars have no Java 25 class files (`truffle-api` tops out at
      `META-INF/versions/21`), so Jetty's ASM 9.7.1 needs no override. **Found:** GraalJS's `ScriptEngine` denies host
      access by default, so calling methods on bound Java objects (Orienteer's `db`/`orient`/`util`) and `Java.type`/`java.*`
      fail — Nashorn allowed them. Handled separately in the next item (D13)
- [ ] A11 `./mvnw clean verify` green on 21 **and** 25; `./mvnw install`; confirm the jar and the parent pom in
      `~/.m2/repository/ru/ydn/wicket/wicket-console/`, class files major 65; `--add-opens` only where a test proves it
- [ ] A11 Update `AGENTS.md` files (build status, stack) and write "Handoff to Orienteer" below
- [ ] Exit check: `./mvnw clean verify` green on JDK 21 and 25; `./mvnw install` done; test results match Appendix C
      (differences explained)

## B — Stage B: Wicket 8 → 9, still javax (→ Orienteer P5)

Goal: Wicket 9.24 (last 9.x), javax Servlet 3.1, green on 21/25. Migration guide:
https://cwiki.apache.org/confluence/display/WICKET/Migration+to+Wicket+9.0 · Size: **S (≈ 0.5 day)**.
Version `1.5-SNAPSHOT` → `1.5` (D8); never install a Stage B build as `1.4-SNAPSHOT`.

- [ ] Prerequisite (D8): `1.4` released from Stage A and `master` moved to `1.5-SNAPSHOT`; if Stage B must start earlier,
      work on a branch with `1.5-SNAPSHOT`
- [ ] Wicket 8.15.0 → 9.24.0 (`wicket-core`, `wicket-extensions`, `wicket-devutils`)
- [ ] `javax.servlet-api` stays 3.1.0 `provided` (already the Wicket 9 baseline)
- [ ] Tests: `junit-bom` + `junit-jupiter` + `junit-vintage-engine` (test scope): Wicket 9's `WicketTester` asserts with
      `org.junit.jupiter.api.Assertions` (wicket-core declares Jupiter `provided`); optionally migrate the 2 test classes to Jupiter
- [ ] Compile against Wicket 9 and fix what breaks; expected touch points: `devutils.WicketConsoleDebugPanel`
      (`StandardDebugPanel`, `IDebugBarContributor`), `devutils.WicketConsolePage` (`DevUtilsPage`, `InspectorPage`'s `bug.png`),
      `SessionSizeDebugPanel`'s `harddrive.png`, `ISessionStore.BindListener/UnboundListener` in `ScriptExecutorHolder`
- [ ] CSP (open question): `WicketConsolePage.html` inline `<style>` and `javascript:history.go(-1)`; check that
      `ScrollToBottomBehavior` (on-dom-ready script) and `CtrlEnterSubmitBehavior` (Ajax precondition) work under the strict policy
- [ ] Demo: `Start.java` `org.apache.wicket.util.time.Duration` → `java.time.Duration`; `wicket-devutils` 9.24; Jetty 9.4 stays (javax)
- [ ] `util.time`, page store, `ModalWindow`, `WicketTesterScope`, danekja lambdas: not used — confirm with `rg`
- [ ] Update `AGENTS.md` files and "Handoff to Orienteer"
- [ ] Exit check: `./mvnw clean verify` green on 21/25; `./mvnw install`; Orienteer's `orienteer-devutils` compiles against it on its P5 branch

## C — Stage C: jakarta, Wicket 10 (→ Orienteer P6)

Goal: no `javax.servlet` left; Wicket 10.x; `jakarta.servlet-api` 6.0 `provided`. Size: **S (0.5–1 day, mostly the demo)**.

- [ ] Prerequisite: Stage B done (`1.5` released); version `2.0-SNAPSHOT` → `2.0` (D8) — never installed as `1.4-SNAPSHOT` or `1.5-SNAPSHOT`
- [ ] Wicket 9.24 → 10.x (10.11.0 seen); add `org.apache.wicket:wicket-tester` (test scope) for `WicketTester`
- [ ] `javax.servlet:javax.servlet-api` → `jakarta.servlet:jakarta.servlet-api` 6.0.0 `provided` (pom only: no Java file imports `javax.servlet`)
- [ ] Guice 7 / `jakarta.inject` / `jakarta.mail`: not used — nothing to do (verify with `rg`)
- [ ] SLF4J 2 arrives with Wicket 10: demo binding `log4j-slf4j-impl` → `log4j-slf4j2-impl`
- [ ] Demo (if kept, open question): `web.xml` 2.5 → Jakarta EE 10 `web-app_6_0.xsd`; `jetty-maven-plugin` 9.4 →
      `org.eclipse.jetty.ee10:jetty-ee10-maven-plugin` 12.1.x; rewrite `Start.java` for Jetty 12 (`jetty-all` no longer exists)
- [ ] Enforcer `bannedDependencies`: `javax.servlet:*`, `javax.inject:javax.inject`
- [ ] Update `AGENTS.md` files and "Handoff to Orienteer"
- [ ] Exit check: `rg 'javax\.servlet'` finds nothing; `./mvnw clean verify` green on 21/25; `./mvnw install`; Orienteer P6 compiles against it

## R — Release & CI

- [ ] R1 GitHub Actions workflow: push + PR, matrix JDK 21/25 (`actions/setup-java`, `distribution: temurin`, `cache: maven`),
      `./mvnw -B verify`, upload surefire reports, `concurrency`; remove `.travis.yml` and the dead Travis/Coveralls/VersionEye badges
- [ ] R2 Replace OSSRH `distributionManagement` and `nexus-staging-maven-plugin` with
      `org.sonatype.central:central-publishing-maven-plugin` 0.11.0 (`publishingServerId=${central.server.id}`, default
      `central`; snapshots to `https://central.sonatype.com/repository/maven-snapshots/`); exclude the demo
- [ ] R3 Release profile: sources + javadoc jars (doclint `all,-missing`, fix errors), `maven-gpg-plugin` 3.2.8; required POM
      metadata (name, description, url, licenses, developers, scm) valid in every published POM (the module `url` is
      `http://maven.apache.org`, the description says "JDK6+"); maven-release-plugin 2.5.3 → 3.3.1, tag `v@{project.version}`,
      local-only prepare (`pushChanges=false`, `localCheckout=true`)
- [ ] R4 `.github/dependabot.yml` (maven + github-actions, weekly, grouped); ignore majors of Wicket/servlet/Jetty until their stages
- [ ] R5 JaCoCo report (replaces cobertura/coveralls), no thresholds
- [ ] R6 Local validation without any upload: `-Prelease -Dgpg.skip` builds the release artifacts; list what a Central
      bundle would contain; publishing runbook below
- [ ] Owner: first `1.4-SNAPSHOT` deploy (`./mvnw -B clean deploy -Prelease -Dgpg.skip -Dcentral.server.id=ossrh`)
- [ ] Owner: release `1.4` once Orienteer P3 is green against `1.4-SNAPSHOT` (GPG key on a keyserver); then `master` → `1.5-SNAPSHOT` (D8)
- [ ] Exit check: the snapshot (then `1.4`) resolves from the Central Portal in a clean `~/.m2`; Orienteer's
      "Stage A delivered" item can be ticked for wicket-console

## X — Cleanup, docs, known bugs & security

### Docs & cleanup
- [ ] README: prerequisites (JDK 21, `./mvnw`), "bring your own script engine" (GraalJS `js-scriptengine`, or
      `org.openjdk.nashorn:nashorn-core`), current coordinates/version, the demo instructions
- [ ] `.gitignore`: add `.DS_Store`, `.idea/`, `*.iml`, `.vscode/`
- [ ] Library tests run without an SLF4J binding (NOP warning); optionally add a test-scoped binding (never in the jar)
- [ ] `openjson` and `slf4j-api` are used directly but only arrive through wicket-core — keep (they are Wicket's API) or declare
- [ ] Keep all `AGENTS.md` files current at the end of every stage

### Known bugs / tech debt
- [ ] `ScriptExecutorHolder.getScriptExecutor`: for a temporary session it reads the session id **before** `session.bind()`
      and caches the executor under `null`, so the first request's history is lost
- [ ] `ScriptExecutorHolder.scriptExecutorsCache` is a plain `HashMap` shared by all request threads → `ConcurrentHashMap`
- [ ] Engines are never closed: on session unbind, close `AutoCloseable` engines (GraalJS contexts)
- [ ] One engine appears under all its names in the selector (GraalJS registers ~12: `js`, `JavaScript`, `ECMAScript`, `Graal.js`…)
- [ ] `EmbeddedScriptEngine.tryConvertToJSON` relies on the Nashorn-only `Java.asJSONCompatible`; `StorageModel.resurect()` always returns null
- [ ] `HistoryItemPanel` markup uses Bootstrap/Font Awesome classes (`btn`, `fa fa-repeat`) that the library doesn't ship

### Security
- [ ] The console runs arbitrary code on the server with the JVM's privileges. `WicketConsolePanel` has no authorization
      of its own (embedding apps must restrict it; Orienteer does via widget/page permissions); `WicketConsolePage` is only
      guarded by `DevUtilsPage` (development utilities enabled). Document this in the README
- [ ] Demo: `HomePage.html` loads Google Fonts over plain HTTP; quickstart `keystore` (password `wicket`) — demo only

---

## Appendix A — Dependency versions (current → target)

"Latest" = latest stable on Maven Central on 2026-09-25.

| Dependency | Current | Latest seen | Target | Stage |
|---|---|---|---|---|
| JDK (`release`) | 1.8 | 25 LTS (27 non-LTS) | 21, verified on 21 + 25 | A |
| Apache Wicket (core, extensions; devutils `provided`) | 8.3.0 | 10.11.0 (9.x: 9.24.0; 8.x: 8.19.0) | 8.15.0 (A) → 9.24 (B) → 10.x (C) | A/B/C |
| Servlet API (`provided`) | javax 3.1.0 | jakarta 6.1.0 | javax 3.1.0 (A/B) → jakarta 6.0.0 (C) | C |
| JS engine | JDK Nashorn (gone since 15) | GraalJS 25.4.x (25.0.x line: 25.0.4); nashorn-core 15.7 | none in the library (D4); demo: GraalJS 25.0.4 | A |
| SLF4J API (via Wicket) | 1.7.25 | 2.0.x | follows Wicket (2.x with Wicket 10) | C |
| openjson (via Wicket) | 1.0.8 (Wicket 8.3) | 1.0.13 | follows Wicket | — |
| JUnit 4 / Jupiter | 4.12 / — | 4.13.2 / 6.x | 4.13.2 (A) + Jupiter/vintage (B) | A/B |
| Mockito | — (unused) | 5.x | not added | — |
| Demo logging | `slf4j-log4j12` 1.7.21 + log4j 1.2.17 (EOL) | log4j 2.26.1 | `log4j-slf4j-impl` 2.26.1 (A) → `log4j-slf4j2-impl` (C) | A/C |
| Demo Jetty (plugin + `jetty-all` `provided`) | 9.4.12.v20180830 | 12.1.x (9.4.58 last 9.4) | 9.4.58 (A) → 12.1 ee10 (C) | A/C |
| Maven | 3.9.16 (Homebrew) | 3.9.16 (4.0 RC) | wrapper 3.9.16 | A |

## Appendix B — Maven plugins (current → latest seen 2026-09-25)

| Plugin | Current | Latest seen | Action (stage) |
|---|---|---|---|
| maven-compiler-plugin | 3.5.1 | 3.16.0 | bump, `release` 21 (A) |
| maven-surefire-plugin | unpinned (Maven default 3.5.4) | 3.6.0 | pin (A) |
| maven-jar-plugin / maven-war-plugin (demo) | unpinned (3.5.0 / 3.5.1) | 3.5.1 / 3.5.1 | pin (A) |
| maven-resources/clean/install/deploy | unpinned | 3.5.0 / 3.5.0 / 3.2.0 / 3.2.0 | pin (A) |
| maven-dependency / versions / wrapper (helper goals) | unpinned | 3.11.0 / 2.22.0 / 3.3.4 | pin (A) |
| maven-enforcer-plugin | — | 3.6.3 | add (A) |
| jetty-maven-plugin (demo) | 9.4.12 | 9.4.58 → ee10 12.1.x | bump (A), replace (C) |
| maven-source / javadoc / gpg (release profile) | 3.0.0 / 2.10.4 / 1.6 | 3.4.0 / 3.12.0 / 3.2.8 | pin (A), wire up (R) |
| maven-release-plugin | 2.5.3 | 3.3.1 | bump (R) |
| nexus-staging-maven-plugin | 1.6.7 | OSSRH gone | → central-publishing-maven-plugin 0.11.0 (R) |
| maven-eclipse-plugin | 2.10 | retired | remove (A) |
| cobertura-maven-plugin, coveralls-maven-plugin | 2.7, 4.1.0 | dead | remove (A) → jacoco-maven-plugin 0.8.15 (R) |

## Appendix C — Test inventory (2026-09-25)

| Test class | `@Test` | `@Ignore` | Baseline JDK 21: run / errors | After A2 (JDK 21): run / failures | Notes |
|---|---|---|---|---|---|
| `TestWicketConsoleComponents` | 3 → 6 (A2) → 7 (A10) | 0 | 3 / **2** (NPE, no `"JavaScript"` engine) | 6 / 0 | scripts run on the test-only `Calc` engine (D4) |
| demo `TestHomePage` | 1 | 0 | 1 / 0 | 1 / 0 | renders `HomePage` with the panel and the DebugBar |
| demo `TestJavaScriptConsole` (A10) | 2 | 0 | — | — | GraalJS 25.0.4 through `ScriptExecutor` and the panel |

## Appendix D — Stage B/C inventory (2026-09-25)

| Item | Where | Stage |
|---|---|---|
| `javax.servlet` | no Java file; `javax.servlet-api` `provided` in the poms | C (pom only) |
| `javax.inject`, Guice, `javax.mail`, `javax.xml.bind`, `sun.*` | none | — |
| `org.apache.wicket.util.time.Duration` | demo `Start.java` | B |
| `WicketTesterScope`, page store, `ModalWindow`, danekja lambdas | none | — |
| `WicketTester` assertions need JUnit 5 (Wicket 9) / separate `wicket-tester` (Wicket 10) | `TestWicketConsoleComponents`, demo `TestHomePage` | B / C |
| Inline JS/CSS (CSP) | `devutils/WicketConsolePage.html` (inline `<style>`, `javascript:` link) | B |
| devutils API (`StandardDebugPanel`, `IDebugBarContributor`, `DevUtilsPage`) | `devutils/*` | B/C (compile check) |
| Demo: `web.xml` 2.5, Jetty 9 plugin, `Start.java` + `jetty-all` | demo | C |

## Appendix E — Useful commands

```bash
./mvnw versions:display-dependency-updates versions:display-plugin-updates   # what's outdated
./mvnw -pl wicket-console dependency:tree                                    # library classpath
./mvnw -pl wicket-console test -Dtest=TestWicketConsoleComponents            # one test class
unzip -l wicket-console/target/wicket-console-1.4-SNAPSHOT.jar              # what the jar ships
javap -v -cp wicket-console/target/classes ru.ydn.wicket.wicketconsole.ScriptExecutor | grep major
```

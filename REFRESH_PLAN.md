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
| A | Stage A: JDK 21/25 build on the **same** framework majors (Wicket 8.15, javax), works without a JS engine, `./mvnw install` | P0 | P3 | done (local install 2026-09-25; published artifact → R) |
| B | Stage B: Wicket 9.x, still javax; JUnit 5 for the tests → `1.5` | A, `1.4` release (D8) | P5 | todo — not started |
| C | Stage C: Wicket 10.x, `jakarta.servlet` 6, `wicket-tester` → `2.0` | B (D8) | P6 | todo |
| R | Release & CI: GitHub Actions, Maven Central via the Central Portal: `1.4-SNAPSHOT` now, `1.4` after Orienteer P3 is green (D8/D11) | A | P3 "resolvable from a real repo" | `1.4-SNAPSHOT` **published** to the Central Portal snapshots repo (2026-09-28, build `1.4-20260928.231208-1`; deleted ~2026-12-27 unless redeployed); `1.4` release pending (Orienteer P3, GPG key) |
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
| D13 | 2026-09-25 | **Java access for GraalJS (Nashorn parity).** GraalJS's `ScriptEngine` denies host access by default: scripts can't call methods of bound Java objects (Orienteer's `db`/`orient`/`util` bindings) and `Java.type`/`java.*` are undefined; Nashorn allowed both. `EmbeddedScriptEngine` enables `polyglot.js.allowHostAccess` and `polyglot.js.allowHostClassLookup` (any class) on the bindings it creates — only when the engine's factory is GraalJS (`getEngineName()` = `Graal.js`, no GraalJS dependency), only for the console's own engine instances. No global JVM flag; IO, thread creation and native access stay off; no Nashorn syntax mode. Alternatives rejected: a JVM-wide `-Dpolyglot.js.nashorn-compat=true` for Orienteer to mirror (affects every GraalJS engine in the JVM), or an opt-in switch (one more thing to configure for the same result). | accepted (owner, 2026-09-25) |

### Open questions (decide when the phase starts; record the answer above)

- [ ] **Demo after Stage C:** keep it (Jetty 12 ee10 port of plugin, `web.xml`, `Start.java`) or drop it? (C)
- [ ] **CSP (Stage B):** Wicket 9 enables a strict Content Security Policy. `WicketConsolePage.html` has an inline `<style>`
      and a `javascript:history.go(-1)` link; move the style into `wicketconsole.css` (or a separate resource) and replace
      the link? (B)
- [ ] **Known bugs in X** (session id before bind, non-thread-safe cache, engines never closed): fix in `1.4-SNAPSHOT`
      before the release, or in `1.5`? (X)
- [ ] **Engine selector aliases:** show one entry per engine instead of every alias (GraalJS: 14 names)? Visible in
      Orienteer's console widget. (X)

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
- [x] A10 GraalJS Java access like Nashorn (D13): `EmbeddedScriptEngine` puts `polyglot.js.allowHostAccess=true` and
      `polyglot.js.allowHostClassLookup` (any class) into the GraalJS bindings it creates, before the first eval —
      2026-09-25: detection by factory engine name `Graal.js`; if GraalJS rejects the options, a WARN is logged and the
      engine keeps working without Java access. Other engines are untouched. New demo test `testGraalJsJavaAccessLikeNashorn`
      (method calls on a bound `ArrayList`, `Java.type('java.lang.Math')`, `java.lang.Integer`, `new (Java.type(…))`):
      fails before the change (`Unknown identifier: add`), passes after. Library 7/0, demo 4/0
- [x] A11 `./mvnw clean verify` green on 21 **and** 25; `./mvnw install`; confirm the jar and the parent pom in
      `~/.m2/repository/ru/ydn/wicket/wicket-console/`, class files major 65; `--add-opens` only where a test proves it
      — 2026-09-25: JDK 21.0.11 and 25.0.4 both library 7/0, demo 4/0. **No `--add-opens` needed**; no illegal-access
      warnings. JDK 25 only prints Truffle's (GraalJS, demo) `System::load` and `sun.misc.Unsafe::objectFieldOffset`
      warnings, both harmless (see handoff). `./mvnw clean install` on JDK 21: parent pom + library pom + jar (29 classes,
      all major 65, `Build-Jdk-Spec: 21`, SHA-1 identical to `target/`), demo war (never published). Public/protected API
      diffed with `javap -protected` against the 2020 sources (`1efb676`) compiled on Wicket 8.15: **identical** (165 lines).
      A throwaway consumer pom mirroring every orienteer-devutils usage (engines, factory, renderer, panel with
      `IScriptContext`, `DEBUG_BAR_CONTRIB`, `setWicketConsolePageImplementation`, `executeWithoutHistory`) compiles
      against the installed `1.4-SNAPSHOT`; its runtime classpath has no script engine and no logging binding
- [x] A11 Update `AGENTS.md` files (build status, stack) and write "Handoff to Orienteer" below — 2026-09-25
- [x] Exit check: `./mvnw clean verify` green on JDK 21 and 25; `./mvnw install` done; test results match Appendix C
      (differences explained) — 2026-09-25: library 3 → 7 tests (2 fixed by using `Calc`, 4 added: A2 ×3, A10 ×1),
      demo 1 → 4 (`TestJavaScriptConsole` ×3); 0 skipped, 0 failures on both JDKs. **Stage A done (local)**

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

- [x] R1 GitHub Actions workflow: push + PR, matrix JDK 21/25 (`actions/setup-java`, `distribution: temurin`, `cache: maven`),
      `./mvnw -B verify`, upload surefire reports, `concurrency`; remove `.travis.yml` and the dead Travis/Coveralls/VersionEye badges
      — 2026-09-25: `.github/workflows/ci.yml` (push to any branch, PR, manual; `actions/checkout@v7`, `setup-java@v6`,
      `upload-artifact@v7` — latest majors; `contents: read`; `-ntp`), same shape as wicket-orientdb's. Validated against the
      GitHub workflow JSON schema (schemastore); the first real run happens when the owner pushes. No publishing in CI
      (D11). README shows the CI badge instead of the three dead ones. `mvnw` is committed executable (mode 100755)
- [x] R2 Replace OSSRH `distributionManagement` and `nexus-staging-maven-plugin` with
      `org.sonatype.central:central-publishing-maven-plugin` 0.11.0 (`publishingServerId=${central.server.id}`, default
      `central`; snapshots to `https://central.sonatype.com/repository/maven-snapshots/`); exclude the demo — 2026-09-25:
      plugin as build extension in the root `<build><plugins>` (version in `pluginManagement`), properties
      `central.server.id` (default `central`; the owner's token is under `ossrh` → `-Dcentral.server.id=ossrh`) and
      `central.snapshots.url`, `deploymentName` = `wicket-console <version>`, `autoPublish` default false (manual Publish
      in the portal). `distributionManagement` removed entirely (the plugin would otherwise fall back to it for
      snapshots); nexus-staging removed from the `release` profile. `excludeArtifacts` = `wicket-console-demo` (per
      wicket-orientdb's bytecode check of 0.11.0, applied on both the snapshot and the release path); the demo also skips
      sources/javadoc/gpg. Buildplan: `deploy` → `central-publishing:publish` (`injected-central-publishing`) in all 3
      modules, no `maven-deploy-plugin`. Nothing was uploaded. Note: the effective POM still lists
      `oss.sonatype.org` — from the active `archetype` profile in the owner's `~/.m2/settings.xml`, not from this repo
- [x] R3 Release profile: sources + javadoc jars (doclint `all,-missing`, fix errors), `maven-gpg-plugin` 3.2.8; required POM
      metadata (name, description, url, licenses, developers, scm) valid in every published POM (the module `url` is
      `http://maven.apache.org`, the description says "JDK6+"); maven-release-plugin 2.5.3 → 3.3.1, tag `v@{project.version}`,
      local-only prepare (`pushChanges=false`, `localCheckout=true`) — 2026-09-25: profile `release` = `source:jar-no-fork` +
      `javadoc:jar` + `gpg:sign` (3.4.0 / 3.12.0 / 3.2.8; the library has no test-jar, so no test-sources). Javadoc with
      `doclint=all,-missing`: **0 errors, 0 warnings**, no source changes needed (the code has almost no Javadoc).
      `-Prelease -Dgpg.skip verify`: library jar + `-sources` (40 entries incl. markup/CSS) + `-javadoc`; the demo skips
      sources/javadoc/gpg. Without `-Dgpg.skip` and without a key, `gpg:sign` fails ("No secret key") as expected.
      POM metadata (effective POMs of parent and library): name, description (new, accurate), url, license (now https),
      developer, scm all present; module `url`/`scm` no longer get `/<artifactId>` appended
      (`child.*.inherit.append.path=false`), the bogus module `url` `http://maven.apache.org` is gone, scm `connection`
      fixed to `scm:git:https://…`. Release plugin 3.3.1; deprecated `useReleaseProfile` dropped; default next version
      after `1.4` is `1.5-SNAPSHOT` (matches D8)
- [x] R4 `.github/dependabot.yml` (maven + github-actions, weekly, grouped); ignore majors of Wicket/servlet/Jetty until their stages
      — 2026-09-25: groups for Maven plugins, GraalVM and all GitHub Actions; ignored until their stages: Wicket majors,
      `javax.servlet-api`, Jetty minor/major (demo), GraalVM minor/major (follows Orienteer's 25.0.x), JUnit 4 major,
      Log4j major, maven-site-plugin 4 (milestones only). Validated against the Dependabot JSON schema; active once pushed
- [x] R5 JaCoCo report (replaces cobertura/coveralls), no thresholds — 2026-09-25: `jacoco-maven-plugin` 0.8.15,
      `prepare-agent` + `report` at `verify`, instrumenting only `ru/ydn/**` (GraalJS untouched). Green on 21 and 25.
      Baseline coverage of `wicket-console`: 84% instructions, 81% lines (25 classes; the 4 interfaces have no code).
      CI uploads the report from the JDK 21 job
- [x] R6 Local validation without any upload: `-Prelease -Dgpg.skip` builds the release artifacts; list what a Central
      bundle would contain; publishing runbook below — 2026-09-25: `./mvnw -B clean verify -Prelease -Dgpg.skip` green on
      JDK 21 and 25 (7/0 + 4/0, JaCoCo on, 0 javadoc warnings). `release:prepare -DdryRun=true -DreleaseVersion=1.4
      -DdevelopmentVersion=1.5-SNAPSHOT -Dtag=v1.4`: all 17 steps pass (release POMs `1.4` + scm tag `v1.4`, next POMs
      `1.5-SNAPSHOT`), no commit, no tag, nothing installed; `release:clean` afterwards. The Central bundle was assembled
      offline the way the plugin stages it (Maven layout, md5/sha1/sha256/sha512) from a throwaway `git archive` copy set to
      `1.4` and built with `-Prelease -Dgpg.skip verify` (never installed): 5 artifacts + 20 checksums, no demo, no
      SNAPSHOT references; only the `.asc` signatures are missing (no GPG key). File list in the handoff §10. The plugin's
      upload paths are not exercised (they would upload). Finally `./mvnw clean install` on JDK 21 refreshed `~/.m2`
      (POMs = HEAD, jar unchanged: 29 classes, major 65)
- [x] Owner: first `1.4-SNAPSHOT` deploy (`./mvnw -B clean deploy -Prelease -Dgpg.skip -Dcentral.server.id=ossrh`) — runbook below
      — 2026-09-28 ~23:12 UTC, run by the Orienteer session in this checkout from a clean `master` at `bd3a0cb` (level with
      `origin/master`, i.e. pushed) on JDK 21.0.11: BUILD SUCCESS, tests 7/0 + demo 4/0. Uploaded build
      **`1.4-20260928.231208-1`**: `wicket-console-parent` pom; `wicket-console` pom, jar, `-sources`, `-javadoc`
      (unsigned, no `.asc`). `wicket-console-demo` not uploaded (404 on the snapshots repo). **Snapshots are deleted
      after 90 days → around 2026-12-27:** redeploy (same command) or release `1.4` before then
- [ ] Owner: release `1.4` once Orienteer P3 is green against `1.4-SNAPSHOT` (GPG key on a keyserver); then `master` → `1.5-SNAPSHOT` (D8)
- [ ] Owner, recommended: `~/.m2/settings.xml` is world-readable (`644`) with plain-text tokens → `chmod 600 ~/.m2/settings.xml`
      and/or encrypt (`mvn --encrypt-master-password`, `mvn --encrypt-password`); optionally copy the Portal token to a
      server `central` to drop `-Dcentral.server.id=ossrh`. Its active `archetype` profile still adds the dead
      `oss.sonatype.org` snapshots repository to every build (harmless, slow lookups)
- [ ] Optional, owner's call: publish from CI (GitHub secrets + a workflow on tags) and CodeQL — not implemented; CI only builds and tests
- [x] Exit check: the snapshot (then `1.4`) resolves from the Central Portal in a clean `~/.m2`; Orienteer's
      "Stage A delivered" item can be ticked for wicket-console — 2026-09-28 (snapshot): a throwaway consumer pom whose
      only extra repository is the Central Portal snapshots one, built with an empty `-Dmaven.repo.local` and an empty
      settings file (so the owner's `settings.xml` profiles/mirrors can't help), resolved `wicket-console` and its parent
      `1.4-SNAPSHOT` (build `1.4-20260928.231208-1`) from `central-portal-snapshots` and the 11 other runtime artifacts
      (Wicket 8.15.0 and its deps, slf4j-api 1.7.25) from Central, and compiled a probe mirroring every orienteer-devutils
      usage. Jar, `-sources` and `-javadoc` are **byte-identical (SHA-1)** to the deploy build in `wicket-console/target/`
      (jar `a4ce5afe…`, also = the `~/.m2` install) and to the remote `.sha1` files; both POMs are identical to HEAD.
      29 classes, all major 65; no script engine or logging binding on the consumer's runtime classpath. The `1.4`
      release remains open (item above)

### Publishing runbook (owner only — D11)

Prerequisites (one-off; the first two are already true for `ru.ydn`, as wicket-orientdb publishes there):
1. **Central Portal user token** in `~/.m2/settings.xml`. It currently sits under server id `ossrh`, hence
   `-Dcentral.server.id=ossrh` below (property in the root pom; default `central`).
2. **SNAPSHOTs enabled** for the `ru.ydn` namespace (https://central.sonatype.com/publishing/namespaces).
3. **GPG key (needed for `1.4`, not for snapshots):** `gpg --full-generate-key` (RSA 4096, `Ilia Naryzhny <phantom@ydn.ru>`),
   then `gpg --keyserver keyserver.ubuntu.com --send-keys <KEYID>`. The same key serves every `ru.ydn` library.

Snapshot `1.4-SNAPSHOT` (first deployed 2026-09-28, build `1.4-20260928.231208-1`; repeat after every fix Orienteer P3
needs, and **before ~2026-12-27** when the Central Portal deletes it, unless `1.4` is released by then):
```bash
git switch master && git status                                         # clean tree at 1.4-SNAPSHOT (push first if CI should see it)
./mvnw -B clean deploy -Prelease -Dgpg.skip -Dcentral.server.id=ossrh   # -SNAPSHOT → Central Portal snapshots; demo excluded
curl -s https://central.sonatype.com/repository/maven-snapshots/ru/ydn/wicket/wicket-console/wicket-console/1.4-SNAPSHOT/maven-metadata.xml
```
Uploads: `wicket-console-parent` pom; `wicket-console` pom, jar, `-sources`, `-javadoc` (timestamped, unsigned). Snapshots
are not validated and are deleted 90 days after upload. Once a GPG key exists, drop `-Dgpg.skip` to sign them too.

Release `1.4` (only after Orienteer P3 is green against `1.4-SNAPSHOT` — D8):
```bash
./mvnw -B release:prepare -DreleaseVersion=1.4 -DdevelopmentVersion=1.5-SNAPSHOT -Dtag=v1.4   # local commits + tag only
./mvnw -B release:perform -Darguments=-Dcentral.server.id=ossrh   # builds tag v1.4 with -Prelease: signs, uploads the bundle
# → https://central.sonatype.com/publishing/deployments : inspect "wicket-console 1.4", then Publish (or Drop and fix)
git push origin master && git push origin v1.4                   # after publishing
```
If validation fails before anything was pushed: Drop the deployment, `git tag -d v1.4`, `git reset --hard HEAD~2`
(the two `[maven-release-plugin]` commits), `./mvnw release:clean`, fix, repeat. Central releases are immutable once published.

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
      guarded by `DevUtilsPage` (development utilities enabled). Document this in the README. With GraalJS the console
      deliberately has full Java access (D13), exactly as with Nashorn: it is no sandbox
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

| Test class | `@Test` | `@Ignore` | Baseline JDK 21: run / errors | Stage A exit, JDK 21 and 25: run / failures | Notes |
|---|---|---|---|---|---|
| `TestWicketConsoleComponents` | 3 → 6 (A2) → 7 (A10) | 0 | 3 / **2** (NPE, no `"JavaScript"` engine) | 7 / 0 | scripts run on the test-only `Calc` engine (D4) |
| demo `TestHomePage` | 1 | 0 | 1 / 0 | 1 / 0 | renders `HomePage` with the panel and the DebugBar |
| demo `TestJavaScriptConsole` (A10) | 2 → 3 (D13) | 0 | — | 3 / 0 | GraalJS 25.0.4 through `ScriptExecutor` and the panel; Java access |

Stage A exit (surefire 3.6.0): **library 7 run, demo 4 run, 0 failures, 0 errors, 0 skipped** on Temurin 21.0.11 and 25.0.4.
`testDefaultEngineWithoutJavaScript` would be skipped only if a JS engine reached the library's test classpath.

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

## Handoff to Orienteer

Stage A + phase R, 2026-09-25; published 2026-09-28. **`1.4-SNAPSHOT` is published** to the Central Portal snapshots
repository (build `1.4-20260928.231208-1`, deployed from `bd3a0cb` = `origin/master`, unsigned). Orienteer resolves it
from there with the repository snippet in §5 (verified from an empty local repo, phase R exit check); no local install
is needed any more. **The Central Portal deletes snapshots 90 days after upload, i.e. around 2026-12-27:** before then
either redeploy (runbook, phase R; each redeploy creates a new timestamped build) or release `1.4` (after Orienteer P3
is green, D8), or Orienteer's builds stop resolving it.

### 1. Published and installed artifacts

Central Portal snapshots (`https://central.sonatype.com/repository/maven-snapshots/ru/ydn/wicket/wicket-console/`),
build `1.4-20260928.231208-1`, unsigned:

| Coordinates | Published files |
|---|---|
| `ru.ydn.wicket.wicket-console:wicket-console-parent:1.4-SNAPSHOT:pom` | `.pom` |
| `ru.ydn.wicket.wicket-console:wicket-console:1.4-SNAPSHOT:jar` | `.pom`, `.jar` (29 classes, 38805 bytes, SHA-1 `a4ce5afe79972389044b7042660042840a6ab615`), `-sources.jar`, `-javadoc.jar` |
| `wicket-console-demo` | not published (404), by design |

Locally in `~/.m2/repository/ru/ydn/wicket/wicket-console/` the same `1.4-SNAPSHOT` parent pom and jar (the deploy's
`install` phase wrote the identical jar), plus the demo war (never published).

- There is no test-jar (there never was one; Orienteer doesn't use one).
- **Bytecode:** `--release 21`, every class file is major version **65**; the manifest says `Build-Jdk-Spec: 21`.
- **Built and tested with:** Temurin 21.0.11 and Temurin 25.0.4, Maven 3.9.16 through `./mvnw`.

### 2. Versions compiled against

| Library | Version | Scope in the library pom |
|---|---|---|
| Apache Wicket (core, extensions) | **8.15.0** (was 8.3.0) | compile |
| wicket-devutils | 8.15.0 | **provided** (optional at runtime: `Initializer` probes for `DebugBar`) |
| Servlet API | `javax.servlet:javax.servlet-api` 3.1.0 (no Java file uses it) | **provided** |
| openjson, SLF4J API | 1.0.11, 1.7.25 — transitive from wicket-core 8.15.0; **no binding** | — |
| Script engine | **none** (D4); tested with the test-only `Calc` engine and, in the demo, GraalJS 25.0.4 | — |
| JUnit | 4.13.2 | test |

### 3. JVM flags Orienteer must mirror

- **Required: none.** All tests pass on JDK 21 and 25 without `--add-opens`/`--add-exports`; the library uses no JDK
  internals and no reflection into them.
- **Optional (GraalJS noise, not caused by this library, same as wicket-orientdb's handoff):**
  `-Dpolyglot.engine.WarnInterpreterOnly=false` (Truffle "fallback runtime" warning, JDK 21 and 25);
  `--enable-native-access=ALL-UNNAMED` (JDK 25 `System::load` warning from `truffle-api`);
  `--sun-misc-unsafe-memory-access=allow` (JDK 25 `Unsafe::objectFieldOffset` warning; **JDK 23+ only**, JDK 21 refuses to start with it).

### 4. Behavior changes Orienteer will see (no signature changes)

1. **Missing engine → error result (D5).** `ScriptExecutor.execute(...)`/`executeWithoutHistory(...)` with an unknown
   engine name (or `null`) return a `ScriptResult` whose `error` is "Script engine 'X' is not available. Available
   engines: […]" instead of throwing a `NullPointerException`. `execute(command)` (default `"JavaScript"`) is now safe.
2. **Default engine fallback (D6).** `WicketConsolePanel` preselects `"JavaScript"` if present (it is, via GraalJS),
   otherwise the first engine in sorted order.
3. **Engine state between commands (D7).** `EmbeddedScriptEngine` uses `engine.createBindings()`. With GraalJS, variables
   and functions defined in one console command were lost in the next (a new JS context per eval, also a leak); now
   they persist per session and engine, as with Nashorn. Orienteer's own `ODBScriptEngine`/`ODBConsoleEngine` implement
   `IScriptEngine` directly and are not affected.
4. **Java access from GraalJS (D13).** For GraalJS only, the console enables host access and host class lookup on the
   engine it creates, so public methods of the widget's `db`/`orient`/`util` bindings are callable and `Java.type(...)`/
   `java.*` work, as with Nashorn. No global flag, nothing to configure in Orienteer. IO, threads and native access
   stay off. The console remains full server-side code execution (it always was): keep it behind admin permissions.

### 5. Actions for Orienteer (P1/P3)

1. Resolve `wicket-console:1.4-SNAPSHOT` from the Central Portal snapshots repository (published 2026-09-28; drop any
   reliance on a local install). Coordinates and version are unchanged, so `orienteer-devutils/pom.xml` needs no edit.
   Orienteer's root pom already has this repository (its P1, for wicket-orientdb); this parent pom declares none.
   Drop the repository once Orienteer switches to the `1.4` release:
   ```xml
   <repository>
     <id>central-portal-snapshots</id>
     <url>https://central.sonatype.com/repository/maven-snapshots/</url>
     <releases><enabled>false</enabled></releases>
     <snapshots><enabled>true</enabled></snapshots>
   </repository>
   ```
2. JavaScript in the devutils console needs a JSR-223 JS engine on Orienteer's runtime classpath: GraalJS 25.0.4 via
   wicket-orientdb (Orienteer D8) is enough (`js-scriptengine` registers `js`, `JavaScript`, `ECMAScript`, `Graal.js`…).
   The P3 item "Nashorn replacement … verify devutils consoles" can use `TestJavaScriptConsole` in the demo as a model.
3. The selector lists GraalJS under ~14 names (it always listed every name of every engine); cosmetic, tracked in X.

### 6. Public API changes

- **None.** `javap -protected` of all 29 classes is identical to the 2020 code (`1efb676`) compiled against Wicket 8.15.
  Changed implementations only: `ScriptExecutor.executeWithoutHistory`, `WicketConsolePanel` constructor,
  `EmbeddedScriptEngine` constructor (+ 2 private helpers).

### 7. Transitive dependency changes (library runtime classpath, baseline → Stage A)

| Change | Artifacts | Cause |
|---|---|---|
| upgraded | wicket-core/-request/-util/-extensions 8.3.0 → 8.15.0; openjson 1.0.8 → 1.0.11; commons-fileupload 1.4 → 1.5; commons-io 2.5 → 2.6; commons-collections4 4.2 → 4.4; danekja 1.8.3 → 1.8.6 | Wicket 8.15 (all identical to what Orienteer already gets from its own Wicket 8.15) |
| unchanged | slf4j-api 1.7.25 | — |
| added / removed | none | — |

### 8. Test results

| JDK | wicket-console | demo |
|---|---|---|
| Temurin 21.0.11 | 7 run, 0 failures, 0 errors, 0 skipped | 4 run, 0 failures |
| Temurin 25.0.4 | 7 run, 0 failures, 0 errors, 0 skipped | 4 run, 0 failures |

Baseline was 3 run / 2 errors (NPE without Nashorn) + demo 1. Nothing is `@Ignore`d or skipped. The demo's `jetty:run`
serves the home page and `WicketConsolePage` on both JDKs (Jetty 9.4.58), with GraalJS preselected.

### 9. Work for Stages B and C (plan sections B and C)

- **Stage B** (Wicket 9.24, javax, `1.5-SNAPSHOT`). Estimate: **S, ≈ 0.5 day.** Wicket 9 bump; Jupiter + vintage for
  `WicketTester` assertions; CSP for `WicketConsolePage.html` (inline `<style>`, `javascript:` link); compile check of the
  devutils API; demo `Start.java` `util.time.Duration`. No servlet, page-store, `ModalWindow` or `WicketTesterScope` work.
- **Stage C** (Wicket 10, jakarta Servlet 6, `2.0-SNAPSHOT`). Estimate: **S, 0.5–1 day, mostly the demo.** Library: pom-only
  (`jakarta.servlet-api` 6.0 `provided`, `wicket-tester` for tests) — no Java file imports `javax.servlet`, no Guice or
  `javax.inject`. Demo (if kept): Jetty 12 ee10 plugin, Jakarta EE 10 `web.xml`, `Start.java`, `log4j-slf4j2-impl`.
- Orienteer must still move to the new versions for B/C: they are Wicket-major rebuilds even with tiny code diffs.

### 10. What the owner needs to do to publish, and open questions

1. ~~Review and push~~ — done: `origin/master` = `bd3a0cb`; its CI run (JDK 21/25) succeeded on 2026-09-25, and
   Dependabot is running.
2. ~~Deploy the snapshot~~ — done 2026-09-28 (build `1.4-20260928.231208-1`), verified from an empty local repo.
   Orienteer's P1 "3 missing POMs" drops to 2, and its "Stage A delivered" line can list wicket-console.
3. **Before ~2026-12-27:** redeploy the snapshot (same command) unless `1.4` is released by then — the Central Portal
   deletes snapshots after 90 days.
4. **Later, for `1.4`:** a GPG key on a keyserver (shared by all `ru.ydn` libraries), then `release:prepare`/`perform`
   after Orienteer P3 is green (D8).
5. Recommended: `chmod 600 ~/.m2/settings.xml` (plain-text tokens, currently world-readable); optionally a `central` server entry.

Open questions (none blocks Stage A):
- **Demo after Stage C:** keep it (Jetty 12 ee10 port) or drop it? (C)
- **CSP for Stage B:** move the inline `<style>` of `WicketConsolePage.html` into CSS and replace the `javascript:` link? (B)
- **Known bugs** (plan X): temporary-session history lost on the first request (id read before `bind()`), non-thread-safe
  executor cache, GraalJS contexts never closed on session end. Fix them in `1.4-SNAPSHOT` before the `1.4` release, or in `1.5`?
- **Engine selector** lists every alias of every engine (GraalJS: 14 names). Deduplicate (behavior change visible in Orienteer's widget)?

### 11. Phase R — Central bundle (local dry run, 2026-09-25)

What the owner's `release:perform` for `1.4` will upload, minus the `.asc` signatures (each file below also gets `.asc`
when signed). Each file has `.md5`, `.sha1`, `.sha256` and `.sha512` next to it (20 checksum files); 25 entries in total.
The demo is excluded. The snapshot deploy uploaded the same 5 artifacts as timestamped `1.4-SNAPSHOT` builds
(confirmed 2026-09-28 on the snapshots repository: `1.4-20260928.231208-1` pom/jar/sources/javadoc + parent pom).

| Size (bytes) | Path |
|---|---|
| 12228 | `ru/ydn/wicket/wicket-console/wicket-console-parent/1.4/wicket-console-parent-1.4.pom` |
| 4883 | `ru/ydn/wicket/wicket-console/wicket-console/1.4/wicket-console-1.4.pom` |
| 38786 | `ru/ydn/wicket/wicket-console/wicket-console/1.4/wicket-console-1.4.jar` |
| 20140 | `ru/ydn/wicket/wicket-console/wicket-console/1.4/wicket-console-1.4-sources.jar` |
| 203665 | `ru/ydn/wicket/wicket-console/wicket-console/1.4/wicket-console-1.4-javadoc.jar` |

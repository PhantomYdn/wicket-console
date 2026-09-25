package ru.ydn.wicket.wicketconsole;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Smoke test of the console with a real JavaScript engine: GraalJS 25 (demo dependency; the library has none),
 * the engine Orienteer uses since the JDK dropped Nashorn.
 */
public class TestJavaScriptConsole
{
	private static final String JS = "JavaScript";

	private WicketTester tester;

	@Before
	public void setUp()
	{
		tester = new WicketTester(new WicketApplication());
	}

	@After
	public void tearDown()
	{
		tester.destroy();
	}

	@Test
	public void testGraalJsThroughScriptExecutor()
	{
		assertTrue(ScriptExecutor.getSupportedEngines().contains(JS));
		ScriptExecutor se = ScriptExecutorHolder.get().getScriptExecutor();

		ScriptResult result = se.execute("1+Math.sin(0)*2", JS, null);
		assertNoError(result);
		assertEquals(1.0, ((Number)result.getResult()).doubleValue(), 0);

		// State survives between commands (REFRESH_PLAN.md D7): variables, functions and $result
		assertNoError(se.execute("var counter = 40; function inc(x) { return x + 1; }", JS, null));
		result = se.execute("inc(counter) + 1", JS, null);
		assertNoError(result);
		assertEquals(42, ((Number)result.getResult()).intValue());
		result = se.execute("$result * 2", JS, null);
		assertNoError(result);
		assertEquals(84, ((Number)result.getResult()).intValue());

		// Output is captured per command
		result = se.execute("print('hello'); console.log('world')", JS, null);
		assertNoError(result);
		assertEquals("hello\nworld\n", result.getOut());

		// Script errors end up in the result, not as exceptions
		result = se.execute("noSuchFunction()", JS, null);
		assertNull(result.getResult());
		assertTrue(result.getError(), result.getError().contains("ReferenceError"));

		// Values bound through an IScriptContext are visible to the script
		IScriptContext context = new IScriptContext() {
			@Override
			public Map<String, Object> getBindings() {
				return Collections.<String, Object>singletonMap("answer", 41);
			}
		};
		result = se.execute("answer + 1", JS, context);
		assertNoError(result);
		assertEquals(42, ((Number)result.getResult()).intValue());
		assertEquals(7, se.getHistory().size());
	}

	@Test
	public void testGraalJsJavaAccessLikeNashorn()
	{
		// REFRESH_PLAN.md D13: GraalJS denies host access by default; the console enables it (as Nashorn had it), so
		// methods of bound Java objects (Orienteer's db/orient/util) and Java classes are usable from scripts
		ScriptExecutor se = ScriptExecutorHolder.get().getScriptExecutor();
		final List<String> list = new ArrayList<String>(Arrays.asList("a", "b"));
		IScriptContext context = new IScriptContext() {
			@Override
			public Map<String, Object> getBindings() {
				return Collections.<String, Object>singletonMap("list", list);
			}
		};
		ScriptResult result = se.execute("list.add('c'); list.size() + list.get(1)", JS, context);
		assertNoError(result);
		assertEquals("3b", result.getResult());
		assertEquals(3, list.size());

		result = se.execute("Java.type('java.lang.Math').max(3, 7)", JS, null);
		assertNoError(result);
		assertEquals(7, ((Number)result.getResult()).intValue());

		result = se.execute("java.lang.Integer.toHexString(255)", JS, null);
		assertNoError(result);
		assertEquals("ff", result.getResult());

		result = se.execute("new (Java.type('java.lang.StringBuilder'))('ab').reverse().toString()", JS, null);
		assertNoError(result);
		assertEquals("ba", result.getResult());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testGraalJsThroughThePanel()
	{
		tester.startPage(HomePage.class);
		// With a JavaScript engine present, the panel keeps "JavaScript" as its default engine (D6)
		DropDownChoice<String> engineSelector = (DropDownChoice<String>)tester.getComponentFromLastRenderedPage("console:form:scriptEngine");
		assertEquals(JS, engineSelector.getModelObject());

		FormTester formTester = tester.newFormTester("console:form");
		formTester.setValue("script", "var a = 6; a * 7");
		tester.executeAjaxEvent("console:form:submit", "click");
		ListView<ScriptResult> history = (ListView<ScriptResult>)tester.getComponentFromLastRenderedPage("console:form:historyContainer:history");
		assertEquals(1, history.getModelObject().size());
		ScriptResult item = history.getModelObject().get(0);
		assertNoError(item);
		assertEquals(JS, item.getEngine());
		assertEquals(42, ((Number)item.getResult()).intValue());
		tester.assertContains("wc-item-return");
	}

	private static void assertNoError(ScriptResult result)
	{
		assertTrue(result.getError(), Strings.isEmpty(result.getError()));
	}
}

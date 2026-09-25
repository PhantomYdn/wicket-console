package ru.ydn.wicket.wicketconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeFalse;
import ru.ydn.wicket.wicketconsole.calc.CalcScriptEngineFactory;
import ru.ydn.wicket.wicketconsole.devutils.WicketConsolePage;

/**
 * Runs without any JavaScript engine (there is none in the JDK since 15): scripts use the test-only
 * {@link CalcScriptEngineFactory} engine, found through {@code javax.script} like any other engine.
 */
public class TestWicketConsoleComponents
{
	private static final String JAVASCRIPT = "JavaScript";

	private WicketTester tester;
	
	@Before
	public void setUp()
	{
		tester = new WicketTester();
	}
	
	@After
	public void after()
	{
		tester.destroy();
	}
	
	@Test
	public void testSimpleExecution() {
		ScriptExecutor se = ScriptExecutorHolder.get().getScriptExecutor();
		assertEquals((double)4, ((Number)se.executeWithoutHistory("2+2",CalcScriptEngineFactory.NAME,null).getResult()).doubleValue(), 0);
		assertEquals((double)4, ((Number)se.executeWithoutHistory("var a=2; a+a",CalcScriptEngineFactory.NAME,null).getResult()).doubleValue(), 0);
	}
	
	@Test
	public void testOutputErrorsAndState() {
		ScriptExecutor se = ScriptExecutorHolder.get().getScriptExecutor();
		ScriptResult result = se.execute("print(\"hello\"); 1+1", CalcScriptEngineFactory.NAME, null);
		assertEquals("hello\n", result.getOut());
		assertTrue(Strings.isEmpty(result.getError()));
		assertEquals(2L, result.getResult());
		assertNotNull(result.getDuration());
		// The engine keeps its state between commands: variables and the previous result ($result)
		assertEquals(20L, se.execute("x = $result*10", CalcScriptEngineFactory.NAME, null).getResult());
		assertEquals(21L, se.execute("x+1", CalcScriptEngineFactory.NAME, null).getResult());
		// Output written before a failure is kept next to the error (issue #14)
		result = se.execute("print(\"before\"); fail(\"boom\")", CalcScriptEngineFactory.NAME, null);
		assertEquals("before\n", result.getOut());
		assertTrue(result.getError(), result.getError().contains("boom"));
		assertNull(result.getResult());
		// Bindings of an IScriptContext are visible to the script
		IScriptContext context = new IScriptContext() {
			@Override
			public Map<String, Object> getBindings() {
				return Collections.<String, Object>singletonMap("answer", 41);
			}
		};
		assertEquals(42L, se.executeWithoutHistory("answer+1", CalcScriptEngineFactory.NAME, context).getResult());
		assertEquals(4, se.getHistory().size());
	}
	
	@Test
	public void testMissingEngine() {
		ScriptExecutor se = ScriptExecutorHolder.get().getScriptExecutor();
		ScriptResult result = se.executeWithoutHistory("2+2", "NoSuchEngine", null);
		assertNotNull(result);
		assertEquals("NoSuchEngine", result.getEngine());
		assertEquals("2+2", result.getScript());
		assertNull(result.getResult());
		assertTrue(result.getError(), result.getError().contains("'NoSuchEngine' is not available"));
		assertTrue(result.getError(), result.getError().contains(CalcScriptEngineFactory.NAME));
		assertTrue(se.getHistory().isEmpty());
		result = se.executeWithoutHistory("2+2", null, null);
		assertNull(result.getResult());
		assertTrue(result.getError(), result.getError().contains("'null' is not available"));
	}
	
	@Test
	public void testDefaultEngineWithoutJavaScript() {
		assumeFalse("a JavaScript engine is on the classpath", ScriptExecutor.getSupportedEngines().contains(JAVASCRIPT));
		ScriptExecutor se = ScriptExecutorHolder.get().getScriptExecutor();
		// execute(command) defaults to "JavaScript": an error result in the history instead of a NullPointerException
		ScriptResult result = se.execute("2+2");
		assertEquals(JAVASCRIPT, result.getEngine());
		assertNull(result.getResult());
		assertTrue(result.getError(), result.getError().contains("'JavaScript' is not available"));
		assertEquals(1, se.getHistory().size());
		// The panel preselects the first available engine instead of the missing "JavaScript"
		tester.startComponentInPage(WicketConsolePanel.class);
		DropDownChoice<?> engineSelector = (DropDownChoice<?>)tester.getComponentFromLastRenderedPage("form:scriptEngine");
		assertEquals(sortedEngines().get(0), engineSelector.getModelObject());
	}
	
	@Test
	public void testRenderingWicketConsolePage()
	{
		tester.startPage(WicketConsolePage.class);
		tester.assertRenderedPage(WicketConsolePage.class);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testWicketConsoleComponent()
	{
		tester.startComponentInPage(WicketConsolePanel.class);
		tester.assertComponent("", WicketConsolePanel.class);
		tester.assertComponent("form", Form.class);
		tester.assertComponent("form:historyContainer", WebMarkupContainer.class);
		tester.assertComponent("form:historyContainer:history", WebMarkupContainer.class);
		tester.assertComponent("form:script", TextArea.class);
		ListView<ScriptResult> historyListView = (ListView<ScriptResult>)tester.getComponentFromLastRenderedPage("form:historyContainer:history");
		List<ScriptResult> history = historyListView.getModelObject();
		assertTrue(history.isEmpty());
		FormTester formTester = tester.newFormTester("form");
		formTester.setValue("script", "2+2");
		formTester.select("scriptEngine", Collections.binarySearch(sortedEngines(), CalcScriptEngineFactory.NAME));
		tester.executeAjaxEvent("form:submit", "click");
		history = historyListView.getModelObject();
		assertTrue(history.size()==1);
		ScriptResult item = history.get(0);
		assertEquals("2+2", item.getScript());
		assertEquals(CalcScriptEngineFactory.NAME, item.getEngine());

		Object ret = item.getResult();
		assertNotNull(ret);
		assertTrue(ret instanceof Number);
		assertEquals((double)4, ((Number)ret).doubleValue(), 0);
		assertTrue(Strings.isEmpty(item.getOut()));
		assertTrue(Strings.isEmpty(item.getError()));
		
		tester.executeAjaxEvent("form:clearHistory", "click");
		history = historyListView.getModelObject();
		assertTrue(history.isEmpty());
	}
	
	private static List<String> sortedEngines() {
		List<String> engines = new ArrayList<String>(ScriptExecutor.getSupportedEngines());
		Collections.sort(engines);
		return engines;
	}
}

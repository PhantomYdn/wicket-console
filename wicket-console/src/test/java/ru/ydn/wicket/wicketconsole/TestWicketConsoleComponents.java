package ru.ydn.wicket.wicketconsole;

import java.util.List;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebMarkupContainer;
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
import ru.ydn.wicket.wicketconsole.devutils.WicketConsolePage;

public class TestWicketConsoleComponents
{
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
		assertEquals(4, se.executeWithoutHistory("2+2","JavaScript",null).getResult());
		assertEquals(4.0, se.executeWithoutHistory("var a=2; a+a","JavaScript",null).getResult());
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
		tester.executeAjaxEvent("form:submit", "click");
		history = historyListView.getModelObject();
		assertTrue(history.size()==1);
		ScriptResult item = history.get(0);
		assertEquals("2+2", item.getScript());

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
}

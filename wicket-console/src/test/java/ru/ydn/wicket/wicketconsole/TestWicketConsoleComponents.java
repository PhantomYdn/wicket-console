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
		ListView<ScriptHistoryItem> historyListView = (ListView<ScriptHistoryItem>)tester.getComponentFromLastRenderedPage("form:historyContainer:history");
		List<ScriptHistoryItem> history = historyListView.getModelObject();
		assertTrue(history.isEmpty());
		FormTester formTester = tester.newFormTester("form");
		formTester.setValue("script", "2+2");
		tester.executeAjaxEvent("form:submit", "click");
		history = historyListView.getModelObject();
		assertTrue(history.size()==1);
		ScriptHistoryItem item = history.get(0);
		assertEquals("2+2", item.getScript());
		assertEquals(4.0, item.getResultObject().getReturnedObject());
		assertTrue(Strings.isEmpty(item.getResultObject().getOut()));
		assertTrue(Strings.isEmpty(item.getResultObject().getError()));
		
		tester.executeAjaxEvent("form:clearHistory", "click");
		history = historyListView.getModelObject();
		assertTrue(history.isEmpty());
	}
}

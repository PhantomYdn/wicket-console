package ru.ydn.wicket.wicketconsole;

import java.util.List;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;


public class WicketConsolePanel extends Panel
{

	public WicketConsolePanel(String id)
	{
		super(id);
		Form<String> form =new Form<String>("form");
		
		IModel<List<ScriptHistoryItem>> historyModel = new LoadableDetachableModel<List<ScriptHistoryItem>>() {

			@Override
			protected List<ScriptHistoryItem> load() {
				return ScriptExecutorHolder.get().getScriptExecutor().getHistory();
			}
		};
		
		ListView<ScriptHistoryItem> history = new ListView<ScriptHistoryItem>("history", historyModel) {
			
			@Override
			protected void populateItem(ListItem<ScriptHistoryItem> item) {
				item.add(new Label("item", new PropertyModel<String>(item.getModel(), "content")));
			}
		};
		form.add(history);
		final TextArea<String> script = new TextArea<String>("script", Model.of(""));
		form.add(script);
		form.add(new Button("submit")
		{
			@Override
			public void onSubmit() {
				ScriptExecutorHolder.get().getScriptExecutor().execute(script.getModelObject());
				script.setModelObject("");
			}
			
		});
		add(form);
	}

}

package ru.ydn.wicket.wicketconsole;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.HeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.string.Strings;

import ru.ydn.wicket.wicketconsole.behavior.CtrlEnterSubmitBehavior;
import ru.ydn.wicket.wicketconsole.behavior.ScrollToBottomBehavior;


public class WicketConsolePanel extends Panel
{
	private IModel<String> scriptModel = Model.of("");
	private IModel<Boolean> keepScriptModel = Model.of(false);
	private IModel<String> scriptEngineModel = Model.of("JavaScript");
	
	private WebMarkupContainer historyContainer;
	private TextArea<String> scriptTextArea;
	private DropDownChoice<String> engineSelector;
	
	private IScriptContext context;
	
	public WicketConsolePanel(String id){
		this(id,null);
	}
	
	public WicketConsolePanel(String id,IScriptContext context)
	{
		super(id);
		this.context = context;

		setOutputMarkupId(true);
		Form<String> form =new Form<String>("form");		
		scriptTextArea = new TextArea<String>("script", scriptModel);
		scriptTextArea.add(new CtrlEnterSubmitBehavior()
		{

			@Override
			protected void onSubmit(AjaxRequestTarget target) {
				onScriptExecute(target);
			}
			
		}).setOutputMarkupId(true);
		form.add(scriptTextArea);
		
		List<String> engines = new ArrayList<String>(ScriptExecutor.getSupportedEngines());
		Collections.sort(engines);
		engineSelector = new DropDownChoice<String>("scriptEngine",scriptEngineModel,engines);
		engineSelector.setOutputMarkupId(true);
		form.add(engineSelector);
		
		form.add(new AjaxCheckBox("keepScript", keepScriptModel) {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				//NOP
			}
		});
		form.add(new AjaxButton("submit")
		{
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				onScriptExecute(target);
			}
			
		});
		form.add(new AjaxButton("clearHistory")
		{
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				ScriptExecutorHolder.get().getScriptExecutor().getHistory().clear();
				target.add(historyContainer);
				target.focusComponent(scriptTextArea);
			}
			
		}.setDefaultFormProcessing(false));
		IModel<List<ScriptResult>> historyModel = new LoadableDetachableModel<List<ScriptResult>>() {

			@Override
			protected List<ScriptResult> load() {
				return ScriptExecutorHolder.get().getScriptExecutor().getHistory();
			}
		};
		historyContainer = new WebMarkupContainer("historyContainer");
		historyContainer.add(ScrollToBottomBehavior.INSTANCE).setOutputMarkupId(true);
		form.add(historyContainer);
		ListView<ScriptResult> history = new ListView<ScriptResult>("history", historyModel) {
			
			@Override
			protected void populateItem(ListItem<ScriptResult> item) {
				item.add(new HistoryItemPanel("item", item.getModel(),scriptTextArea,engineSelector));
			}
		};
		history.setReuseItems(true);
		historyContainer.add(history);

		add(form);
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		HeaderItem cssResource = getCSSResource();
		if(cssResource!=null) response.render(cssResource);
	}
	
	protected void onScriptExecute(AjaxRequestTarget target)
	{
		String commandScript = scriptModel.getObject();
		if(!Strings.isEmpty(commandScript))
		{
			String scriptEngineName = scriptEngineModel.getObject();
			if (!Strings.isEmpty(scriptEngineName)){
				ScriptExecutorHolder.get().getScriptExecutor().execute(commandScript,scriptEngineName,context);
			}else{
				ScriptExecutorHolder.get().getScriptExecutor().execute(commandScript);
			}
			
			if(!keepScriptModel.getObject())
			{
				scriptModel.setObject("");
				if(target!=null)
				{
					target.add(scriptTextArea);
					target.focusComponent(scriptTextArea);
				}
			}
			if(target!=null) target.add(historyContainer);
		}
	}
	
	protected HeaderItem getCSSResource()
	{
		return CssHeaderItem.forReference(new PackageResourceReference(WicketConsolePanel.class, "wicketconsole.css"));
	}

	public IModel<String> getScriptModel() {
		return scriptModel;
	}

	public void setScriptModel(IModel<String> scriptModel) {
		this.scriptModel = scriptModel;
	}

	public boolean isKeepScript() {
		return keepScriptModel.getObject();
	}

	public WicketConsolePanel setKeepScript(boolean keepScript) {
		this.keepScriptModel.setObject(keepScript);
		return this;
	}
	
	

}

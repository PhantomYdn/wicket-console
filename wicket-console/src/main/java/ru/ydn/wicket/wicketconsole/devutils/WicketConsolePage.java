package ru.ydn.wicket.wicketconsole.devutils;

import org.apache.wicket.devutils.DevUtilsPage;
import org.apache.wicket.devutils.inspector.InspectorPage;
import org.apache.wicket.devutils.inspector.LiveSessionsPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;

import ru.ydn.wicket.wicketconsole.WicketConsolePanel;

public class WicketConsolePage extends DevUtilsPage
{

	public WicketConsolePage(PageParameters parameters)
	{
		super(parameters);
		add(new WicketConsolePanel("console"));
		add(new Image("bug", new PackageResourceReference(InspectorPage.class, "bug.png")));
		add(new Label("wicketVersion", getApplication().getFrameworkSettings().getVersion()));
	}
	
	@Override
	public boolean isVersioned()
	{
		return false;
	}

}

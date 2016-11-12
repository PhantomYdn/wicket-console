package ru.ydn.wicket.wicketconsole.devutils;

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.devutils.debugbar.DebugBar;
import org.apache.wicket.devutils.debugbar.IDebugBarContributor;
import org.apache.wicket.devutils.debugbar.SessionSizeDebugPanel;
import org.apache.wicket.devutils.debugbar.StandardDebugPanel;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;

public class WicketConsoleDebugPanel extends StandardDebugPanel
{
	public static final IDebugBarContributor DEBUG_BAR_CONTRIB = new IDebugBarContributor()
	{
		private static final long serialVersionUID = 1L;

		@Override
		public Component createComponent(final String id, final DebugBar debugBar)
		{
			return new WicketConsoleDebugPanel(id);
		}

	};

	public WicketConsoleDebugPanel(String id)
	{
		super(id);
	}
	
	public static void initDebugBar(Application application)
	{
		if (application.getDebugSettings().isDevelopmentUtilitiesEnabled())
		{
			DebugBar.registerContributor(WicketConsoleDebugPanel.DEBUG_BAR_CONTRIB);
		}
	}

	@Override
	protected IModel<String> getDataModel() {
		return new ResourceModel("wc.panel.title");
	}

	@Override
	protected ResourceReference getImageResourceReference() {
		return new PackageResourceReference(SessionSizeDebugPanel.class, "harddrive.png");
	}

	@Override
	protected Class<? extends Page> getLinkPageClass() {
		return WicketConsolePage.getWicketConsolePageImplementation();
	}

}

package ru.ydn.wicket.wicketconsole;

import org.apache.wicket.Application;
import org.apache.wicket.IInitializer;
import ru.ydn.wicket.wicketconsole.devutils.WicketConsoleDebugPanel;

public class Initializer implements IInitializer
{
	private static final String DEBUG_BAR_CLASS = "org.apache.wicket.devutils.debugbar.DebugBar";
//	private static final String REGISTER_CONTRIBUTOR_METHOD = "registerContributor";

	@Override
	public void init(Application application) {
		new ScriptExecutorHolder().init(application);
		try
		{
			Class.forName(DEBUG_BAR_CLASS);
			WicketConsoleDebugPanel.initDebugBar(application);
		} catch (ClassNotFoundException e)
		{
			// NOP
		}
	}

	@Override
	public void destroy(Application application) {
		ScriptExecutorHolder.get(application).destroy(application);
	}

}

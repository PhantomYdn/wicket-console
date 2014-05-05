package ru.ydn.wicket.wicketconsole;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Application;
import org.apache.wicket.IInitializer;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.Session;
import org.apache.wicket.request.Request;
import org.apache.wicket.session.ISessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptExecutorHolder implements ISessionStore.BindListener, ISessionStore.UnboundListener, IInitializer
{
	private static final Logger LOG = LoggerFactory.getLogger(ScriptExecutorHolder.class);
	public static final MetaDataKey<ScriptExecutorHolder> SCRIPT_EXECUTOR_HOLDER = new MetaDataKey<ScriptExecutorHolder>() {};

	private Map<String, ScriptExecutor> scriptExecutorsCache = new HashMap<String, ScriptExecutor>();
	
	public void sessionUnbound(String sessionId) {
		scriptExecutorsCache.remove(sessionId);
	}

	public void bindingSession(Request request, Session newSession) {
	}

	public void init(Application application) {
		ISessionStore store = application.getSessionStore();
		store.registerBindListener(this);
		store.registerUnboundListener(this);
		application.setMetaData(SCRIPT_EXECUTOR_HOLDER, this);
	}

	public void destroy(Application application) {
		
	}
	
	public static ScriptExecutorHolder get()
	{
		return Application.get().getMetaData(SCRIPT_EXECUTOR_HOLDER);
	}
	
	public ScriptExecutor getScriptExecutor()
	{
		Session session = Session.get();
		String sessionId = session.getId();
		ScriptExecutor executor = sessionId!=null?scriptExecutorsCache.get(sessionId):null;
		if(executor==null)
		{
			executor = new ScriptExecutor();
			if(session.isTemporary()) session.bind();
			scriptExecutorsCache.put(sessionId, executor);
		}
		return executor;
	}
}

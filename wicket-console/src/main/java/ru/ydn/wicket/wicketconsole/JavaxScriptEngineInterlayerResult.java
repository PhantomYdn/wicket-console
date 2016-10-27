package ru.ydn.wicket.wicketconsole;

import java.io.StringReader;
import java.io.StringWriter;

import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;

public class JavaxScriptEngineInterlayerResult implements IScriptEngineInterlayerResult{

	private ScriptContext ctx;
	private transient Object result;

	public JavaxScriptEngineInterlayerResult() {
		this.ctx = new SimpleScriptContext();
		ctx.setWriter(new StringWriter());
		ctx.setErrorWriter(new StringWriter());
		ctx.setReader(new StringReader(""));
	}
	
	protected ScriptContext getScriptContext(){
		return ctx;
	}

	@Override
	public String getOut() {
		return getContent((StringWriter)ctx.getWriter());
	}

	@Override
	public String getError() {
		return getContent((StringWriter)ctx.getErrorWriter());
	}
	
	private static String getContent(StringWriter writer)
	{
		StringBuffer buf = writer.getBuffer();
		String ret = buf.toString();
		//buf.setLength(0);
		return ret;
	}

	@Override
	public Object getResult() {
		return result;
	}

	protected void setResult(Object result) {
		this.result = result;
	}

}

package ru.ydn.wicket.wicketconsole;

import java.io.StringReader;
import java.io.StringWriter;

import javax.script.ScriptContext;
import javax.script.SimpleScriptContext;

public class JavaxScriptEngineInterlayerResult implements IScriptEngineInterlayerResult{

	private static final long serialVersionUID = 1L;
	
	private ScriptContext ctx;
	private transient Object result;
	
	private String out;
	private String error;
	private IScriptEngineInterlayerResultRenderer renderer;

	public JavaxScriptEngineInterlayerResult() {
		this.ctx = new SimpleScriptContext();
		ctx.setWriter(new StringWriter());
		ctx.setErrorWriter(new StringWriter());
		ctx.setReader(new StringReader(""));
		renderer = new JavaxResultSimpleRenderer(this);
	}
	
	protected ScriptContext getScriptContext(){
		return ctx;
	}

	@Override
	public String getOut() {
		return out;
	}

	@Override
	public String getError() {
		return error;
	}
	
	private static String getContentAndClear(StringWriter writer)
	{
		StringBuffer buf = writer.getBuffer();
		String ret = buf.toString();
		buf.setLength(0);
		return ret;
	}

	@Override
	public Object getResult() {
		return result;
	}

	protected void setResult(Object result) {
		out = getContentAndClear((StringWriter)ctx.getWriter());
		error = getContentAndClear((StringWriter)ctx.getErrorWriter()); 
		this.result = result;
	}

	public IScriptEngineInterlayerResultRenderer getRenderer() {
		return renderer;
	}

	public void setRenderer(IScriptEngineInterlayerResultRenderer renderer) {
		this.renderer = renderer;
	}
	
	
	

}

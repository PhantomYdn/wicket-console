package ru.ydn.wicket.wicketconsole.calc;

import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

/**
 * Tiny JSR-223 engine for the tests. Statements are separated by {@code ;} or new lines (so string literals can't
 * contain them); the value of the last statement is the result:
 * <ul>
 * <li>{@code [var] name = expr} assigns to the engine-scope bindings</li>
 * <li>{@code print(expr)} writes the value and a new line to the context's writer</li>
 * <li>{@code fail(expr)} throws a {@link ScriptException}</li>
 * <li>{@code expr}: numbers, {@code "strings"} and variables (engine or global scope) combined with {@code +} and
 * {@code *} and parentheses; {@code +} with a string concatenates</li>
 * </ul>
 */
public class CalcScriptEngine extends AbstractScriptEngine
{
	private static final Pattern ASSIGNMENT = Pattern.compile("(?:var\\s+)?([A-Za-z_$][\\w$]*)\\s*=\\s*(.+)", Pattern.DOTALL);
	private static final Pattern CALL = Pattern.compile("(print|fail)\\s*\\((.*)\\)", Pattern.DOTALL);

	private final ScriptEngineFactory factory;

	public CalcScriptEngine(ScriptEngineFactory factory) {
		this.factory = factory;
	}

	@Override
	public Object eval(String script, ScriptContext context) throws ScriptException {
		Object result = null;
		for(String statement : script.split("[;\\n]")) {
			statement = statement.trim();
			if(!statement.isEmpty()) result = statement(statement, context);
		}
		return result;
	}

	@Override
	public Object eval(Reader reader, ScriptContext context) throws ScriptException {
		StringWriter script = new StringWriter();
		char[] buf = new char[1024];
		try {
			for(int read; (read = reader.read(buf)) >= 0;) script.write(buf, 0, read);
		} catch (IOException e) {
			throw new ScriptException(e);
		}
		return eval(script.toString(), context);
	}

	@Override
	public Bindings createBindings() {
		return new SimpleBindings();
	}

	@Override
	public ScriptEngineFactory getFactory() {
		return factory;
	}

	private Object statement(String statement, ScriptContext context) throws ScriptException {
		Matcher m = ASSIGNMENT.matcher(statement);
		if(m.matches()) {
			Object value = new Parser(m.group(2), context).parse();
			context.getBindings(ScriptContext.ENGINE_SCOPE).put(m.group(1), value);
			return value;
		}
		m = CALL.matcher(statement);
		if(m.matches()) {
			Object value = new Parser(m.group(2), context).parse();
			if("fail".equals(m.group(1))) throw new ScriptException(String.valueOf(value));
			try {
				context.getWriter().write(value + "\n");
				context.getWriter().flush();
			} catch (IOException e) {
				throw new ScriptException(e);
			}
			return null;
		}
		return new Parser(statement, context).parse();
	}

	/** Recursive descent over one expression: sum := product ('+' product)*, product := term ('*' term)* */
	private static final class Parser
	{
		private final String src;
		private final ScriptContext context;
		private int pos;

		Parser(String src, ScriptContext context) {
			this.src = src;
			this.context = context;
		}

		Object parse() throws ScriptException {
			Object value = sum();
			skipSpaces();
			if(pos < src.length()) throw new ScriptException("Unexpected '" + src.charAt(pos) + "' in: " + src);
			return value;
		}

		private Object sum() throws ScriptException {
			Object value = product();
			while(consume('+')) {
				Object right = product();
				if(value instanceof Number && right instanceof Number) value = arithmetic(value, right, '+');
				else value = String.valueOf(value) + right;
			}
			return value;
		}

		private Object product() throws ScriptException {
			Object value = term();
			while(consume('*')) {
				Object right = term();
				if(!(value instanceof Number && right instanceof Number)) {
					throw new ScriptException("Can't multiply " + value + " by " + right);
				}
				value = arithmetic(value, right, '*');
			}
			return value;
		}

		private Object term() throws ScriptException {
			skipSpaces();
			if(pos >= src.length()) throw new ScriptException("Unexpected end of: " + src);
			char c = src.charAt(pos);
			if(consume('(')) {
				Object value = sum();
				if(!consume(')')) throw new ScriptException("Missing ')' in: " + src);
				return value;
			} else if(c == '"') {
				int end = src.indexOf('"', pos + 1);
				if(end < 0) throw new ScriptException("Unterminated string in: " + src);
				String value = src.substring(pos + 1, end);
				pos = end + 1;
				return value;
			} else if(Character.isDigit(c)) {
				int start = pos;
				while(pos < src.length() && (Character.isDigit(src.charAt(pos)) || src.charAt(pos) == '.')) pos++;
				String number = src.substring(start, pos);
				return number.indexOf('.') >= 0 ? (Object) Double.valueOf(number) : (Object) Long.valueOf(number);
			} else if(Character.isJavaIdentifierStart(c)) {
				int start = pos;
				while(pos < src.length() && Character.isJavaIdentifierPart(src.charAt(pos))) pos++;
				String name = src.substring(start, pos);
				if(context.getAttributesScope(name) < 0) throw new ScriptException(name + " is not defined");
				return context.getAttribute(name);
			}
			throw new ScriptException("Unexpected '" + c + "' in: " + src);
		}

		private static Object arithmetic(Object left, Object right, char op) {
			Number a = (Number) left;
			Number b = (Number) right;
			boolean integral = !(a instanceof Double || a instanceof Float || b instanceof Double || b instanceof Float);
			if(integral) return op == '+' ? a.longValue() + b.longValue() : a.longValue() * b.longValue();
			return op == '+' ? a.doubleValue() + b.doubleValue() : a.doubleValue() * b.doubleValue();
		}

		private boolean consume(char c) {
			skipSpaces();
			if(pos < src.length() && src.charAt(pos) == c) {
				pos++;
				return true;
			}
			return false;
		}

		private void skipSpaces() {
			while(pos < src.length() && Character.isWhitespace(src.charAt(pos))) pos++;
		}
	}
}

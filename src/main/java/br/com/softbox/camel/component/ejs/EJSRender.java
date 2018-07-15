package br.com.softbox.camel.component.ejs;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;


public class EJSRender {
	private static final String SCRIPT = getLoadCommandsDependencies();
	
	private static String getLoadCommandsDependencies() {
		return Arrays.asList(
			"/ejs/json.js",
			"/ejs/utils.js",
			"/ejs/ejs.js",
			"/ejs/ejs-render.js")
		.stream()
		.map((resourceName) ->  "load('" + EJSRender.class.getResource(resourceName) + "');")
		.collect(Collectors.joining("\n"));
	}
	
	private static String readToString(InputStream inputStream) throws Exception {
		ByteArrayOutputStream result = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length;
		
		while ((length = inputStream.read(buffer)) != -1) {
			result.write(buffer, 0, length);
		}
		
		return result.toString("UTF-8");
	}
	
	/**
	 * Renderiza o template fornecido o stream de entrada.
	 * 
	 * @param is Input stream do template.
	 * @param bindings Valores que serao mesclados no template.
	 * 
	 * @return String restante da mesclagem do template com os valores fornecidos.
	 * 
	 * @throws Exception
	 */
	public static String render(InputStream is, Map<String, Object> bindings) throws Exception {
		ScriptEngineManager sem = new ScriptEngineManager();
		
		ScriptEngine engine = sem.getEngineByName("JavaScript");
		
		String template = readToString(is);
		
		engine.put("bindings", bindings);
		engine.put("template", template);
		
		return (String) engine.eval(SCRIPT + "\n;ejs.render(template, toJS(bindings || {}));");
	}
}
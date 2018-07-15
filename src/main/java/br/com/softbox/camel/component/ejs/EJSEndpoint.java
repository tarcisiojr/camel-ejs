package br.com.softbox.camel.component.ejs;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Message;
import org.apache.camel.component.ResourceEndpoint;
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.util.ExchangeHelper;
import org.apache.camel.util.ObjectHelper;

@UriEndpoint(firstVersion = "1.0.0", scheme = "ejs", title = "EJS", syntax = "ejs:resourceUri", producerOnly = true, label = "transformation")
public class EJSEndpoint extends ResourceEndpoint {

	public static final String EJS_RESOURCE_URI = "CamelEJSResourceUri";

	public static final String EJS_TEMPLATE = "CamelEJSTemplate";

	public static final String EJS_CONTEXT = "CamelEJSContext";

	@UriParam(defaultValue = "true")
	private boolean loaderCache = true;
	@UriParam
	private String encoding;
	@UriParam
	private String propertiesFile;

	public EJSEndpoint() {
	}

	public EJSEndpoint(String uri, EJSComponent component, String resourceUri) {
		super(uri, component, resourceUri);
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	@Override
	public ExchangePattern getExchangePattern() {
		return ExchangePattern.InOut;
	}

	@Override
	protected String createEndpointUri() {
		return "ejs:" + getResourceUri();
	}

	public boolean isLoaderCache() {
		return loaderCache;
	}

	public void setLoaderCache(boolean loaderCache) {
		this.loaderCache = loaderCache;
	}

	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}

	public String getEncoding() {
		return encoding;
	}

	public EJSEndpoint findOrCreateEndpoint(String uri, String newResourceUri) {
		String newUri = uri.replace(getResourceUri(), newResourceUri);
		log.debug("Getting endpoint with URI: {}", newUri);
		return getCamelContext().getEndpoint(newUri, EJSEndpoint.class);
	}

	@Override
	protected void onExchange(Exchange exchange) throws Exception {
		String path = getResourceUri();
		ObjectHelper.notNull(path, "resourceUri");

		String newResourceUri = exchange.getIn().getHeader(EJSEndpoint.EJS_RESOURCE_URI, String.class);
		if (newResourceUri != null) {
			exchange.getIn().removeHeader(EJSEndpoint.EJS_RESOURCE_URI);

			log.debug("{} set to {} creating new endpoint to handle exchange", EJSEndpoint.EJS_RESOURCE_URI,
					newResourceUri);
			EJSEndpoint newEndpoint = findOrCreateEndpoint(getEndpointUri(), newResourceUri);
			newEndpoint.onExchange(exchange);
			return;
		}

		InputStream inputStream;
		String content = exchange.getIn().getHeader(EJSEndpoint.EJS_TEMPLATE, String.class);
		if (content != null) {
			// use content from header
			inputStream = new ByteArrayInputStream(content.getBytes());
			if (log.isDebugEnabled()) {
				log.debug("EJS content read from header {} for endpoint {}", EJSEndpoint.EJS_TEMPLATE,
						getEndpointUri());
			}
			// remove the header to avoid it being propagated in the routing
			exchange.getIn().removeHeader(EJSEndpoint.EJS_TEMPLATE);
		} else {
			if (log.isDebugEnabled()) {
				log.debug("EJS content read from resource {} with resourceUri: {} for endpoint {}",
						new Object[] { getResourceUri(), path, getEndpointUri() });
			}
			inputStream = getResourceAsInputStream(); // getEncoding();
		}

		Map<String, Object> variableMap = ExchangeHelper.createVariableMap(exchange);

		String renderedTemplate = EJSRender.render(inputStream, variableMap);

		// now lets output the results to the exchange
		Message out = exchange.getOut();
		out.setBody(renderedTemplate);
		out.setHeaders(exchange.getIn().getHeaders());
		out.setAttachments(exchange.getIn().getAttachments());
	}
}
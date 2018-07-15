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


/**
 * Transforms the message using a Velocity template.
 */
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

//    private synchronized VelocityEngine getVelocityEngine() throws Exception {
//        if (velocityEngine == null) {
//            velocityEngine = new VelocityEngine();
//
//            // set the class resolver as a property so we can access it from CamelVelocityClasspathResourceLoader
//            velocityEngine.addProperty("CamelClassResolver", getCamelContext().getClassResolver());
//
//            // set default properties
//            Properties properties = new Properties();
//            properties.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_CACHE, isLoaderCache() ? "true" : "false");
//            properties.setProperty(RuntimeConstants.RESOURCE_LOADER, "file, class");
//            properties.setProperty("class.resource.loader.description", "Camel Velocity Classpath Resource Loader");
//            properties.setProperty("class.resource.loader.class", CamelVelocityClasspathResourceLoader.class.getName());
//            final Logger velocityLogger = LoggerFactory.getLogger("org.apache.camel.maven.Velocity");
//            properties.setProperty(RuntimeConstants.RUNTIME_LOG_NAME, velocityLogger.getName());
//            
//          
//
//            // load the velocity properties from property file which may overrides the default ones
//            if (ObjectHelper.isNotEmpty(getPropertiesFile())) {
//                InputStream reader = ResourceHelper.resolveMandatoryResourceAsInputStream(getCamelContext(), getPropertiesFile());
//                try {
//                    properties.load(reader);
//                    log.info("Loaded the velocity configuration file " + getPropertiesFile());
//                } finally {
//                    IOHelper.close(reader, getPropertiesFile(), log);
//                }
//            }
//
//            log.debug("Initializing VelocityEngine with properties {}", properties);
//            // help the velocityEngine to load the CamelVelocityClasspathResourceLoader
//            ClassLoader old = Thread.currentThread().getContextClassLoader();
//            try {
//                ClassLoader delegate = new CamelVelocityDelegateClassLoader(old);
//                Thread.currentThread().setContextClassLoader(delegate);
//                velocityEngine.init(properties);
//            } finally {
//                Thread.currentThread().setContextClassLoader(old);
//            }
//        }
//        return velocityEngine;
//    }
//
//    public void setVelocityEngine(VelocityEngine velocityEngine) {
//        this.velocityEngine = velocityEngine;
//    }

    public boolean isLoaderCache() {
        return loaderCache;
    }

    /**
     * Enables / disables the velocity resource loader cache which is enabled by default
     */
    public void setLoaderCache(boolean loaderCache) {
        this.loaderCache = loaderCache;
    }

    /**
     * Character encoding of the resource content.
     */
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public String getEncoding() {
        return encoding;
    }

    /**
     * The URI of the properties file which is used for VelocityEngine initialization.
     */
    public void setPropertiesFile(String file) {
        propertiesFile = file;
    }

    public String getPropertiesFile() {
        return propertiesFile;
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

            log.debug("{} set to {} creating new endpoint to handle exchange", EJSEndpoint.EJS_RESOURCE_URI, newResourceUri);
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
                log.debug("EJS content read from header {} for endpoint {}", EJSEndpoint.EJS_TEMPLATE, getEndpointUri());
            }
            // remove the header to avoid it being propagated in the routing
            exchange.getIn().removeHeader(EJSEndpoint.EJS_TEMPLATE);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("EJS content read from resource {} with resourceUri: {} for endpoint {}", new Object[]{getResourceUri(), path, getEndpointUri()});
            }
            inputStream = getResourceAsInputStream(); // getEncoding();
        }

        // getResourceAsInputStream also considers the content cache
//        String logTag = getClass().getName();
//        Context velocityContext = exchange.getIn().getHeader(VelocityConstants.VELOCITY_CONTEXT, Context.class);
//        if (velocityContext == null) {
//            Map<String, Object> variableMap = ExchangeHelper.createVariableMap(exchange);
//
//            @SuppressWarnings("unchecked")
//            Map<String, Object> supplementalMap = exchange.getIn().getHeader(VelocityConstants.VELOCITY_SUPPLEMENTAL_CONTEXT, Map.class);
//            if (supplementalMap != null) {
//                variableMap.putAll(supplementalMap);
//            }
//
//            velocityContext = new VelocityContext(variableMap);
//        }
//
//        // let velocity parse and generate the result in buffer
//        VelocityEngine engine = getVelocityEngine();
//        log.debug("Velocity is evaluating using velocity context: {}", velocityContext);
//        engine.evaluate(velocityContext, buffer, logTag, reader);

        Map<String, Object> variableMap = ExchangeHelper.createVariableMap(exchange);
        
        String renderedTemplate = EJSRender.render(inputStream, variableMap);
        
        // now lets output the results to the exchange
        Message out = exchange.getOut();
        out.setBody(renderedTemplate);
        out.setHeaders(exchange.getIn().getHeaders());
        out.setAttachments(exchange.getIn().getAttachments());
    }
}
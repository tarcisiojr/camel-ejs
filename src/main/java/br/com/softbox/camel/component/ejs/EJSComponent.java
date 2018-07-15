package br.com.softbox.camel.component.ejs;

import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.UriEndpointComponent;
import org.apache.camel.util.ResourceHelper;

@SuppressWarnings("deprecation")
public class EJSComponent extends UriEndpointComponent {
    
	public EJSComponent() {
        super(EJSEndpoint.class);
    }


    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        boolean cache = getAndRemoveParameter(parameters, "contentCache", Boolean.class, Boolean.TRUE);

        EJSEndpoint answer = new EJSEndpoint(uri, this, remaining);
        setProperties(answer, parameters);
        answer.setContentCache(cache);

        // if its a http resource then append any remaining parameters and update the resource uri
        if (ResourceHelper.isHttpUri(remaining)) {
            remaining = ResourceHelper.appendParameters(remaining, parameters);
            answer.setResourceUri(remaining);
        }

        return answer;
    }
}
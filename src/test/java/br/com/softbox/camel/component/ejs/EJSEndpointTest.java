package br.com.softbox.camel.component.ejs;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Test;

public class EJSEndpointTest extends CamelTestSupport {

//    @EndpointInject(uri = "mock:result")
//    protected MockEndpoint resultEndpoint;
    
//    @EndpointInject(uri = "ejs:result")
//    protected EJSEndpoint ejsEndpoint;

//    @Produce(uri = "direct:start")
//    protected ProducerTemplate template;

    @Test
    public void testCheckTemplate() throws Exception {
    	Exchange exchange = template.request("direct:start", new Processor() {
			
			@Override
			public void process(Exchange exchange) throws Exception {
				Map<String, Object> body = new HashMap<>();
				
				body.put("who", "World");
				
				exchange.getIn().setBody(body);
			}
		});

    	final Object body = exchange.getOut().getBody();
    	
    	final String expectedBody = "Hello World!";

    	assertIsInstanceOf(String.class, body);
    	assertEquals(expectedBody, body);
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() throws Exception {
            	EJSEndpoint ejsEndpoint = new EJSEndpoint();
            	ejsEndpoint.setCamelContext(context);
            	ejsEndpoint.setResourceUri("br/com/softbox/camel/component/ejs/nop.ejs");
            	
            	context.addEndpoint("ejs", ejsEndpoint);
            	
                from("direct:start").to("ejs");
            }
        };
    }

}

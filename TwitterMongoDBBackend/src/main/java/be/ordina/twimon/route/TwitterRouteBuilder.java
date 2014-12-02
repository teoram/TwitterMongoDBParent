package be.ordina.twimon.route;

import org.apache.camel.builder.RouteBuilder;

import com.mongodb.DBObject;

import twitter4j.TwitterException;

public class TwitterRouteBuilder extends RouteBuilder {

	@Override
	public void configure() throws Exception {
			
		
		from("servlet:///retrieveTweets")
			.routeId("servlet:///retrieveTweets")
			.to("seda:retrieveTweetsByPaging?waitForTaskToComplete=Never")
			.transform(constant("OK"))
		.end();
		
		from("seda:retrieveTweetsByPaging")
			.routeId("seda:retrieveTweetsByPaging")
			.split().method("tweetServiceBean", "retrieveTweetsByPagingCollection")
				.to("seda:handleSingleQuery?waitForTaskToComplete=Never")
		.end();
		
		
		
		// Access us using http://localhost:8080/camel/hello
        from("servlet:///hello").transform().constant("Hello from Camel!");
        
        
        from("servlet:///hello2")
        	.routeId("hello2")
        	.to("direct:test")
        .end();
        
        from("direct:test")
        	.routeId("direct:test")
        	.transform().constant("Hello from Camel via 2nd route !")
        .end();
        
        
        from("servlet:///getMessages")
        	.routeId("servlet:///getMessages")
        	.to("bean:messageConsumer?method=consume")
        	.transform().body()
        .end();
	}

}

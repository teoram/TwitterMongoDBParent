package be.ordina.twimon.route;

import org.apache.camel.builder.RouteBuilder;

import twitter4j.TwitterException;

public class TwitterRouteBuilder extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		
		onException(TwitterException.class)
			.maximumRedeliveries(5)
			.delay(300000l)
			.to("direct:retrieveTweetsByPaging")
		.end();
		
		
		from("servlet:///retrieveTweets")
			.to("direct:retrieveTweetsByPaging")
		.end();
		
		from("direct:retrieveTweetsByPaging")
			.split().method("tweetServiceBean", "retrieveTweetsByPagingCollection")
				.to("direct:handleSingleQuery")
		.end();
		
		from("direct:handleSingleQuery")
			.to("bean:tweetServiceBean?method=handleRetrieveTweetsForSingleQuery")
			.transform().body()
			.to("seda:messageQueue")
			.choice()
                .when(header("continue").isEqualTo("true"))
                    .to("direct:retrieveTweetsByPaging")
                .otherwise()
                	.transform().simple("No more data to cralw for {in.body}")
                	.to("seda:messageQueue")
             .end()
		.end();
			
		from("direct:handleException")
			.choice()
				.when(header("continue").isEqualTo("true"))
                    .to("direct:retrieveTweetsByPaging")
                .otherwise()
                	.transform().simple("No more data to cralw for {in.body}")
                	.to("seda:messageQueue")
             .end()
        .end();
			
		
		
		// Access us using http://localhost:8080/camel/hello
        from("servlet:///hello").transform().constant("Hello from Camel!");
        
        
        from("servlet:///getMessages")
        	.to("bean:messageConsumer?method=consume")
        	.transform().body()
        .end();
	}

}

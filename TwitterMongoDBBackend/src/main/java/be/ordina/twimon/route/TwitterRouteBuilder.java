package be.ordina.twimon.route;

import org.apache.camel.builder.RouteBuilder;

import com.mongodb.DBObject;

import twitter4j.TwitterException;

public class TwitterRouteBuilder extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		
		onException(TwitterException.class)
			.maximumRedeliveries(5)
			.setHeader("body", body())
			.transform().constant(">>>>>>>>>>>>>>>>> will handle exception")
			.to("stream:out")
			.removeHeader("body")
			.transform().header("body")
			.delay(300000l)
			.to("seda:retrieveTweetsByPaging?waitForTaskToComplete=Never")
			.transform().constant(">>>>>>>>>>>>>>>>> handled exception")
			.to("stream:out")
		.end();
		
		
		from("servlet:///retrieveTweets")
			.to("seda:retrieveTweetsByPaging?waitForTaskToComplete=Never")
			.transform(constant("OK"))
		.end();
		
		from("seda:retrieveTweetsByPaging")
			.split().method("tweetServiceBean", "retrieveTweetsByPagingCollection")
				.to("seda:handleSingleQuery?waitForTaskToComplete=Never")
		.end();
		
		from("seda:handleSingleQuery")
			.setHeader("originalBody", body())
			.to("bean:tweetServiceBean?method=handleRetrieveTweetsForSingleQuery")
			.setHeader("newBody", body())
			.transform().simple("completed query for ${body.get('festival')}")
			.to("seda:messageQueue?waitForTaskToComplete=Never")
			.log("orginalMax = ${header.orginalBody.get('maxId')}")
			.log("orginalMax = ${header.orginalBody}")
			.log("newMax = " + header("newBody.get('maxId')"))
			.choice()
                .when((header("originalBody.get('maxId')")).isNotEqualTo((header("newBody.get('maxId')"))))
                	.transform().header("newBody")
                	.log("going to Repeat the fetch for ${body.get('festival')}")
                    .to("seda:handleSingleQuery?waitForTaskToComplete=Never")
                    .transform(constant("OK"))
                .otherwise()
	                .transform().header("newBody")
                	.transform().simple("No more data to crawl for ${body.get('festival')}")
                	.log("completed a query for a festival")
                	.to("seda:messageQueue?waitForTaskToComplete=Never")
                	.transform(constant("OK"))
             .end()
		.end();
			
		
		// Access us using http://localhost:8080/camel/hello
        from("servlet:///hello").transform().constant("Hello from Camel!");
        
        
        from("servlet:///hello2")
        	.to("direct:test")
        .end();
        
        from("direct:test")
        	.transform().constant("Hello from Camel via 2nd route !")
        .end();
        
        
        from("servlet:///getMessages")
        	.to("bean:messageConsumer?method=consume")
        	.transform().body()
        .end();
	}

}

package be.ordina.twimon.route;

import org.apache.camel.builder.RouteBuilder;

import twitter4j.TwitterException;

public class TwitterRouteWithExceptionBuilder extends RouteBuilder {

	@Override
	public void configure() throws Exception {
		
		onException(TwitterException.class)
			.routeId("twitterExceptionHandler")
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
		
		from("seda:handleSingleQuery")
			.routeId("seda:handleSingleQuery")
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
                	.log("going to Repeat the fetch for ${body.get('follow')}")
                    .to("seda:handleSingleQuery?waitForTaskToComplete=Never")
                    .transform(constant("OK"))
                .otherwise()
	                .transform().header("newBody")
                	.transform().simple("No more data to crawl for ${body.get('follow')}")
                	.log("completed a query for a festival")
                	.to("seda:messageQueue?waitForTaskToComplete=Never")
                	.transform(constant("OK"))
             .end()
		.end();
			
	}

}

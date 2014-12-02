package be.ordina.twimon.web;

import java.util.Arrays;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.metrics.routepolicy.MetricsRoutePolicyFactory;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.spring.SpringCamelContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.context.embedded.ServletRegistrationBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import be.ordina.twimon.route.MessageConsumer;
import be.ordina.twimon.route.TwitterRouteBuilder;
import be.ordina.twimon.route.TwitterRouteWithExceptionBuilder;
import be.ordina.twimon.service.MessageService;
import be.ordina.twimon.service.TweetService;
import be.ordina.twimon.service.impl.MessageServiceImpl;
import be.ordina.twimon.service.impl.TweetServiceImpl;
import be.ordina.twimon.web.config.ConnectionSettings;

@Configuration
@EnableConfigurationProperties
@EnableAutoConfiguration(exclude = {MongoRepositoriesAutoConfiguration.class, MongoAutoConfiguration.class})
@ComponentScan
public class Application {
	
	private static final String CAMEL_URL_MAPPING = "/camel/*";
    private static final String CAMEL_SERVLET_NAME = "CamelServlet";
    
    private static ApplicationContext applicationContext;

    public static void main(String[] args) {
        applicationContext = SpringApplication.run(Application.class, args);

        System.out.println("Let's inspect the beans provided by Spring Boot:");

        String[] beanNames = applicationContext.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        for (String beanName : beanNames) {
            System.out.println("bean: " + beanName);
        }
    }
    
    @Autowired
    private ConnectionSettings connectionSettings;
    
    @Bean
    public TweetService tweetServiceBean() {
    	return new TweetServiceImpl(connectionSettings.getConsumerKey()
    			, connectionSettings.getConsumerSecret()
    			, connectionSettings.getAccessToken()
    			, connectionSettings.getAccessTokenSecret());
    }
    
    @Bean
    public MessageService messageServiceBean() throws Exception {
    	return new MessageServiceImpl(camelContext(applicationContext));
    }
    
    
    @Bean
    public ServletRegistrationBean servletRegistrationBean() {
        ServletRegistrationBean registration = 
        		new ServletRegistrationBean(new CamelHttpTransportServlet(), CAMEL_URL_MAPPING);
        registration.setName(CAMEL_SERVLET_NAME);
        return registration;
    }
 
    @Bean
    public SpringCamelContext camelContext(ApplicationContext applicationContext) throws Exception {
        SpringCamelContext camelContext = new SpringCamelContext(applicationContext);
       camelContext.addRoutes(routeBuilder());
       camelContext.addRoutes(routeBuilderWithException());
       
       camelContext.addRoutePolicyFactory(new MetricsRoutePolicyFactory());
       
       
       MessageConsumer messageConsumer = messageConsumer();
       messageConsumer.setConsumer(camelContext.createConsumerTemplate());
       messageConsumer.setProducer(camelContext.createProducerTemplate());
        
       return camelContext;
    }
 
    @Bean
    public RouteBuilder routeBuilder() {
        return new TwitterRouteBuilder();
    }
    @Bean
    public RouteBuilder routeBuilderWithException() {
    	return new TwitterRouteWithExceptionBuilder();
    }
    
    @Bean
    public MessageConsumer messageConsumer() {
    	return new  MessageConsumer();
    }
    
    

}
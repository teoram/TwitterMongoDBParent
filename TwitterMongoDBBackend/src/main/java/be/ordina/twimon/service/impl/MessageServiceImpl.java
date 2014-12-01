package be.ordina.twimon.service.impl;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.annotation.Autowired;

import be.ordina.twimon.service.MessageService;

public class MessageServiceImpl implements MessageService{
	
	private CamelContext camelContext;
	
	@Autowired
	public MessageServiceImpl(CamelContext camelContext) {
		this.camelContext = camelContext;
	}
	
	@Override
	public String getMessageFromQueue() {
		
		try {
			Exchange camelExchange = 
					camelContext.getEndpoint("seda:messageQueue").createPollingConsumer().receiveNoWait();
			
			if (camelExchange != null) {
				return camelExchange.getIn().getBody().toString();
			}
		} catch (Exception e) {
			e.printStackTrace();
			return "ERROR";
		}
		
		return "No messages found";
		
	}
	
	
	

}
package be.ordina.twimon.route;

import org.apache.camel.ConsumerTemplate;
import org.apache.camel.ProducerTemplate;

public class MessageConsumer {
	
	
	private int count;
    private ConsumerTemplate consumer;
    private ProducerTemplate producer;
 
    public void setConsumer(ConsumerTemplate consumer) {
        this.consumer = consumer;
    }
 
    public void setProducer(ProducerTemplate producer) {
        this.producer = producer;
        this.producer.setDefaultEndpointUri("seda:messageQueue");
    }
 
    public String consume() {
        // loop to empty queue
    	String msgBody = "";
    	System.out.println("msgBody set");
    	
    	int count = 1;
    	
        while (count < 5) {
            // receive the message from the queue, wait at most 1 sec
            String msg = consumer.receiveBody("seda:messageQueue", 1000, String.class);
            System.out.println("msg received = " + msg);
            if (msg == null) {
                // no more messages in queue
                break;
            }

            // do something with body
            msgBody = msgBody + "Message " + count + ": "+ msg + "<br/>";
            
            count = count + 1; 
        }
        
        if (msgBody.length() == 0) {
        	msgBody = "No messages on queue";
        }
        
        System.out.println(msgBody);
        //producer.sendBody(msgBody);
        return msgBody;
    }

}

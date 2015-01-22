package storm.tpb.kafka;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import storm.tpb.testing.*;

import java.util.TimerTask;
import java.util.Timer;

public class ProducerTest  extends TimerTask {
	private final ProducerConfig config;
	Producer<String, String> producer ;
	private final String topic;
	private static long rangeTime=0;
	public static long count = 0;
	public ProducerTest(String topic,int runningtime) {
		Properties props = new Properties();
		//props.put("zk.connect", "127.0.0.1:2181");
		props.put("serializer.class", storm.tpb.util.Properties.getString("kafka.serializer.class"));
		props.put("metadata.broker.list", storm.tpb.util.Properties.getString("kafka.broker.list") );
	    config = new ProducerConfig(props);
	    producer = new Producer<String, String>(config);
		this.topic = topic;
	}
	public static void main(String[] args) {
		long firstArg=0;
		if (args.length >= 1) {
		    try {
		        firstArg = Long.parseLong(args[0]);
		    } catch (NumberFormatException e) {
		        System.err.println("Argument" + args[0] + " must be an Long.");
		        System.exit(1);
		    }
		}
		long secondArg=1000;
		if (args.length >= 2){
			 try {
				  secondArg = Long.parseLong(args[1]);
			    } catch (NumberFormatException e) {
			        System.err.println("Argument" + args[1] + " must be an Long.");
			        System.exit(1);
			 }	
		}
		if(firstArg != 0)
		rangeTime = firstArg/secondArg;
		
		ProducerTest producertask = new ProducerTest(storm.tpb.util.Properties.getString("storm.kafka_topic"),1000);
		new Timer().scheduleAtFixedRate(producertask,0, secondArg);
	}
	
	public void run() {
		
		ObjectMapper mapper = new ObjectMapper();
		String mess="";
		try {
			mess = mapper.writeValueAsString(GetObject());
			producer.send(new KeyedMessage<String, String>(topic, mess));
			System.out.println("time up! "+ mess);
			++count;
			if(rangeTime != 0 && count==rangeTime){
				System.exit(0);
			}
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	}

    private static storm.tpb.testing.Transaction GetObject()
    {
        Random randomGenerator = new Random();
        final String trx_id = java.util.UUID.randomUUID().toString();
        //Generate Transaction Code
        final String[] trx_codes = new String[] {"Deposit", "Withdrawal", "Transfer From", "Transfer To", "Balance Inquiry"};
        final Random rand = new Random();
        final String trx_code = trx_codes[rand.nextInt(trx_codes.length)];
        //Generate Channel ID
        final String[] ch_ids = new String[] {"Branch 1", "Branch 2", "Branch 3", "Contact Center"};
        final String ch_id = ch_ids[rand.nextInt(ch_ids.length)];
        //Generate Account
        final String[] acc_nos = new String[] {"100-121-12121212", "200-555-12313123", "100-643-10231323", "400-223-32424234", "500-123-23313443"};
        final String acc_no = acc_nos[rand.nextInt(acc_nos.length)];
        //Generate Amount
        final Integer amount = randomGenerator.nextInt(200);
        storm.tpb.testing.Transaction tran = new storm.tpb.testing.Transaction(trx_id,trx_code,ch_id,amount,acc_no,"Savings",System.currentTimeMillis());
        return tran;
    }
	
}

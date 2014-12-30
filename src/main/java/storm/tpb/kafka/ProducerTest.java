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
		if (args.length > 0) {
		    try {
		        firstArg = Long.parseLong(args[0]);
		    } catch (NumberFormatException e) {
		        System.err.println("Argument" + args[0] + " must be an Long.");
		        System.exit(1);
		    }
		}
		if(firstArg != 0)
		rangeTime = firstArg/800;
		
		ProducerTest producertask = new ProducerTest(storm.tpb.util.Properties.getString("kafka.topic.name"),1000);
		new Timer().scheduleAtFixedRate(producertask,0, 800);
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
	
	private static Transaction GetObject()
	{
		   Random randomGenerator = new Random();
		   Integer trx_id;
		   trx_id = randomGenerator.nextInt(1000);
		   String trx_code;
		   int a = randomGenerator.nextInt(5);
		   if(a==1)trx_code="Deposit";
		   else if (a==2)trx_code="Withdrawal";
		   else if (a==3)trx_code="Transfer From";
		   else if (a==4)trx_code="Transfer To";
		   else if (a==5)trx_code="Balance Inquiry";
		   else trx_code="Deposit";
		   String ch_id;
		   a = randomGenerator.nextInt(4);
		   if(a==1)ch_id="Branch 1";
		   else if (a==2)ch_id="Branch 2";
		   else if (a==3)ch_id="Branch 3";
		   else if (a==4)ch_id="Contact Center";
		   else ch_id="Branch 1";
		    Integer amount = randomGenerator.nextInt(200);
		    String acc_no=java.util.UUID.randomUUID().toString();
		Transaction tran = new  Transaction(trx_id,trx_code,ch_id,amount,acc_no,"Savings");
		return tran;
	}
	
}

package storm.tpb.kafka;

import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import java.net.*;
import java.io.*;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class ProducerTest  {
	public static void main(String[] args) {
		Properties props = new Properties();
		//props.put("zk.connect", "127.0.0.1:2181");
		props.put("serializer.class", "kafka.serializer.StringEncoder");
		props.put("metadata.broker.list", "localhost:9092");
		props.put("producer.type", "sync");
		ProducerConfig config = new ProducerConfig(props);
		
		Producer<String, String> producer = new Producer<String, String>(config);
		int port = 8002;//the address of the remote server
		String ServerIP = "127.0.0.1";
		long startTime = 0, endTime = 0, latencyShot = 0, latencyLong = 0;
		try{
			System.out.println(InetAddress.getLocalHost().getHostAddress());

			Socket socket = new Socket(InetAddress.getLocalHost().getHostAddress(),port);//initiate a socket object

			OutputStream os = socket.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os);
			PrintWriter pw = new PrintWriter(osw);//create a PrintWriter object for output

			InputStream sIn = socket.getInputStream();
			InputStreamReader isr = new InputStreamReader(sIn);
			BufferedReader br = new BufferedReader(isr);//create a BufferReader object for input

			pw.print("This is written from Client to Server.");
			pw.flush();


			socket.close();

			}catch(IOException e){
			System.err.print(e);
			}
			
		
		for (int i = 0; i < 100000; i++){
			ObjectMapper mapper = new ObjectMapper();
			String mess="";
			try {
				mess = mapper.writeValueAsString(GetObject());
				producer.send(new KeyedMessage<String, String>("test", mess));
				 
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

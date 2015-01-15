package storm.tpb.topology;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;

import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import org.apache.log4j.BasicConfigurator;

import org.json.simple.JSONValue;
import storm.kafka.*;
import storm.kafka.trident.OpaqueTridentKafkaSpout;

import storm.kafka.trident.TridentKafkaConfig;

import backtype.storm.Config;
import backtype.storm.LocalCluster;

import backtype.storm.tuple.Fields;

import storm.tpb.bolts.FunctionRouter;
import storm.trident.operation.*;
import storm.tpb.util.Properties;
import storm.trident.Stream;
import storm.trident.TridentTopology;
import storm.trident.tuple.TridentTuple;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.logging.Logger;

public class TopologyMain {
	private final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(this.getClass());
	private static final String KAFKA_TOPIC =
			Properties.getString("storm.kafka_topic");
	public static final Jedis jedis=new Jedis(Properties.getString("redis.host"), Properties.getInt("redis.port"));;
	public static void main(String[] args) throws Exception {
		Config conf = new Config();
		conf.setMaxSpoutPending(500);
		if (args.length == 0) {
			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology("log-analysis", conf,
					createTopology());
		} else {
			conf.setNumWorkers(3);
			StormSubmitter.submitTopology(args[0], conf,
					createTopology());
		}
	}

	private static Config createConfig(boolean local)
	{
		int workers = Properties.getInt("storm.workers");
		Config conf = new Config();
		conf.put(Config.NIMBUS_HOST, "localhost");
		conf.setDebug(true);
		if (local)
			conf.setMaxTaskParallelism(workers);
		else
			conf.setNumWorkers(workers);
		return conf;
	}
	public static StormTopology createTopology() {
		/*TridentTopology topology = new TridentTopology();
		TridentKafkaConfig spoutConf = new TridentKafkaConfig(
				new ZkHosts(Properties.getString("storm.zkhosts")) ,
				KAFKA_TOPIC ,
				"storm"
		) ;
		spoutConf. scheme = new SchemeAsMultiScheme( new StringScheme( ) ) ;*/

		TridentTopology topology = new TridentTopology();
		BrokerHosts zk = new ZkHosts(Properties.getString("storm.zkhosts"));
		TridentKafkaConfig spoutConf = new TridentKafkaConfig(zk,KAFKA_TOPIC);
		spoutConf.scheme = new SchemeAsMultiScheme(new StringScheme());
		OpaqueTridentKafkaSpout spout = new OpaqueTridentKafkaSpout(spoutConf);


		Stream spoutStream = topology.newStream("kafka-stream",spout);
		Fields jsonFields = new Fields("trx_id", "trx_code","ch_id","amount","acc_no","prd_id");
		Stream parsedStream = spoutStream.each(new Fields("str"), new
				JsonProjectFunction(jsonFields), jsonFields);
		EWMA ewmasecond = new EWMA().sliding(9.0, EWMA.Time.SECONDS).
				withAlpha(EWMA.ONE_MINUTE_ALPHA);
		EWMA ewmaminutes = new EWMA().sliding(1.0, EWMA.Time.MINUTES);
		EWMA ewmahours = new EWMA().sliding(1.0, EWMA.Time.HOURS);
		//Total--------------------------------------------------------
		/*//Seconde
		Stream secondStream = parsedStream.each(new
				Fields(), new MovingCountFunction(ewmasecond, EWMA.Time.MINUTES), new
				Fields("count"));

		//Minutes
		Stream minutesStream = parsedStream.each(new
				Fields(), new MovingCountFunction(ewmaminutes, EWMA.Time.MINUTES), new
				Fields("count"));
		//Hours
		Stream hoursStream = parsedStream.each(new
				Fields(), new MovingCountFunction(ewmahours, EWMA.Time.HOURS), new
				Fields("count"));
*/
		//Branch1---------------------------------------------------------------------------------
		Stream streamBranch1  = parsedStream.each(new
				Fields("ch_id"), new FilterChannel(PARAM.Channel.BRANCH1));
		//Seconde

		Stream secondStreambr1 = streamBranch1.each(new
				Fields(), new MovingCountFunction(ewmasecond, EWMA.Time.MINUTES), new
				Fields("count"));
		//secondStreambr1.each(new Fields("ch_id","count"),new FilterToJedis());
		//Minutes

		/*Stream minutesStreambr1 = streamBranch1.each(new Fields("trx_id"),new MovingCountFunction(ewmaminutes, EWMA.Time.MINUTES), new
				Fields("count"));
		minutesStreambr1.each(new Fields("ch_id","count"),new FilterToJedis());*/
		//Hours

	/*	Stream hoursStreambr1 = streamBranch1.each(new
				Fields(), new MovingCountFunction(ewmahours, EWMA.Time.HOURS), new
				Fields("count"));
		hoursStreambr1.each(new Fields("ch_id","count"),new FilterToJedis());*/
		/*//Branch2//--------------------------------------------------------------------------------
		Stream streamBranch2  = parsedStream.each(new
				Fields("ch_id"), new FilterChannel(PARAM.Channel.BRANCH2));
		//Seconde

		Stream secondStreambr2 = streamBranch2.each(new
				Fields(), new MovingCountFunction(ewmasecond, EWMA.Time.MINUTES), new
				Fields("count"));
		secondStreambr2.each(new Fields("ch_id","count"),new FilterToJedis());
		//Minutes

		Stream minutesStreambr2 = streamBranch2.each(new
				Fields(), new MovingCountFunction(ewmaminutes, EWMA.Time.MINUTES), new
				Fields("count"));
		minutesStreambr2.each(new Fields("ch_id","count"),new FilterToJedis());
		//Hours

		Stream hoursStreambr2 = streamBranch2.each(new
				Fields(), new MovingCountFunction(ewmahours, EWMA.Time.HOURS), new
				Fields("count"));
		hoursStreambr2.each(new Fields("ch_id","count"),new FilterToJedis());
		//Branch3//--------------------------------------------------------------------------------
		Stream streamBranch3  = parsedStream.each(new
				Fields("ch_id"), new FilterChannel(PARAM.Channel.BRANCH3));
		//Seconde

		Stream secondStreambr3 = streamBranch3.each(new
				Fields(), new MovingCountFunction(ewmasecond, EWMA.Time.MINUTES), new
				Fields("count"));
		secondStreambr3.each(new Fields("ch_id","count"),new FilterToJedis());
		//Minutes

		Stream minutesStreambr3 = streamBranch3.each(new
				Fields(), new MovingCountFunction(ewmaminutes, EWMA.Time.MINUTES), new
				Fields("count"));
		minutesStreambr3.each(new Fields("ch_id","count"),new FilterToJedis());
		//Hours

		Stream hoursStreambr3 = streamBranch3.each(new
				Fields(), new MovingCountFunction(ewmahours, EWMA.Time.HOURS), new
				Fields("count"));
		hoursStreambr3.each(new Fields("ch_id","count"),new FilterToJedis());
		//Branch4//--------------------------------------------------------------------------------
		Stream streamBranch4  = parsedStream.each(new
				Fields("ch_id"), new FilterChannel(PARAM.Channel.BRANCH4));
		//Seconde

		Stream secondStreambr4 = streamBranch4.each(new
				Fields(), new MovingCountFunction(ewmasecond, EWMA.Time.MINUTES), new
				Fields("count"));
		secondStreambr4.each(new Fields("ch_id","count"),new FilterToJedis());
		//Minutes

		Stream minutesStreambr4 = streamBranch4.each(new
				Fields(), new MovingCountFunction(ewmaminutes, EWMA.Time.MINUTES), new
				Fields("count"));
		minutesStreambr4.each(new Fields("ch_id","count"),new FilterToJedis());
		//Hours

		Stream hoursStreambr4 = streamBranch4.each(new
				Fields(), new MovingCountFunction(ewmahours, EWMA.Time.HOURS), new
				Fields("count"));
		hoursStreambr4.each(new Fields("ch_id","count"),new FilterToJedis());*/


		//topology.newStream("spoutkafka1" , new TransactionalTridentKafkaSpout(spoutConf)).shuffle()
		//.each(new FunctionRouter(),new Fields("trx_id", "trx_code","ch_id","amount","acc_no","prd_id"));


		return topology.build();
	}

	public static class JsonProjectFunction extends BaseFunction {
		private Fields fields;
		public JsonProjectFunction(Fields fields) {
			this.fields = fields;
		}
		public void execute(TridentTuple tuple, TridentCollector
				collector) {
			String json = tuple.getString(0);
			Map<String, Object> map = (Map<String, Object>)
					JSONValue.parse(json);
			Values values = new Values();
			for (int i = 0; i < this.fields.size(); i++) {
				values.add(map.get(this.fields.get(i)));
			}
			System.out.println(values + "DI DOI NHA MA");

			collector.emit(values);
		}
	}
	public static class FilterChannel extends BaseFilter {
		private  PARAM.Channel channel;
		public FilterChannel(PARAM.Channel channel) {
			this.channel = channel;
		}
		public boolean isKeep(TridentTuple tuple) {
			System.out.println( tuple.get(0) + "VINH BIET VINH BIET");

			return tuple.get(0).equals( channel.getValue());
		}
	}

	public static class Print1 extends BaseFilter {

		public Print1() {

		}
		@Override
		public boolean isKeep(TridentTuple tuple) {
			System.out.println(tuple.get(0) + "VI THE TOI QUEN");
			return true;
		}
	}


	public static class Print extends BaseFilter {

		public Print() {
			System.out.println("DOI TOI VO CUNG BUON");
		}
		@Override
		public boolean isKeep(TridentTuple tuple) {
			System.out.println(tuple.get(0) + "TIEN NHAN");
			return true;
		}
	}


	public static class FilterToJedis extends BaseFilter {
		//	private static final Logger LOG = LoggerFactory.getLogger(BaseFunction.class);
		public FilterToJedis(){

		}
		@Override
		public boolean isKeep(TridentTuple tuple){
			TopologyMain.jedis.publish("real-time-" + tuple.getString(0), tuple.getLong(1).toString());
			return true;
			//TopologyMain.jedis.rpush("real-time-60s-" + transaction.getch_id(), transaction.getamount().toString());
			//TopologyMain.jedis.blpop(0, "real-time-60s-" + transaction.getch_id());
		}
	}

	public static class MovingCountFunction extends BaseFunction {
		//	private static final Logger LOG = LoggerFactory.getLogger(BaseFunction.class);
		private EWMA ewma;
		private EWMA.Time emitRatePer;
		public MovingCountFunction(EWMA ewma, EWMA.Time emitRatePer){
			this.ewma = ewma;
			this.emitRatePer = emitRatePer;
		}
		@Override
		public void execute(TridentTuple tuple, TridentCollector
				collector) {
			this.ewma.mark();
			//	LOG.debug("Rate: {}", this.ewma.getAverageRatePer(this.emitRatePer));
			//collector.emit(new Values(this.ewma.getAverageRatePer(this.emitRatePer)));
			System.out.println(this.ewma.getCount(this.emitRatePer) + "TIEN NHAN");
			collector.emit(new Values(this.ewma.getCount(this.emitRatePer)));
		}
	}

	public static class PublishToRedis extends BaseFunction {
		//	private static final Logger LOG = LoggerFactory.getLogger(BaseFunction.class);
		public PublishToRedis(){

		}
		@Override
		public void execute(TridentTuple tuple, TridentCollector
				collector) {
			TopologyMain.jedis.publish("real-time-" + tuple.getString(0), tuple.getLong(1).toString());

			//TopologyMain.jedis.rpush("real-time-60s-" + transaction.getch_id(), transaction.getamount().toString());
			//TopologyMain.jedis.blpop(0, "real-time-60s-" + transaction.getch_id());
		}
	}

	public static class MovingAverageFunction extends BaseFunction {
		//	private static final Logger LOG = LoggerFactory.getLogger(BaseFunction.class);
		private EWMA ewma;
		private EWMA.Time emitRatePer;
		public MovingAverageFunction(EWMA ewma, EWMA.Time emitRatePer){
			this.ewma = ewma;
			this.emitRatePer = emitRatePer;
		}
		@Override
		public void execute(TridentTuple tuple, TridentCollector
				collector) {
			this.ewma.mark();
			//	LOG.debug("Rate: {}", this.ewma.getAverageRatePer(this.emitRatePer));
			collector.emit(new Values(this.ewma.getAverageRatePer(this.emitRatePer)));
		}
	}
}
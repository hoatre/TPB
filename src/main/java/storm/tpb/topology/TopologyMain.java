package storm.tpb.topology;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;

import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.task.IMetricsContext;
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
import storm.trident.operation.builtin.Count;
import storm.trident.operation.builtin.Sum;
import storm.trident.state.State;
import storm.trident.state.StateFactory;
import storm.trident.tuple.TridentTuple;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class TopologyMain {
	private final org.apache.log4j.Logger LOGGER = org.apache.log4j.Logger.getLogger(this.getClass());
	private static final String KAFKA_TOPIC =
			Properties.getString("storm.kafka_topic");

	public static void main(String[] args) throws Exception {
		Config conf = new Config();
		conf.setMaxSpoutPending(500);
		//conf.put("task.heartbeat.frequency.secs",3);
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
		conf.put(Config.TOPOLOGY_TICK_TUPLE_FREQ_SECS, 1);
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
		spoutConf.startOffsetTime =-1;

		//spoutConf.metricsTimeBucketSizeInSecs=1000;
		OpaqueTridentKafkaSpout spout = new OpaqueTridentKafkaSpout(spoutConf);


		Stream spoutStream = topology.newStream("kafka-stream",spout);
		Fields jsonFields = new Fields("trx_id", "trx_code","ch_id","amount","acc_no","prd_id");
		Stream parsedStream = spoutStream.each(new Fields("str"), new
				JsonProjectFunction(jsonFields), jsonFields);
		EWMA ewmasecond = new EWMA().sliding(10.0, EWMA.Time.SECONDS);
		EWMA ewmaminutes = new EWMA().sliding(1.0, EWMA.Time.MINUTES);
		EWMA ewmahours = new EWMA().sliding(1.0, EWMA.Time.HOURS);

		//TransCode//------------------------------------------------------------------------------

		/*Stream groupStram = parsedStream.groupBy(new Fields("acc_no", "trx_code"))
										.aggPartition()*/


//Total--------------------------------------------------------
		//Seconde
		/*Stream secondStream = parsedStream.each(new
				Fields("amount"), new MovingCountFunction(ewmasecond, EWMA.Time.MINUTES), new
				Fields("count"));
*/
		//Minutes
		Stream minutesStream = parsedStream.each(new
				Fields("amount"), new MovingCountFunction(ewmaminutes, EWMA.Time.MINUTES), new
				Fields("count","sum"))
				.each(new Fields("ch_id", "count", "amount"), new FilterToJedis(PARAM.Channel.TOTAL));;
		/*//Hours
		Stream hoursStream = parsedStream.each(new
				Fields(), new MovingCountFunction(ewmahours, EWMA.Time.HOURS), new
				Fields("count","sum"));*/
		//Branch1---------------------------------------------------------------------------------
		Stream streamBranch1  = parsedStream.each(new
				Fields("ch_id"), new FilterChannel(PARAM.Channel.BRANCH1));
		//Seconde

		/*Stream secondStreambr1 = streamBranch1.each(new
				Fields("amount"), new MovingCountFunction(ewmasecond, EWMA.Time.SECONDS), new
				Fields("count","sum"));
		secondStreambr1.each(new Fields("ch_id","count","amount"),new FilterToJedis());*/
		//Minutes

		Stream minutesStreambr1 = streamBranch1.each(new Fields(),new MovingCountFunction(ewmaminutes, EWMA.Time.MINUTES), new
				Fields("count","sum"));
		minutesStreambr1.each(new Fields("ch_id","count","amount"),new FilterToJedis(PARAM.Channel.BRANCH1));
		//Hours

	/*	Stream hoursStreambr1 = streamBranch1.each(new
				Fields("amount"), new MovingCountFunction(ewmahours, EWMA.Time.HOURS), new
				Fields("count","sum"));
		hoursStreambr1.each(new Fields("ch_id","count"),new FilterToJedis());*/

		//Branch2//--------------------------------------------------------------------------------
		Stream streamBranch2  = parsedStream.each(new
				Fields("ch_id"), new FilterChannel(PARAM.Channel.BRANCH2));
		//Seconde

		/*Stream secondStreambr2 = streamBranch2.each(new
				Fields("amount"), new MovingCountFunction(ewmasecond, EWMA.Time.SECONDS), new
				Fields("count","sum"));
		secondStreambr2.each(new Fields("ch_id","count","amount"),new FilterToJedis());*/
		//Minutes

		Stream minutesStreambr2 = streamBranch2.each(new
				Fields("amount"), new MovingCountFunction(ewmaminutes, EWMA.Time.MINUTES), new
				Fields("count","sum"));
		minutesStreambr2.each(new Fields("ch_id","count","amount"),new FilterToJedis(PARAM.Channel.BRANCH2));
		/*//Hours

		Stream hoursStreambr2 = streamBranch2.each(new
				Fields("amount"), new MovingCountFunction(ewmahours, EWMA.Time.HOURS), new
				Fields("count","sum"));
		hoursStreambr2.each(new Fields("ch_id","count"),new FilterToJedis());*/
		//Branch3//--------------------------------------------------------------------------------
		Stream streamBranch3  = parsedStream.each(new
				Fields("ch_id"), new FilterChannel(PARAM.Channel.BRANCH3));
		//Seconde

		/*Stream secondStreambr3 = streamBranch3.each(new
				Fields("amount"), new MovingCountFunction(ewmasecond, EWMA.Time.SECONDS), new
				Fields("count","sum"));
		secondStreambr3.each(new Fields("ch_id","count"),new FilterToJedis());*/
		//Minutes

		Stream minutesStreambr3 = streamBranch3.each(new
				Fields("amount"), new MovingCountFunction(ewmaminutes, EWMA.Time.MINUTES), new
				Fields("count","sum"));
		minutesStreambr3.each(new Fields("ch_id","count","amount"),new FilterToJedis(PARAM.Channel.BRANCH3));
		/*//Hours

		Stream hoursStreambr3 = streamBranch3.each(new
				Fields("amount"), new MovingCountFunction(ewmahours, EWMA.Time.HOURS), new
				Fields("count","sum"));
		hoursStreambr3.each(new Fields("ch_id","count"),new FilterToJedis());*/
		//Branch4//--------------------------------------------------------------------------------
		Stream streamBranch4  = parsedStream.each(new
				Fields("ch_id"), new FilterChannel(PARAM.Channel.BRANCH4));
		//Seconde
/*
		Stream secondStreambr4 = streamBranch4.each(new
				Fields("amount"), new MovingCountFunction(ewmasecond, EWMA.Time.SECONDS), new
				Fields("count","sum"));
		secondStreambr4.each(new Fields("ch_id","count","amount"),new FilterToJedis());*/
		//Minutes

		Stream minutesStreambr4 = streamBranch4.each(new
				Fields("amount"), new MovingCountFunction(ewmaminutes, EWMA.Time.MINUTES), new
				Fields("count","sum"));
		minutesStreambr4.each(new Fields("ch_id","count","amount"),new FilterToJedis(PARAM.Channel.BRANCH4));
		//Hours

		/*Stream hoursStreambr4 = streamBranch4.each(new
				Fields("amount"), new MovingCountFunction(ewmahours, EWMA.Time.HOURS), new
				Fields("count","sum"));
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
	public  static  class  StateSumFactory implements StateFactory{
		@Override
		public State makeState(Map map, IMetricsContext iMetricsContext, int i, int i1) {
			return null;
		}
	}
	public  static  class StateTrident implements  State{

		@Override
		public void beginCommit(Long aLong) {

		}

		@Override
		public void commit(Long aLong) {

		}

	}
	public static class FilterTransCode extends BaseFilter {
		private  PARAM.TransCode transCode;
		public FilterTransCode(PARAM.TransCode transCode) {
			this.transCode = transCode;
		}
		public boolean isKeep(TridentTuple tuple) {
			return tuple.get(0).equals( transCode.getValue());
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
			System.out.println(tuple.get(0) + " Thu lan 1");
//			System.out.println(tuple.get(1) + "Thu Lan 2");
//			System.out.println(tuple.get(2) + " Thu lan 3");
			return true;
		}
	}


	public static class FilterToJedis extends BaseFilter {
		//	private static final Logger LOG = LoggerFactory.getLogger(BaseFunction.class);
		private  PARAM.Channel channel;
		public FilterToJedis(PARAM.Channel channel){
			this.channel = channel;
		}
		@Override
		public boolean isKeep(TridentTuple tuple){
			Jedis jedis;
			jedis=new Jedis(Properties.getString("redis.host"), Properties.getInt("redis.port"));
			jedis.set("real-time-" + channel.getValue(), tuple.getLong(1).toString());
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
			this.ewma.mark(tuple.getInteger(0));
			//	LOG.debug("Rate: {}", this.ewma.getAverageRatePer(this.emitRatePer));
			//collector.emit(new Values(this.ewma.getAverageRatePer(this.emitRatePer)));
			System.out.println(this.ewma.getCount() + "TIEN NHAN");
			collector.emit(new Values(this.ewma.getCount(),this.ewma.getSum()));
		}


	}


}
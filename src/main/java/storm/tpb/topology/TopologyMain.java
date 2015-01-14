package storm.tpb.topology;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.StormTopology;
import backtype.storm.spout.MultiScheme;
import backtype.storm.spout.SchemeAsMultiScheme;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import org.apache.log4j.BasicConfigurator;
import org.slf4j.LoggerFactory;
import storm.kafka.SpoutConfig;
import storm.kafka.StringScheme;
import storm.kafka.ZkHosts;
import storm.kafka.trident.OpaqueTridentKafkaSpout;
import storm.kafka.trident.TransactionalTridentKafkaSpout;
import storm.kafka.trident.TridentKafkaConfig;
import storm.tpb.spouts.WordReader;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import storm.tpb.bolts.WordCounter;
import storm.tpb.bolts.FunctionRouter;
import storm.trident.operation.*;
import storm.tpb.util.Properties;
import storm.trident.Stream;
import storm.trident.TridentTopology;
import storm.trident.tuple.TridentTuple;
import redis.clients.jedis.Jedis;
import java.util.logging.Logger;

public class TopologyMain {
	private static final String KAFKA_TOPIC =
			Properties.getString("storm.kafka_topic");
	public static final Jedis jedis=new Jedis(Properties.getString("redis.host"), Properties.getInt("redis.port"));;
	public static void main(String[] args) throws Exception {
		BasicConfigurator.configure();
		if (args != null && args.length > 0)
		{
			StormSubmitter.submitTopology(
					args[0],
					createConfig(false),
					createTopology());
		}
		else
		{
			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology(
					"Transaction-Topology",
					createConfig(true),
					createTopology());
			Thread.sleep(60000);
			cluster.shutdown();
		}
		Utils.sleep(10000);

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
		TridentTopology topology = new TridentTopology();
		TridentKafkaConfig spoutConf = new TridentKafkaConfig(
				new ZkHosts(Properties.getString("storm.zkhosts")) ,
				KAFKA_TOPIC ,
				"storm"
		) ;
		spoutConf. scheme = new SchemeAsMultiScheme( new StringScheme( ) ) ;

		OpaqueTridentKafkaSpout spout = new OpaqueTridentKafkaSpout(spoutConf);
		Stream spoutStream = topology.newStream("kafka-stream",spout);
		Fields jsonFields = new Fields("trx_id", "trx_code","ch_id","amount","acc_no","prd_id");
		Stream parsedStream = spoutStream.each(new Fields(), new
				FunctionRouter(), jsonFields);
		EWMA ewmasecond = new EWMA().sliding(1.0, EWMA.Time.SECONDS);
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
		secondStreambr1.each(new Fields("ch_id","count"),new FilterToJedis());
		//Minutes

		Stream minutesStreambr1 = streamBranch1.each(new
				Fields(), new MovingCountFunction(ewmaminutes, EWMA.Time.MINUTES), new
				Fields("count"));
		minutesStreambr1.each(new Fields("ch_id","count"),new FilterToJedis());
		//Hours

		Stream hoursStreambr1 = streamBranch1.each(new
				Fields(), new MovingCountFunction(ewmahours, EWMA.Time.HOURS), new
				Fields("count"));
		hoursStreambr1.each(new Fields("ch_id","count"),new FilterToJedis());
		//Branch2//--------------------------------------------------------------------------------
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
		hoursStreambr4.each(new Fields("ch_id","count"),new FilterToJedis());


		//topology.newStream("spoutkafka1" , new TransactionalTridentKafkaSpout(spoutConf)).shuffle()
		//.each(new FunctionRouter(),new Fields("trx_id", "trx_code","ch_id","amount","acc_no","prd_id"));


		return topology.build();
	}
	public static class FilterChannel extends BaseFilter {
		private PARAM.Channel channel;
		public FilterChannel(PARAM.Channel channel) {
		}
		public boolean isKeep(TridentTuple tuple) {
			return tuple.get(0) == channel;
		}
	}

	public static class Print extends BaseFilter {

		public Print() {
		}

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
			this.ewma.mark(tuple.getLong(0));
			//	LOG.debug("Rate: {}", this.ewma.getAverageRatePer(this.emitRatePer));
			//collector.emit(new Values(this.ewma.getAverageRatePer(this.emitRatePer)));
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
			this.ewma.mark(tuple.getLong(0));
		//	LOG.debug("Rate: {}", this.ewma.getAverageRatePer(this.emitRatePer));
			collector.emit(new Values(this.ewma.getAverageRatePer(this.emitRatePer)));
		}
	}
}
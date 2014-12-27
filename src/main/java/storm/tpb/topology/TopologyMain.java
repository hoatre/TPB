package storm.tpb.topology;
import storm.tpb.spouts.WordReader;
import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.tuple.Fields;
import storm.tpb.bolts.WordCounter;
import storm.tpb.bolts.WordNormalizer;

public class TopologyMain {
	public static void main(String[] args) throws InterruptedException {
		// Topology definition
		TopologyBuilder builder = new TopologyBuilder();
		builder.setSpout("word-reader", new WordReader());
		builder.setBolt("word-normalizer", new WordNormalizer())
				.shuffleGrouping("word-reader");
		builder.setBolt("word-counter", new WordCounter(), 2).fieldsGrouping(
				"word-normalizer", new Fields("word"));
		// Configuration
		Config conf = new Config();
		conf.put("wordsFile", "src/main/resources/words.txt");
		conf.setDebug(false);
		// Topology run
		conf.put(Config.TOPOLOGY_MAX_SPOUT_PENDING, 1);
		LocalCluster cluster = new LocalCluster();
		cluster.submitTopology("StormDemo-Toplogie", conf,
				builder.createTopology());
		Thread.sleep(1000);
		cluster.shutdown();
	}
}
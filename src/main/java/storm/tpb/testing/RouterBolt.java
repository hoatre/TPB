package storm.tpb.testing;

import backtype.storm.Config;
import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by HieuLD on 12/25/14.
 */
public class RouterBolt extends BaseRichBolt {
    private static final long serialVersionUID = 42L;
    private static final Logger LOGGER =
            Logger.getLogger(RouterBolt.class);
    private OutputCollector collector;

    public void prepare(Map stormConf, TopologyContext context,
                        OutputCollector collector) {
        this.collector=collector;
    }

    public void cleanup() {}

    public void declareOutputFields(OutputFieldsDeclarer declarer)
    {
        declarer.declare(new Fields("trx_id", "trx_code", "ch_id", "amount", "acc_no", "prd_id", "timestamp"));
    }

    public void execute(Tuple input)
    {
        LOGGER.debug("Filtering incoming Transaction");
        Transaction _transaction = Utils.GetTransactionFromJSon(input);
        collector.emit(new Values(_transaction.gettrx_id(), _transaction.gettrx_code(), _transaction.getch_id(), _transaction.getamount(), _transaction.getacc_no(), _transaction.getprd_id(), _transaction.gettimetamp()));

    }

    public Map<String, Object> getComponentConfiguration() {
        return null;

    }
}

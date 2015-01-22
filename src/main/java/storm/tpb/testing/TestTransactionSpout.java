package storm.tpb.testing;

import backtype.storm.Config;
import backtype.storm.topology.OutputFieldsDeclarer;

import java.io.IOException;
import java.util.Map;
import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;
import backtype.storm.utils.Utils;
import java.util.HashMap;
import java.util.Random;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by HieuLD on 12/25/14.
 */


public class TestTransactionSpout extends BaseRichSpout {
    public static Logger LOG = LoggerFactory.getLogger(TestTransactionSpout.class);
    boolean _isDistributed;
    SpoutOutputCollector _collector;

    public TestTransactionSpout() {
        this(true);
    }

    public TestTransactionSpout(boolean isDistributed) {
        _isDistributed = isDistributed;
    }

    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        _collector = collector;
    }

    public void close() {

    }

    public void nextTuple() {
        Utils.sleep(1000);
        ObjectMapper mapper = new ObjectMapper();
        String mess;

        try {
            mess = mapper.writeValueAsString(GetObject());
            _collector.emit(new Values(mess));

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
        Transaction tran = new  Transaction(trx_id,trx_code,ch_id,amount,acc_no,"Savings",System.currentTimeMillis());
        return tran;
    }

    public void ack(Object msgId) {

    }

    public void fail(Object msgId) {

    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("obj_transaction"));
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        if(!_isDistributed) {
            Map<String, Object> ret = new HashMap<String, Object>();
            ret.put(Config.TOPOLOGY_MAX_TASK_PARALLELISM, 1);
            return ret;
        } else {
            return null;
        }
    }
}
package storm.tpb.bolts;
import org.apache.log4j.Logger;
import java.io.IOException;
import java.util.Map;


import org.codehaus.jackson.map.ObjectMapper;
import storm.tpb.kafka.Transaction;
import storm.trident.operation.BaseFunction;
import storm.trident.operation.TridentCollector;
import storm.trident.tuple.TridentTuple;
import backtype.storm.tuple.Values;
/**
 * Created by Administrator on 1/8/2015.
 */
public class FunctionRouter extends BaseFunction {
    private static final Logger LOGGER =
            Logger.getLogger(RouterBolt.class);
    private static final ObjectMapper mapper = new ObjectMapper();
    public void execute(TridentTuple tuple, TridentCollector collector) {
        String json = tuple.getString(0);
        try
        {
            Transaction trans = mapper.readValue(json, Transaction.class);
            Integer trx_id;
            String trx_code;
            String ch_id;
            Integer amount;
            String acc_no;
            String prd_id;

            trx_id = trans.gettrx_id();
            trx_code = trans.gettrx_code();
            ch_id = trans.getch_id();
            amount = trans.getamount();
            acc_no = trans.getacc_no();
            prd_id = trans.getprd_id();
            collector.emit(new Values(trx_id,trx_code,ch_id,amount,acc_no,prd_id));
        }
        catch (IOException ex)
        {
            LOGGER.error("IO error while filtering tweets", ex);
            LOGGER.trace(null, ex);
        }
    }
}

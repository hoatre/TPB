package storm.tpb.testing;

import backtype.storm.tuple.Tuple;
import backtype.storm.tuple.Values;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by HieuLD on 12/25/14.
 */
public class Utils {
    private static final ObjectMapper mapper = new ObjectMapper();
    public static Transaction GetTransactionFromJSon(Tuple input)
    {
        String json = input.getString(0);
        JsonNode root;
        Transaction _transaction = new Transaction();
        DateFormat dateTime = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        try
        {
            root = mapper.readValue(json, JsonNode.class);
            if (root.get("trx_id") != null && root.get("trx_code") != null)
            {
                _transaction.settrx_id(root.get("trx_id").getTextValue());
                _transaction.settrx_code(root.get("trx_code").getTextValue());
                _transaction.setch_id(root.get("ch_id").getTextValue());
                _transaction.setamount(root.get("amount").getIntValue());
                _transaction.setacc_no(root.get("acc_no").getTextValue());
                _transaction.setprd_id(root.get("prd_id").getTextValue());
                _transaction.settimetamp(root.get("timestamp").getLongValue());

            }
        }
        catch (IOException ex)
        {

        }
        return _transaction;
    }
}

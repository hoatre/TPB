package storm.tpb.testing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by HieuLD on 1/15/15.
 */

public class AccountAmountData {

    String strAccountNumber;
    long strAccountAmount;

    public AccountAmountData() {
    }

    public AccountAmountData(String strAccountNumber, long strAccountAmount) {
        this.strAccountNumber = strAccountNumber;
        this.strAccountAmount = strAccountAmount;
    }



    public String getAccountNumber() {
        return strAccountNumber;
    }

    public void setAccountNumber(String strAccountNumber) {
        this.strAccountNumber = strAccountNumber;
    }

    public long getAccountAmount() {
        return strAccountAmount;
    }

    public void setAccountAmount(long strAccountAmount) {
        this.strAccountAmount = strAccountAmount;
    }


    @Override
    public String toString()
    {
        return ("Account: "+this.getAccountNumber()+" Amount : "+this.getAccountAmount());
    }

    public static List<AccountAmountData> SumAmountByAccount
            (List<AccountAmountData> list) {

        Map<String, AccountAmountData> map = new HashMap<String, AccountAmountData>();

        for (AccountAmountData p : list) {
            String name = p.getAccountNumber();
            AccountAmountData sum = map.get(name);
            if (sum == null) {
                sum = new AccountAmountData(name, 0);
                map.put(name, sum);
            }
            sum.setAccountAmount(sum.getAccountAmount() + p.getAccountAmount());
        }
        return new ArrayList<AccountAmountData>(map.values());
    }
}
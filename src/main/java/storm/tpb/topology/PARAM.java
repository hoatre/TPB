package storm.tpb.topology;

import java.io.Serializable;

/**
 * Created by Administrator on 1/14/2015.
 */
public class PARAM implements Serializable  {
    public static enum Channel {
        BRANCH1("Branch 1"), BRANCH2("Branch 2"), BRANCH3("Branch 3"), BRANCH4("Contact Center"), TOTAL("Total");
        private String value;
        private Channel(String value) {
            this.value = value;
        }
        public String getValue() {
            return value;
        }
    }

    public static enum TransCode {
        DEPOSIT("Deposit"), WITHDRAWAL("Withdrawal"), TRANSFERFROM("Transfer From"), TRANSFERTO("Transfer To"),BALANCE("Balance Inquiryo");
        private String value;
        private TransCode(String value) {
            this.value = value;
        }
        public String getValue() {
            return value;
        }
    }

    public static enum Time {
        MILLISECONDS(1), SECONDS(1000), MINUTES(SECONDS.getTime() *
                60), HOURS(MINUTES.getTime() * 60), DAYS(HOURS
                .getTime() * 24), WEEKS(DAYS.getTime() * 7);
        private long millis;
        private Time(long millis) {
            this.millis = millis;
        }
        public long getTime() {
            return this.millis;
        }
    }
}

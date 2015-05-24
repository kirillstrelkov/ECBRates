package ee.kirill.ecbrates;

import java.util.HashMap;

public class Currency extends HashMap<String, String> {
    public static final String CURRENCY = "currency";
    public static final String RATE = "rate";

    public Currency(String currency, String rate) {
        put(CURRENCY, currency);
        put(RATE, rate);
    }

    public String getCurrency() {
        return get(CURRENCY);
    }

    public String getRate() {
        return get(RATE);
    }
}

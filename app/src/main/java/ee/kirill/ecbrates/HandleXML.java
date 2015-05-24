package ee.kirill.ecbrates;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class HandleXML {
    private static final String TAG = "HandleXML";
    public volatile boolean parsingComplete = true;
    private String urlString = null;
    private SharedPreferences preferences = null;
    private Context context = null;
    private HandlingFile handlingFile = null;
    private String xmlDateAsString = null;
    private XmlPullParserFactory xmlFactoryObject;

    private ArrayList<Currency> currencies = new ArrayList<Currency>();

    public HandleXML(Context context) {
        this.context = context;
        this.handlingFile = new HandlingFile(context);
    }

    public HandleXML(Context context, String url, SharedPreferences preferences) {
        this.context = context;
        this.handlingFile = new HandlingFile(context);
        this.urlString = url;
        this.preferences = preferences;
    }

    private String getXmlDateAsString() {
        return xmlDateAsString;
    }

    private void populateCurrencyList(XmlPullParser ecbParser) {
        currencies.clear();
        try {
            int eventType = ecbParser.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && ecbParser.getName().equals("Cube")) {
                    int attributeCount = ecbParser.getAttributeCount();
                    if (attributeCount == 2) {
                        String currency = ecbParser.getAttributeValue(0);
                        String rate = ecbParser.getAttributeValue(1);
                        currencies.add(new Currency(currency, rate));
                    } else if (attributeCount == 1) {
                        xmlDateAsString = ecbParser.getAttributeValue(0);
                    }
                }
                eventType = ecbParser.next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void fetchXML() {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection)
                            url.openConnection();
                    conn.setReadTimeout(10000 /* milliseconds */);
                    conn.setConnectTimeout(15000 /* milliseconds */);
                    conn.setRequestMethod("GET");
                    conn.setDoInput(true);
                    conn.connect();
                    InputStream stream = conn.getInputStream();

                    xmlFactoryObject = XmlPullParserFactory.newInstance();
                    XmlPullParser parser = xmlFactoryObject.newPullParser();

                    parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
                    parser.setInput(stream, null);
                    populateCurrencyList(parser);
                    stream.close();

                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(ECBRatesActivity.PREF_LAST_UPDATE_DATE, getXmlDateAsString());
                    editor.commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                parsingComplete = false;
            }
        });

        thread.start();


    }

    public List<Currency> getCurrenciesFromInternet() {
        fetchXML();
        while (parsingComplete) {
            ;
        }
        return currencies;
    }

    public List<Currency> getCurrenciesFromLocalXML() {
        try {
            XmlPullParser parser = context.getResources().getXml(R.xml.ecb_rates);
            populateCurrencyList(parser);
        } catch (Throwable t) {
            Log.v(TAG, "Error XML-file loading: " + t.toString());
        }

        return currencies;
    }

    public void saveXML(List<Currency> currencies, String filename) {
        String xmlContent = getXMLContentFromList(currencies);
        handlingFile.writeToFile(xmlContent, filename);
    }


    private String getXMLContentFromList(List<Currency> currencies) {
        StringBuilder builder = new StringBuilder();
        String updateDate = getXmlDateAsString();

        builder.append("<Cube>\n");
        builder.append("<Cube time=\"" + updateDate + "\">\n");
        for (Currency currency : currencies) {
            builder.append("<Cube currency=\"" + currency.getCurrency() + "\" rate=\"" + currency.getRate() + "\"/>\n");
        }
        builder.append("</Cube>\n");
        builder.append("</Cube>\n");

        return builder.toString();
    }
}

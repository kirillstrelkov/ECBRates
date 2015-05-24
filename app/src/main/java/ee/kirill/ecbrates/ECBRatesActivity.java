package ee.kirill.ecbrates;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;


public class ECBRatesActivity extends ActionBarActivity {
    public static final String PREF_LAST_UPDATE_DATE = "lastUpdateDate";

    private static final String TAG = "ECBRatesActivity";
    private static final String XML_FILENAME = "ecb_rates.xml";

    public static SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ecbrates);

        SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        String lastUpdateDateAsString = preferences.getString(PREF_LAST_UPDATE_DATE, null);

        HandleXML handleXML = new HandleXML(this, "http://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml", preferences);

        List<Currency> currencies;
        if (isUpToDateXML(lastUpdateDateAsString)) {
            Log.v(TAG, "Using local resource");
            currencies = handleXML.getCurrenciesFromLocalXML();
            Log.v(TAG, "Currencies received");
        } else {
            Log.v(TAG, "Using internet resource");
            currencies = handleXML.getCurrenciesFromInternet();
            Log.v(TAG, "Currencies received");
            handleXML.saveXML(currencies, XML_FILENAME);
        }

        String[] from = new String[]{Currency.CURRENCY, Currency.RATE};
        int[] to = {R.id.textViewCurrency, R.id.textViewRate};

        ListView listView = (ListView) findViewById(R.id.listView);
        listView.setAdapter(new SimpleAdapter(this, currencies, R.layout.currency_list_item, from, to));
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_ecbrates, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_start_cross_currency_calc) {
            Intent intent = new Intent(this, CrossCurrencyCalcActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isUpToDateXML(String lastUpdateDateAsString) {
        Calendar currentCalendar = Calendar.getInstance();

        String lastFridayDateAsString = "";
        int currentDayOfWeek = currentCalendar.get(Calendar.DAY_OF_WEEK);
        boolean isSaturday = currentDayOfWeek == Calendar.SATURDAY;
        boolean isSunday = currentDayOfWeek == Calendar.SUNDAY;
        boolean isWeekend = isSaturday || isSunday;
        if (isWeekend) {
            Calendar lastFridayDate = Calendar.getInstance();
            lastFridayDate.setTime(currentCalendar.getTime());
            if (isSaturday) {
                lastFridayDate.add(Calendar.DAY_OF_YEAR, -1);
            } else {
                lastFridayDate.add(Calendar.DAY_OF_YEAR, -2);
            }
            lastFridayDateAsString = DATE_FORMAT.format(lastFridayDate.getTime());
        }


        String currentDateAsString = DATE_FORMAT.format(currentCalendar.getTime());

        Log.v(TAG, "Current date: " + currentDateAsString);
        Log.v(TAG, "Last update date: " + lastUpdateDateAsString);
        Log.v(TAG, "Last friday date: " + lastFridayDateAsString);

        boolean isUpToDateXML = lastUpdateDateAsString != null &&
                ((!isWeekend && currentDateAsString.equals(lastUpdateDateAsString)) ||
                        (isWeekend && lastFridayDateAsString.equals(lastUpdateDateAsString)));
        Log.v(TAG, "Is up-to-date XML: " + isUpToDateXML);
        return isUpToDateXML;
    }
}

package ee.kirill.ecbrates;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CrossCurrencyCalcActivity extends ActionBarActivity {
    private static final String TAG = "CrossCurrencyCalc";

    private Spinner spinner1;
    private Spinner spinner2;
    private Map<String, String> currenciesAndRates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cross_currency_calc);

        HandleXML handleXML = new HandleXML(this);
        List<Currency> currencies = handleXML.getCurrenciesFromLocalXML();
        currenciesAndRates = currencyListToHashMap(currencies);

        List<String> currenciesAsString = new ArrayList<>();
        currenciesAsString.addAll(currenciesAndRates.keySet());
        Collections.sort(currenciesAsString);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, currenciesAsString);

        AdapterView.OnItemSelectedListener listener = new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                recalculate();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                recalculate();
            }
        };

        spinner1 = (Spinner) findViewById(R.id.spinner_currency1);
        spinner1.setAdapter(adapter);
        spinner1.setOnItemSelectedListener(listener);

        spinner2 = (Spinner) findViewById(R.id.spinner_currency2);
        spinner2.setAdapter(adapter);
        spinner2.setOnItemSelectedListener(listener);

        recalculate();
    }

    private void recalculate() {
        String currency1 = spinner1.getSelectedItem().toString();
        double rate1 = Double.parseDouble(currenciesAndRates.get(currency1));
        Log.v(TAG, "1 Currency: " + currency1 + "Rate:" + rate1);

        String currency2 = spinner2.getSelectedItem().toString();
        double rate2 = Double.parseDouble(currenciesAndRates.get(currency2));
        Log.v(TAG, "2 Currency: " + currency2 + "Rate:" + rate2);

        TextView textViewRate = (TextView) findViewById(R.id.textRate);
        textViewRate.setText(String.format("Cross currency rate(%s/%s): %f", currency1, currency2, rate2 / rate1));
    }

    private Map<String, String> currencyListToHashMap(List<Currency> currencies) {
        HashMap<String, String> map = new HashMap<>();
        for (Currency currency : currencies) {
            map.put(currency.getCurrency(), currency.getRate());
        }
        return map;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cross_currency_calc, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_start_currency_list) {
            Intent intent = new Intent(this, ECBRatesActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

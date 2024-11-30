package com.example.cconverter;

import android.os.AsyncTask;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiConnector {
    private final String selectedItem1;
    private final String selectedItem2;
    private final String rateAmount;
    private final String baseUrl = "https://v6.exchangerate-api.com/v6/835357fdc6e44b914ef2e043/pair/";

    public ApiConnector(String selectedItem1, String selectedItem2, String rateAmount) {
        this.selectedItem1 = selectedItem1;
        this.selectedItem2 = selectedItem2;
        this.rateAmount = rateAmount;
    }

    public interface ApiCallback {
        void onSuccess(String conversionRate, String TimeLastUpdate, String TimeNextUpdate);
        void onError(Exception e);
    }

    public void fetchConversionRate(ApiCallback callback) {
        new AsyncTask<Void, Void, String[]>() {
            private Exception exception;

            @Override
            protected String[] doInBackground(Void... voids) {
                try {
                    String apiUrl = baseUrl + selectedItem1 + "/" + selectedItem2 + "/" + rateAmount;
                    URL url = new URL(apiUrl);
                    HttpURLConnection request = (HttpURLConnection) url.openConnection();
                    request.connect();

                    JsonParser jp = new JsonParser();
                    JsonElement root = jp.parse(new InputStreamReader((InputStream) request.getContent()));
                    JsonObject jsonobj = root.getAsJsonObject();

                    String conversionRate = jsonobj.get("conversion_rate").getAsString();
                    String lastUpdateTime = jsonobj.get("time_last_update_utc").getAsString();
                    String nextUpdateTime = jsonobj.get("time_next_update_utc").getAsString();
                    return new String[]{conversionRate, lastUpdateTime, nextUpdateTime};
                } catch (Exception e) {
                    exception = e;
                    return null;
                }
            }

            @Override
            protected void onPostExecute(String[] result) {
                if (exception == null && result != null) {
                    callback.onSuccess(result[0], result[1], result[2]);
                } else {
                    callback.onError(exception);
                }
            }
        }.execute();
    }
}

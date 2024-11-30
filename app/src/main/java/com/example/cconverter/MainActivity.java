package com.example.cconverter;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {
    private TextView titleText, ierText, ierUnit, switchLangText, amount1Text, amount2Text;
    private EditText amount1Unit;
    private TextView amount2Unit, lutText, lutUnit, nutText, nutUnit;
    private String selectedItem1, selectedItem2;
    private double conversionRate;
    private FileStreams fileStreams;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
    }

    private void initializeViews() {
        titleText = findViewById(R.id.titleText);
        ierText = findViewById(R.id.ierText);
        ierUnit = findViewById(R.id.ierUnit);
        switchLangText = findViewById(R.id.switchLangText);
        amount1Text = findViewById(R.id.amount1Text);
        amount2Text = findViewById(R.id.amount2Text);
        lutText = findViewById(R.id.lutText);
        lutUnit = findViewById(R.id.lutUnit);
        nutText = findViewById(R.id.nutText);
        nutUnit = findViewById(R.id.nutUnit);
        amount1Unit = findViewById(R.id.amount1Unit);
        amount2Unit = findViewById(R.id.amount2Unit);

        ImageButton uaSelect = findViewById(R.id.uaSelect);
        ImageButton ruSelect = findViewById(R.id.ruSelect);
        ImageButton enSelect = findViewById(R.id.enSelect);
        ImageButton plSelect = findViewById(R.id.plSelect);
        ImageButton nlSelect = findViewById(R.id.nlSelect);
        ImageButton convertButton = findViewById(R.id.convertButton);
        ImageButton settingsOpen = findViewById(R.id.settingsOpen);

        Spinner spinner1 = findViewById(R.id.spinner1);
        Spinner spinner2 = findViewById(R.id.spinner2);

        Context context = getApplicationContext();
        fileStreams = new FileStreams(context);

        String selectedLang = fileStreams.getSelectLang();
        switch (selectedLang) {
            case "ukrainian":
                selectUkrainian();
                break;
            case "russian":
                selectRussian();
                break;
            case "english":
                selectEnglish();
                break;
            case "polish":
                selectPolish();
                break;
            case "dutch":
                selectDutch();
                break;
        }

        ierUnit.setOnClickListener(v -> copyToClipboard(ierUnit));
        lutUnit.setOnClickListener(v -> copyToClipboard(lutUnit));
        amount2Unit.setOnClickListener(v -> copyToClipboard(amount2Unit));

        String[] items = getResources().getStringArray(R.array.currencies_list);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_spinner_item, items);
        adapter.setDropDownViewResource(R.layout.custom_spinner_dropdown_item);
        spinner1.setAdapter(adapter);
        spinner2.setAdapter(adapter);
        spinner2.setSelection(2);

        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String fullItem = parent.getItemAtPosition(position).toString();
                selectedItem1 = extractCurrencyCode(fullItem);
                updateConversionRate();
            }
            public void onNothingSelected(AdapterView<?> parent) {}
        });
        spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String fullItem = parent.getItemAtPosition(position).toString();
                selectedItem2 = extractCurrencyCode(fullItem);
                updateConversionRate();
            }
            public void onNothingSelected(AdapterView<?> parent) {}});

        convertButton.setOnClickListener(v -> {
            if (conversionRate != 0) {
                try {
                    double amount = Double.parseDouble(amount1Unit.getText().toString());
                    double result = amount * conversionRate;
                    double roundedResult = Math.round(result * 100.0) / 100.0;
                    amount2Unit.setText(String.valueOf(roundedResult));
                } catch (NumberFormatException e) {
                    Toast.makeText(getBaseContext(), "Invalid Amount", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getBaseContext(), "Conversion rate not available", Toast.LENGTH_SHORT).show();
            }
        });

        ImageButton settingsButton = findViewById(R.id.settingsOpen);
        settingsButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });


        ierUnit.setOnClickListener(v -> copyToClipboard(ierUnit));
        lutUnit.setOnClickListener(v -> copyToClipboard(lutUnit));
        nutUnit.setOnClickListener(v -> copyToClipboard(nutUnit));
        amount2Unit.setOnClickListener(v -> copyToClipboard(amount2Unit));

        uaSelect.setOnClickListener(v -> selectUkrainian());
        ruSelect.setOnClickListener(v -> selectRussian());
        enSelect.setOnClickListener(v -> selectEnglish());
        plSelect.setOnClickListener(v -> selectPolish());
        nlSelect.setOnClickListener(v -> selectDutch());
    }

    private void updateConversionRate() {
        if (selectedItem1 != null && selectedItem2 != null) {
            ApiConnector apiConnector = new ApiConnector(selectedItem1, selectedItem2, "1");
            apiConnector.fetchConversionRate(new ApiConnector.ApiCallback() {
                @SuppressLint("SetTextI18n")
                @Override
                public void onSuccess(String conversionRateStr, String LastUpdateTime, String NextUpdateTime) {
                    conversionRate = Double.parseDouble(conversionRateStr);
                    ierUnit.setText("1 " + selectedItem1 + " = " + conversionRateStr + " " + selectedItem2);
//                    lutUnit.setText(LastUpdateTime.replace(" +0000", ""));
//                    nutUnit.setText(NextUpdateTime.replace(" +0000", ""));
                }

                @SuppressLint("SetTextI18n")
                @Override
                public void onError(Exception e) {
                    ierUnit.setText("Error fetching rate");
                }
            });
        }
    }
    private void copyToClipboard(TextView textView) {
        String text = textView.getText().toString();
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Text Copied", text);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(MainActivity.this, "Copied to clipboard", Toast.LENGTH_SHORT).show();
        }

    private void selectLanguage(String langCode, int titleRes, int ierRes, int amount1Res, int amount2Res, int lutRes, int nutRes, int switchLangRes, String toastMessage) {
        titleText.setText(titleRes);
        ierText.setText(ierRes);
        amount1Text.setText(amount1Res);
        amount2Text.setText(amount2Res);
        lutText.setText(lutRes);
        nutText.setText(nutRes);
        switchLangText.setText(switchLangRes);

        Toast.makeText(getBaseContext(), toastMessage, Toast.LENGTH_SHORT).show();
        fileStreams.setSelectedLanguage(langCode);
    }

    private void selectUkrainian() {
        selectLanguage("ukrainian", R.string.UAtitleText, R.string.UAierText, R.string.UAamount1Text, R.string.UAamount2Text, R.string.UAlutText, R.string.UAnutText, R.string.UAswitchLangText, "Вибрана Українська");
    }

    private void selectRussian() {
        selectLanguage("russian", R.string.RUtitleText, R.string.RUierText, R.string.RUamount1Text, R.string.RUamount2Text, R.string.RUlutText, R.string.RUnutText, R.string.RUswitchLangText, "Выбран Русский");
    }

    private void selectEnglish() {
        selectLanguage("english", R.string.ENtitleText, R.string.ENierText, R.string.ENamount1Text, R.string.ENamount2Text, R.string.ENlutText, R.string.ENnutText, R.string.ENswitchLangText, "Selected English");
    }

    private void selectPolish() {
        selectLanguage("polish", R.string.PLtitleText, R.string.PLierText, R.string.PLamount1Text, R.string.PLamount2Text, R.string.PLlutText, R.string.PLnutText, R.string.PLswitchLangText, "Wybrany Polski");
    }

    private void selectDutch() {
        selectLanguage("dutch", R.string.NLtitleText, R.string.NLierText, R.string.NLamount1Text, R.string.NLamount2Text, R.string.NLlutText, R.string.NLnutText, R.string.NLswitchLangText, "Nederlands geselecteerd");
    }


    private String extractCurrencyCode(String fullItem) {
        Pattern pattern = Pattern.compile("\\b[A-Z]{3}\\b");
        Matcher matcher = pattern.matcher(fullItem);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }
}
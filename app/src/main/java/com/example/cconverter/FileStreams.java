package com.example.cconverter;

import android.content.Context;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class FileStreams {
    private final Context context;

    public FileStreams(Context context) {
        this.context = context;
    }

    public void setSelectedLanguage(String lang) {
        writeText(lang);
    }

    public String getSelectLang() {
        return readText();
    }

    private void writeText(String content) {
        try (FileOutputStream fos = context.openFileOutput("language.txt", Context.MODE_PRIVATE)) {
            fos.write(content.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String readText() {
        StringBuilder stringBuilder = new StringBuilder();
        try (FileInputStream fis = context.openFileInput("language.txt")) {
            int character;
            while ((character = fis.read()) != -1) {
                stringBuilder.append((char) character);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }
}

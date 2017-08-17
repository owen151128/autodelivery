package com.hpcnt.autodelivery;

import android.support.annotation.NonNull;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.List;

public class TestUtil {

    public static String getStringFromResource(ClassLoader classLoader, String fileName) {
        InputStream inputStream = classLoader.getResourceAsStream(fileName);
        String response = "";
        try {
            response = IOUtils.toString(inputStream, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    public static <T> List<T> getListObjectFromJson(@NonNull String jsonString, @NonNull Class<T> buildClass, @NonNull Type type) {
        return getListObjectFromJson(jsonString, buildClass, type, null);
    }

    public static <T> List<T> getListObjectFromJson(@NonNull String jsonString, @NonNull Class<T> buildClass, @NonNull Type type, JsonDeserializer<T> jsonDeserializer) {
        GsonBuilder builder = new GsonBuilder();
        if (jsonDeserializer != null)
            builder.registerTypeAdapter(buildClass, jsonDeserializer);

        Gson gson = builder.create();
        return gson.fromJson(jsonString, type);
    }
}

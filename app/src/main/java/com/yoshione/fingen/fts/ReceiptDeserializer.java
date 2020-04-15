package com.yoshione.fingen.fts;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.yoshione.fingen.fts.models.Document;
import com.yoshione.fingen.fts.models.FtsResponse;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

public class ReceiptDeserializer implements JsonDeserializer<FtsResponse> {

    public FtsResponse deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        FtsResponse response = new FtsResponse();
        if (!json.isJsonNull()) {
            JsonObject obj = json.getAsJsonObject();
            Set<Map.Entry<String, JsonElement>> entries = obj.entrySet();//will return members of your object
            for (Map.Entry<String, JsonElement> entry: entries) {
                response.setDocument((Document) context.deserialize(entry.getValue(), Document.class));
            }
        }
        return response;
    }
}


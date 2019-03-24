package cps.model;

import com.google.gson.*;

import java.lang.reflect.Type;

public class SignalDeserializer implements JsonDeserializer<Signal>
{
    @Override
    public Signal deserialize(JsonElement json, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        //TODO: Co to?
//        JsonElement function = jsonObject.get("function");
        return jsonDeserializationContext.deserialize(jsonObject, SignalChart.class);

    }
}

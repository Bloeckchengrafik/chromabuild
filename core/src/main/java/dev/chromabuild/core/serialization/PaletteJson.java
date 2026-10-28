package dev.chromabuild.core.serialization;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.chromabuild.core.Palette;
import dev.chromabuild.core.PaletteEntry;
import java.util.ArrayList;

/** Versioned external palette representation. Does not imply registry validity or placement permission. */
public final class PaletteJson {
    public static final int SCHEMA = 1;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private PaletteJson() { }

    public static String write(NamedPalette named) {
        JsonObject root = new JsonObject();
        root.addProperty("schema", SCHEMA);
        root.addProperty("name", named.name());
        JsonArray entries = new JsonArray();
        for (var entry : named.palette().entries()) {
            JsonObject json = new JsonObject();
            json.addProperty("block", entry.stateId());
            json.addProperty("weight", entry.weight());
            json.addProperty("position", entry.gradientPosition());
            json.addProperty("enabled", entry.enabled());
            if (!entry.variationGroup().isEmpty())
                json.addProperty("variationGroup", entry.variationGroup());
            entries.add(json);
        }
        root.add("entries", entries);
        return GSON.toJson(root);
    }

    public static NamedPalette read(String source) {
        try {
            JsonElement parsed = JsonParser.parseString(source);
            if (!parsed.isJsonObject()) throw new IllegalArgumentException("palette must be a JSON object");
            JsonObject root = parsed.getAsJsonObject();
            if (requiredInt(root, "schema") != SCHEMA)
                throw new IllegalArgumentException("unsupported palette schema");
            String name = requiredString(root, "name");
            JsonArray entries = root.getAsJsonArray("entries");
            if (entries == null || entries.size() == 0 || entries.size() > 128)
                throw new IllegalArgumentException("entries must contain 1..128 blocks");
            var decoded = new ArrayList<PaletteEntry>(entries.size());
            for (JsonElement element : entries) {
                if (!element.isJsonObject()) throw new IllegalArgumentException("entry must be an object");
                JsonObject e = element.getAsJsonObject();
                String block = requiredString(e, "block");
                double weight = requiredDouble(e, "weight");
                double pos = requiredDouble(e, "position");
                boolean enabled = !e.has("enabled") || requiredBoolean(e, "enabled");
                String group = e.has("variationGroup") ? requiredString(e, "variationGroup") : "";
                decoded.add(new PaletteEntry(block, weight, pos, enabled, group));
            }
            return new NamedPalette(name, new Palette(decoded));
        } catch (IllegalStateException | UnsupportedOperationException | NumberFormatException ex) {
            throw new IllegalArgumentException("invalid palette JSON", ex);
        }
    }

    private static String requiredString(JsonObject o, String key) {
        JsonElement e = o.get(key);
        if (e == null || !e.isJsonPrimitive() || !e.getAsJsonPrimitive().isString())
            throw new IllegalArgumentException("missing or invalid string: " + key);
        return e.getAsString();
    }
    private static int requiredInt(JsonObject o, String key) {
        JsonElement e = o.get(key);
        if (e == null || !e.isJsonPrimitive() || !e.getAsJsonPrimitive().isNumber())
            throw new IllegalArgumentException("missing or invalid integer: " + key);
        int result = e.getAsInt();
        if (e.getAsDouble() != result) throw new IllegalArgumentException("integer required: " + key);
        return result;
    }
    private static double requiredDouble(JsonObject o, String key) {
        JsonElement e = o.get(key);
        if (e == null || !e.isJsonPrimitive() || !e.getAsJsonPrimitive().isNumber())
            throw new IllegalArgumentException("missing or invalid number: " + key);
        return e.getAsDouble();
    }
    private static boolean requiredBoolean(JsonObject o, String key) {
        JsonElement e = o.get(key);
        if (e == null || !e.isJsonPrimitive() || !e.getAsJsonPrimitive().isBoolean())
            throw new IllegalArgumentException("missing or invalid boolean: " + key);
        return e.getAsBoolean();
    }
}

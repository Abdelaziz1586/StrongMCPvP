package pebbleprojects.strongMCPvP.functions.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.Map;

public final class JsonConfiguration extends ConfigurationProvider {

    private final Gson json = new GsonBuilder().serializeNulls().setPrettyPrinting().registerTypeAdapter(Configuration.class, (JsonSerializer<Configuration>) (src, typeOfSrc, context) -> context.serialize(src.self)).create();

    @Override
    public void save(Configuration config, File file) throws IOException {
        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(file.toPath()), StandardCharsets.UTF_8)) {
            save(config, writer);
        }
    }

    @Override
    public void save(Configuration config, Writer writer) {
        json.toJson(config.self, writer);
    }

    @Override
    public Configuration load(File file) throws IOException {
        return load(file, null);
    }

    @Override
    public Configuration load(File file, Configuration defaults) throws IOException {
        try (FileInputStream is = new FileInputStream(file)) {
            return load(is, defaults);
        }
    }

    @Override
    public Configuration load(Reader reader) {
        return load(reader, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Configuration load(Reader reader, Configuration defaults) {
        Map<String, Object> map = json.fromJson(reader, LinkedHashMap.class);
        if (map == null) {
            map = new LinkedHashMap<>();
        }
        return new Configuration(map, defaults);
    }

    @Override
    public Configuration load(InputStream is) {
        return load(is, null);
    }

    @Override
    public Configuration load(InputStream is, Configuration defaults) {
        return load(new InputStreamReader(is, StandardCharsets.UTF_8), defaults);
    }

    @Override
    public Configuration load(String string) {
        return load(string, null);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Configuration load(String string, Configuration defaults) {
        Map<String, Object> map = json.fromJson(string, LinkedHashMap.class);
        if (map == null) {
            map = new LinkedHashMap<>();
        }
        return new Configuration(map, defaults);
    }
}

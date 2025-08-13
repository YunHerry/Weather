package indi.yunherry.weather.loader;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
// loadConfig -> isValid -> EnableConfig
public abstract class AbstractLoader<T> {
    private static final Logger log = LoggerFactory.getLogger(AbstractLoader.class);
    protected final Gson gson = new Gson();
    // subclass must back to a LoaderName
    public abstract String getLoaderName();
    public abstract String getNamespace();
    public void loadLoader() {
        log.info("Loader: {} is loading",this.getLoaderName());
    }
    protected static AbstractLoader<?> register() {
        throw new UnsupportedOperationException("Subclass must implement register() method");
    }
    public boolean isValid(JsonObject jsonObject) {
        return jsonObject.get("loader").getAsString().equals(this.getLoaderName());
    }
    public abstract void process(JsonObject jsonObject);
    public abstract float getYAxis(LoaderConfig loaderConfig);
    public abstract Vector4f findColorByKey(String key, LoaderConfig loaderConfig);

}
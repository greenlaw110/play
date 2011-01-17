package play.mvc.results;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

import play.exceptions.UnexpectedException;
import play.mvc.Http.Request;
import play.mvc.Http.Response;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSerializer;

/**
 * 200 OK with application/json
 */
public class RenderJson extends Result {

    private static class ES_ implements ExclusionStrategy {
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }
        public boolean shouldSkipField(FieldAttributes f) {
            return f.getAnnotation(NoJsonExpose.class) != null;
        }
    }

    String json;

    protected Gson gson() {
        return gb().create();
    }
    
    protected GsonBuilder gb() {
        return  new GsonBuilder().setExclusionStrategies(new ES_()).serializeNulls();
    }

    public RenderJson(Object o) {
        json = gson().toJson(o);
    }

    public RenderJson(Object o, Type type) {
        json = new Gson().toJson(o, type);
    }

    public RenderJson(Object o, JsonSerializer<?>... adapters) {
        GsonBuilder gson = new GsonBuilder();
        for (Object adapter : adapters) {
            Type t = getMethod(adapter.getClass(), "serialize").getParameterTypes()[0];
            gson.registerTypeAdapter(t, adapter);
        }
        json = gson.create().toJson(o);
    }

    public RenderJson(String jsonString) {
        json = jsonString;
    }

    public void apply(Request request, Response response) {
        try {
            setContentTypeIfNotSet(response, "application/json; charset=utf-8");
            response.out.write(json.getBytes("utf-8"));
        } catch (Exception e) {
            throw new UnexpectedException(e);
        }
    }

    //
    static Method getMethod(Class<?> clazz, String name) {
        for (Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(name)) {
                return m;
            }
        }
        return null;
    }
}

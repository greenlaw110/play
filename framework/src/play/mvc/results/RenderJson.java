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
        return new GsonBuilder().setExclusionStrategies(new ES_()).serializeNulls();
    }

    public RenderJson(Object o) {
        json = gson().toJson(o);
    }
    
    public RenderJson(Object o, Type type) {
        json = gson().toJson(o, type);
    }

    public RenderJson(Object o, JsonSerializer<?>... adapters) {
        GsonBuilder gson = gb();
        for(Object adapter : adapters) {
            Type t = getMethod(adapter.getClass(), "serialize").getParameterTypes()[0];;
            gson.registerTypeAdapter(t, adapter);
        }
        json = gson.create().toJson(o);
    }
    
    public RenderJson(String jsonString) {
        json = jsonString;
    }

    public void apply(Request request, Response response) {
        try {
            String encoding = getEncoding();
            setContentTypeIfNotSet(response, "application/json; charset="+encoding);
            response.out.write(json.getBytes(encoding));
        } catch (Exception e) {
            throw new UnexpectedException(e);
        }
    }
    
    //
    static Method getMethod(Class clazz, String methodName) {
        Method bestMatch = null;
        for(Method m : clazz.getDeclaredMethods()) {
            if (m.getName().equals(methodName) && !m.isBridge()) {
                if (bestMatch == null || !Object.class.equals(m.getParameterTypes()[0])) {
                    bestMatch = m;
                }
            }
        }
        return bestMatch;
    }


}

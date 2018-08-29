package task.util;

import android.support.annotation.NonNull;

import java.util.Map;
import java.util.concurrent.Callable;

import io.reactivex.functions.Consumer;

/**
 * Created by sjj on 2017/11/17.
 */

public class Maps {
    @NonNull
    public static <T> T getOrPut(Map<String,T> map, String key, Callable<T> callable) {
        try {
            Object o = map.get(key);
            if (o == null) {
                map.put(key, (T) (o = callable.call()));
            }
            return (T) o;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> void modOrPut(Map<String, T> map, String key, Consumer<T> mod, Callable<T> create) {
        try {
            Object o = map.get(key);
            if (o == null) {
                map.put(key, create.call());
            } else {
                mod.accept((T) o);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}

package test.xuan.liu.com.skylinserverauth;

/**
 * Created by liuxuan on 27/4/18.
 */

public class GeneralModel<T> {
    public int ret;
    public String message;
    public T data;
    public boolean isSuccess(){
        return ret == 200;
    }
    @Override
    public String toString() {
        return "GeneralModel{" +
                "ret=" + ret +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }
}

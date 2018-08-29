package test.xuan.liu.com.skylinserverauth;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by liuxuan on 2017/4/18.
 */

public interface HttpApiService {

//    public static final String url = "https://tool.skylinuav.com";  //正式接口
    public static final String url = "https://devel.skylinuav.com";  //开发接口

    @GET(url+"/v3/api/user/device/aircraft_list")
    Call<GeneralModel<AircrasftListModel<List<ReqeustAuth>>>> getMacList(@Query("token") String token,@Query("team_id") Long team_id);

}

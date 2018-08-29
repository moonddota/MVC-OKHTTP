package test.xuan.liu.com.skylinserverauth;

/**
 * Created by liuxuan on 2018/3/5.
 */

public class AuthModel {
    public AuthModel(boolean authed, int userId, String token, String tid, String userNmae, String teamName) {
        this.authed = authed;
        this.userId = userId;
        this.token = token;
        this.tid = tid;
        this.userName = userName;
        this.teamName = teamName;

    }

    public boolean authed;
    public int userId = 0;
    public String token = "";
    public String tid = "";
    public String userName = "";
    public String teamName = "";
}

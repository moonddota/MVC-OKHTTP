package test.xuan.liu.com.skylinserverauth;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * Created by liuxuan on 2017/4/20.
 */
@Table(name = "LoginTable")
public class LoginTable {
    @Column(name = "userId", isId = true, autoGen = false)
    public int userId;

    @Column(name = "username")
    public String username;

    @Column(name = "name")
    public String name;

    @Column(name = "group")
    public String group;

    @Column(name = "authority")
    public int authority;

    @Column(name = "gauthority")
    public int gauthority;

    @Column(name = "token")
    public String token;

    @Column(name = "isRememberLogin")
    public Boolean isRememberLogin;

    @Column(name = "password")
    public String password;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public int getAuthority() {
        return authority;
    }

    public void setAuthority(int authority) {
        this.authority = authority;
    }

    public int getGauthority() {
        return gauthority;
    }

    public void setGauthority(int gauthority) {
        this.gauthority = gauthority;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Boolean getRememberLogin() {
        return isRememberLogin;
    }

    public void setRememberLogin(Boolean rememberLogin) {
        isRememberLogin = rememberLogin;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    @Override
    public String toString() {
        return "LoginTable{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", name='" + name + '\'' +
                ", group='" + group + '\'' +
                ", authority=" + authority +
                ", gauthority=" + gauthority +
                ", token='" + token + '\'' +
                ", isRememberLogin=" + isRememberLogin +
                ", password='" + password + '\'' +
                '}';
    }
}


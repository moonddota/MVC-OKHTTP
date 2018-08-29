package com.skylin.uav.drawforterrain.checksupdata;

import java.util.List;

/**
 * Created by Administrator on 2017/3/24.
 */

public class Ban {
    /**
     * ret : 200
     * data : [{"version":"0.0.10","versionCode":"10","packageName":"","info":"","path":"resource/apps/1.txt","size":"0","createTime":"0000-00-00 00:00:00","name":"test","id":1}]
     * msg :
     */

    private int ret;
    private String msg;
    private List<DataBean> data;

    @Override
    public String toString() {
        return "Ban{" +
                "ret=" + ret +
                ", msg='" + msg + '\'' +
                ", data=" + data +
                '}';
    }

    public int getRet() {
        return ret;
    }

    public void setRet(int ret) {
        this.ret = ret;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List<DataBean> getData() {
        return data;
    }

    public void setData(List<DataBean> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * version : 0.0.10
         * versionCode : 10
         * packageName :
         * info :
         * path : resource/apps/1.txt
         * size : 0
         * createTime : 0000-00-00 00:00:00
         * name : test
         * id : 1
         */

        private String version;
        private String versionCode;
        private String packageName;
        private String info;
        private String path;
        private String size;
        private String createTime;
        private String name;
        private int id;

        @Override
        public String toString() {
            return "DataBean{" +
                    "version='" + version + '\'' +
                    ", versionCode='" + versionCode + '\'' +
                    ", packageName='" + packageName + '\'' +
                    ", info='" + info + '\'' +
                    ", path='" + path + '\'' +
                    ", size='" + size + '\'' +
                    ", createTime='" + createTime + '\'' +
                    ", name='" + name + '\'' +
                    ", id=" + id +
                    '}';
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getVersionCode() {
            return versionCode;
        }

        public void setVersionCode(String versionCode) {
            this.versionCode = versionCode;
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public String getInfo() {
            return info;
        }

        public void setInfo(String info) {
            this.info = info;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getSize() {
            return size;
        }

        public void setSize(String size) {
            this.size = size;
        }

        public String getCreateTime() {
            return createTime;
        }

        public void setCreateTime(String createTime) {
            this.createTime = createTime;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }
    }


//
//    /**
//     * res : 1
//     * code : 200
//     * message : ok
//     */
//
//    private String res;
//    private int code;
//    private String message;
//
//    public String getRes() {
//        ic_return res;
//    }
//
//    public void setRes(String res) {
//        this.res = res;
//    }
//
//    public int getCode() {
//        ic_return code;
//    }
//
//    public void setCode(int code) {
//        this.code = code;
//    }
//
//    public String getMessage() {
//        ic_return message;
//    }
//
//    public void setMessage(String message) {
//        this.message = message;
//    }
}

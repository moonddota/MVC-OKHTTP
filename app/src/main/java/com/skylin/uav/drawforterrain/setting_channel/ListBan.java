package com.skylin.uav.drawforterrain.setting_channel;

import java.util.List;

/**
 * Created by wh on 2017/8/4.
 */

public class ListBan {


    /**
     * ret : 200
     * data : [[{"firmwareId":"feikongceshi","versionId":"4","versionCode":"1.0.0","fileType":"px4","description":"测试1.0.0","uploadDate":"2017-08-03 15:19:39","release":false,"name":"飞控测试"},{"firmwareId":"feikongceshi","versionId":"5","versionCode":"22","fileType":"px4","description":"222","uploadDate":"2017-08-03 15:35:04","release":false,"name":"飞控测试"},{"firmwareId":"feikongceshi","versionId":"6","versionCode":"2233","fileType":"px4","description":"1111","uploadDate":"2017-08-03 15:35:38","release":false,"name":"飞控测试"},{"firmwareId":"feikongceshi","versionId":"7","versionCode":"223377","fileType":"px4","description":"111177","uploadDate":"2017-08-03 15:35:52","release":false,"name":"飞控测试"},{"firmwareId":"feikongceshi","versionId":"8","versionCode":"111","fileType":"px4","description":"111","uploadDate":"2017-08-03 15:36:37","release":false,"name":"飞控测试"},{"firmwareId":"feikongceshi","versionId":"9","versionCode":"111","fileType":"px4","description":"11111","uploadDate":"2017-08-03 15:40:44","release":true,"name":"飞控测试"},{"firmwareId":"feikongceshi","versionId":"10","versionCode":"22222","fileType":"px4","description":"222","uploadDate":"2017-08-03 15:41:12","release":true,"name":"飞控测试"},{"firmwareId":"feikongceshi","versionId":"11","versionCode":"11","fileType":"px4","description":"11","uploadDate":"2017-08-03 15:46:06","release":false,"name":"飞控测试"},{"firmwareId":"feikongceshi","versionId":"12","versionCode":"112","fileType":"px4","description":"11","uploadDate":"2017-08-03 15:46:19","release":false,"name":"飞控测试"},{"firmwareId":"feikongceshi","versionId":"14","versionCode":"1","fileType":"px4","description":"1","uploadDate":"2017-08-03 16:08:52","release":false,"name":"飞控测试"}]]
     * msg :
     */

    private int ret;
    private String msg;
    private List<List<DataBean>> data;

    @Override
    public String toString() {
        return "ListBan{" +
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

    public List<List<DataBean>> getData() {
        return data;
    }

    public void setData(List<List<DataBean>> data) {
        this.data = data;
    }

    public static class DataBean {
        /**
         * firmwareId : feikongceshi
         * versionId : 4
         * versionCode : 1.0.0
         * fileType : px4
         * description : 测试1.0.0
         * uploadDate : 2017-08-03 15:19:39
         * release : false
         * name : 飞控测试
         */

        private String firmwareId;
        private String versionId;
        private String versionCode;
        private String fileType;
        private String description;
        private String uploadDate;
        private boolean release;
        private String name;

        @Override
        public String toString() {
            return "DataBean{" +
                    "firmwareId='" + firmwareId + '\'' +
                    ", versionId='" + versionId + '\'' +
                    ", versionCode='" + versionCode + '\'' +
                    ", fileType='" + fileType + '\'' +
                    ", description='" + description + '\'' +
                    ", uploadDate='" + uploadDate + '\'' +
                    ", release=" + release +
                    ", name='" + name + '\'' +
                    '}';
        }

        public String getFirmwareId() {
            return firmwareId;
        }

        public void setFirmwareId(String firmwareId) {
            this.firmwareId = firmwareId;
        }

        public String getVersionId() {
            return versionId;
        }

        public void setVersionId(String versionId) {
            this.versionId = versionId;
        }

        public String getVersionCode() {
            return versionCode;
        }

        public void setVersionCode(String versionCode) {
            this.versionCode = versionCode;
        }

        public String getFileType() {
            return fileType;
        }

        public void setFileType(String fileType) {
            this.fileType = fileType;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getUploadDate() {
            return uploadDate;
        }

        public void setUploadDate(String uploadDate) {
            this.uploadDate = uploadDate;
        }

        public boolean isRelease() {
            return release;
        }

        public void setRelease(boolean release) {
            this.release = release;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}

package com.skylin.uav.drawforterrain.login;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AirportBan {

    private int ret;
    private String msg;
    private List<DataBean> data;

    @Override
    public String toString() {
        return "AirportBan{" +
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
         * name : 北京/首都
         * code : ZBAA
         * height : 35.3
         * type : air
         * zones : {"a1":[40.22833333333333,116.52833333333334],"a2":[40.236666666666665,116.61333333333333],"a3":[39.916666666666664,116.66666666666667],"a4":[39.90833333333333,116.58166666666666],"c1":[40.14833333333333,116.55833333333334],"c2":[40.153333333333336,116.61],"c3":[39.99666666666667,116.63666666666667],"c4":[39.99166666666667,116.585],"b1":[40.08,116.51166666666667],"b2":[40.098333333333336,116.67833333333333],"b3":[40.06166666666667,116.685],"b4":[40.04666666666667,116.51666666666667],"radius-c2-b2":7070,"radius-b3-c3":7070,"radius-c4-b4":7070,"radius-b1-c1":7070}
         */

        private String name;
        private String code;
        private double height;
        private String type;
        private ZonesBean zones;

        @Override
        public String toString() {
            return "DataBean{" +
                    "name='" + name + '\'' +
                    ", code='" + code + '\'' +
                    ", height=" + height +
                    ", type='" + type + '\'' +
                    ", zones=" + zones +
                    '}';
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public double getHeight() {
            return height;
        }

        public void setHeight(double height) {
            this.height = height;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public ZonesBean getZones() {
            return zones;
        }

        public void setZones(ZonesBean zones) {
            this.zones = zones;
        }

        public static class ZonesBean {
            /**
             * a1 : [40.22833333333333,116.52833333333334]
             * a2 : [40.236666666666665,116.61333333333333]
             * a3 : [39.916666666666664,116.66666666666667]
             * a4 : [39.90833333333333,116.58166666666666]
             * c1 : [40.14833333333333,116.55833333333334]
             * c2 : [40.153333333333336,116.61]
             * c3 : [39.99666666666667,116.63666666666667]
             * c4 : [39.99166666666667,116.585]
             * b1 : [40.08,116.51166666666667]
             * b2 : [40.098333333333336,116.67833333333333]
             * b3 : [40.06166666666667,116.685]
             * b4 : [40.04666666666667,116.51666666666667]
             * radius-c2-b2 : 7070
             * radius-b3-c3 : 7070
             * radius-c4-b4 : 7070
             * radius-b1-c1 : 7070
             */

            @SerializedName("radius-c2-b2")
            private int radiusc2b2;
            @SerializedName("radius-b3-c3")
            private int radiusb3c3;
            @SerializedName("radius-c4-b4")
            private int radiusc4b4;
            @SerializedName("radius-b1-c1")
            private int radiusb1c1;
            private List<Double> a1;
            private List<Double> a2;
            private List<Double> a3;
            private List<Double> a4;
            private List<Double> c1;
            private List<Double> c2;
            private List<Double> c3;
            private List<Double> c4;
            private List<Double> b1;
            private List<Double> b2;
            private List<Double> b3;
            private List<Double> b4;


            @Override
            public String toString() {
                return "ZonesBean{" +
                        "radiusc2b2=" + radiusc2b2 +
                        ", radiusb3c3=" + radiusb3c3 +
                        ", radiusc4b4=" + radiusc4b4 +
                        ", radiusb1c1=" + radiusb1c1 +
                        ", a1=" + a1 +
                        ", a2=" + a2 +
                        ", a3=" + a3 +
                        ", a4=" + a4 +
                        ", c1=" + c1 +
                        ", c2=" + c2 +
                        ", c3=" + c3 +
                        ", c4=" + c4 +
                        ", b1=" + b1 +
                        ", b2=" + b2 +
                        ", b3=" + b3 +
                        ", b4=" + b4 +
                        '}';
            }

            public int getRadiusc2b2() {
                return radiusc2b2;
            }

            public void setRadiusc2b2(int radiusc2b2) {
                this.radiusc2b2 = radiusc2b2;
            }

            public int getRadiusb3c3() {
                return radiusb3c3;
            }

            public void setRadiusb3c3(int radiusb3c3) {
                this.radiusb3c3 = radiusb3c3;
            }

            public int getRadiusc4b4() {
                return radiusc4b4;
            }

            public void setRadiusc4b4(int radiusc4b4) {
                this.radiusc4b4 = radiusc4b4;
            }

            public int getRadiusb1c1() {
                return radiusb1c1;
            }

            public void setRadiusb1c1(int radiusb1c1) {
                this.radiusb1c1 = radiusb1c1;
            }

            public List<Double> getA1() {
                return a1;
            }

            public void setA1(List<Double> a1) {
                this.a1 = a1;
            }

            public List<Double> getA2() {
                return a2;
            }

            public void setA2(List<Double> a2) {
                this.a2 = a2;
            }

            public List<Double> getA3() {
                return a3;
            }

            public void setA3(List<Double> a3) {
                this.a3 = a3;
            }

            public List<Double> getA4() {
                return a4;
            }

            public void setA4(List<Double> a4) {
                this.a4 = a4;
            }

            public List<Double> getC1() {
                return c1;
            }

            public void setC1(List<Double> c1) {
                this.c1 = c1;
            }

            public List<Double> getC2() {
                return c2;
            }

            public void setC2(List<Double> c2) {
                this.c2 = c2;
            }

            public List<Double> getC3() {
                return c3;
            }

            public void setC3(List<Double> c3) {
                this.c3 = c3;
            }

            public List<Double> getC4() {
                return c4;
            }

            public void setC4(List<Double> c4) {
                this.c4 = c4;
            }

            public List<Double> getB1() {
                return b1;
            }

            public void setB1(List<Double> b1) {
                this.b1 = b1;
            }

            public List<Double> getB2() {
                return b2;
            }

            public void setB2(List<Double> b2) {
                this.b2 = b2;
            }

            public List<Double> getB3() {
                return b3;
            }

            public void setB3(List<Double> b3) {
                this.b3 = b3;
            }

            public List<Double> getB4() {
                return b4;
            }

            public void setB4(List<Double> b4) {
                this.b4 = b4;
            }
        }
    }
}

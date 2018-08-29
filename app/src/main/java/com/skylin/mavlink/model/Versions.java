package com.skylin.mavlink.model;

/**
 * Created by SJJ on 2017/6/19.
 */

public class Versions {
    public final int serial;
    public final int type;
    public final int version;
    public final String versions;
    public String updateInfo;
    public Versions(String versions) {
        int[] parse = parse(versions);
        this.serial = parse[0];
        this.type = parse[1];
        this.version = parse[2];
        this.versions = versions.split(" ")[0].trim();
    }

    public Versions(int serial, int type, int version) {
        this.serial = serial;
        this.type = type;
        this.version = version;
        this.versions = "TQ-" + serial + "." + type + "." + version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Versions)) return false;

        Versions versions1 = (Versions) o;

        if (serial != versions1.serial) return false;
        if (type != versions1.type) return false;
        if (version != versions1.version) return false;
        return versions != null ? versions.equals(versions1.versions) : versions1.versions == null;

    }

    @Override
    public int hashCode() {
        int result = serial;
        result = 31 * result + type;
        result = 31 * result + version;
        result = 31 * result + (versions != null ? versions.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Versions{" +
                "serial=" + serial +
                ", type=" + type +
                ", version=" + version +
                ", versions='" + versions + '\'' +
                '}';
    }

    private int[] parse(String s) {
        try {
            String[] split = s.split("-");
            String[] strings = split[1].split(" ")[0].trim().split("\\.");
            return new int[]{Integer.parseInt(strings[0]), Integer.parseInt(strings[1]), Integer.parseInt(strings[2])};
        } catch (Exception e) {
            return new int[]{-1, -1, -1};
        }
    }
}

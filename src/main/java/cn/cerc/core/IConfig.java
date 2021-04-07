package cn.cerc.core;

public interface IConfig {
    String getProperty(String key, String def);

    default public String getProperty(String key) {
        return this.getProperty(key, null);
    }
}

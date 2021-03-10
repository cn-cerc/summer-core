package cn.cerc.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClassConfig {
    private static final Map<String, Properties> buff = new HashMap<>();
    private static final String CONFIGFILE_APPLICATION = "/application.properties";
    private static final Properties applicationConfig = new Properties();
    private Properties packageConfig;
    private String classPath = null;

    private static final Properties localConfig = new Properties();

    static {
        // 加载本地文件配置
        String path = System.getProperty("user.home") + System.getProperty("file.separator");
        String confFile = path + "summer-application.properties";
        try {
            localConfig.clear();
            File file = new File(confFile);
            if (file.exists()) {
                localConfig.load(new FileInputStream(confFile));
                log.info("{} is loaded.", confFile);
            } else {
                log.warn("{} doesn't exist.", confFile);
            }
        } catch (FileNotFoundException e) {
            log.error("The settings file does not exist: {}'", confFile);
        } catch (IOException e) {
            log.error("Failed to load the settings from the file: {}", confFile);
        }
        // 加载项目文件配置
        try {
            InputStream file = ClassConfig.class.getResourceAsStream(CONFIGFILE_APPLICATION);
            if (file != null) {
                applicationConfig.load(new InputStreamReader(file, StandardCharsets.UTF_8));
                log.info("{} is loaded.", CONFIGFILE_APPLICATION);
            } else {
                log.warn("{} doesn't exist.", CONFIGFILE_APPLICATION);
            }
        } catch (IOException e) {
            log.error("Failed to load the settings from the file: {}", CONFIGFILE_APPLICATION);
        }
    }

    public ClassConfig() {

    }

    public ClassConfig(Object owner, String packageName) {
        if (owner instanceof Class)
            this.classPath = ((Class<?>) owner).getName();
        else
            this.classPath = owner.getClass().getName();
        init(packageName);
    }

    private void init(String packageName) {
        if (packageName == null)
            return;

        String configFileName = String.format("/%s.properties", packageName);
        packageConfig = buff.get(packageName);
        if (packageConfig != null)
            return;

        packageConfig = new Properties();
        if (buff.putIfAbsent(packageName, packageConfig) != null)
            return;

        try {
            final InputStream configFile = ClassConfig.class.getResourceAsStream(configFileName);
            if (configFile != null) {
                packageConfig.load(new InputStreamReader(configFile, StandardCharsets.UTF_8));
                log.info("{} is loaded.", configFileName);
            } else {
                log.warn("{} doesn't exist.", configFileName);
            }
        } catch (IOException e) {
            log.error("Failed to load the settings from the file: {}", configFileName);
        }
    }

    /**
     * 读取以自动加上类名开头的数据
     * 
     * @param key          例如 cn.cerc.core.ClassConfig.1
     * @param defaultValue
     * @return
     */
    public String getString(String key, String defaultValue) {
        if (classPath == null) {
            log.warn("classPath is null.");
            return getValue(key, defaultValue);
        }
        return getValue(String.format("%s.%s", this.classPath, key), defaultValue);
    }

    /**
     * 直接读取文件底层数据，尽量改使用 getString
     * 
     * @param key          例如 app.language
     * @param defaultValue
     * @return
     */
    public String getValue(String key, String defaultValue) {
        log.debug("key: {}", key);
        if (localConfig.containsKey(key))
            return localConfig.getProperty(key);
        else if (applicationConfig.containsKey(key))
            return applicationConfig.getProperty(key);
        else if (packageConfig != null)
            return packageConfig.getProperty(key, defaultValue);
        else
            return defaultValue;
    }

    public static void main(String[] args) {
        ClassConfig config = new ClassConfig(ClassConfig.class, "summer-core-cn");
        System.out.println(config.getValue("cn.cerc.core.DataSet.1", "not find."));
        System.out.println(config.getValue("app.language", "not find."));
    }
}

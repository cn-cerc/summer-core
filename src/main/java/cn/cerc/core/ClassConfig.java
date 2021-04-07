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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassConfig implements IConfig {
	private static final Logger log = LoggerFactory.getLogger(ClassConfig.class);
    private static final Map<String, Properties> buff = new HashMap<>();
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
        String confApp = "/application.properties";
        try {
            InputStream file = ClassConfig.class.getResourceAsStream(confApp);
            if (file != null) {
                applicationConfig.load(new InputStreamReader(file, StandardCharsets.UTF_8));
                log.info("{} is loaded.", confApp);
            } else {
                log.warn("{} doesn't exist.", confApp);
            }
        } catch (IOException e) {
            log.error("Failed to load the settings from the file: {}", confApp);
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
     * 读取配置文件数据，不会自动追加class路径，否则请使用 getClassValue
     * 
     * @param key          例如 app.language
     * @param defaultValue 默认值
     * @return 返回字符值
     */
    public String getString(String key, String defaultValue) {
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

    public boolean getBoolean(String key, boolean defaultValue) {
        String value = getString(key, String.valueOf(defaultValue));
        if ("1".equals(value)) {
            log.warn("key {} config old, Please up to use true/false", key);
            return true;
        }
        return "true".equals(value);
    }

    public int getInt(String key, int defaultValue) {
        return Integer.parseInt(getString(key, String.valueOf(defaultValue)));
    }

    /**
     * 读取以自动加上类名开头的数据
     * 
     * @param key          例如 cn.cerc.core.ClassConfig.1
     * @param defaultValue 定义默认值
     * @return 返回值，可为null
     */
    public String getClassProperty(String key, String defaultValue) {
        if (classPath == null) {
            log.warn("classPath is null.");
            return getString(key, defaultValue);
        }
        return getString(String.format("config.%s.%s", this.classPath, key), defaultValue);
    }

    /**
     * 直接读取文件底层数据，尽量改使用 getString
     * 
     * @param key          例如 app.language
     * @param defaultValue 定义默认值
     * @return 返回值，可为null
     */
    @Override
    @Deprecated
    public String getProperty(String key, String defaultValue) {
        return getString(key, defaultValue);
    }

    public static void main(String[] args) {
        ClassConfig config = new ClassConfig(ClassConfig.class, "summer-core-cn");
        System.out.println(config.getString("cn.cerc.core.DataSet.1", "not find."));
        System.out.println(config.getString("app.language", "not find."));
    }

}

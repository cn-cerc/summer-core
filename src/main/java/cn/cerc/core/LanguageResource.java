package cn.cerc.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LanguageResource {
    private static final Logger log = LoggerFactory.getLogger(LanguageResource.class);
    /**
     * 英语美国
     */
    public static final String LANGUAGE_EN = "en";
    /**
     * 简体中文
     */
    public static final String LANGUAGE_CN = "cn";
    /**
     * 繁体中文
     */
    public static final String LANGUAGE_TW = "tw";
    public static final String LANGUAGE_SG = "sg";

    public static String appLanguage = "en";

    private String userLanguage;
    private String resourceFileName;
    private Properties resourceProperties;
    private static Map<String, Properties> items = new HashMap<>();

    static {
        appLanguage = (new ClassConfig(LanguageResource.class, null)).getString("app.language", appLanguage);
    }

    public LanguageResource(String projectId) {
        initResource(projectId, appLanguage);
    }

    public LanguageResource(String projectId, String userLanguage) {
        initResource(projectId, userLanguage);
    }

    private void initResource(String projectId, String userLanguage) {
        if (this.userLanguage == userLanguage)
            return;

        this.userLanguage = userLanguage;// 用户级的语言类型
        if (Utils.isEmpty(userLanguage)) {
            userLanguage = appLanguage;// 若用户没有传入语言类型则默认选择英文文件
        }
        String resourceFileName = String.format("/%s-%s.properties", projectId, userLanguage);
        if (items.containsKey(resourceFileName)) {
            this.resourceFileName = resourceFileName;
            log.debug("{} is reload.", resourceFileName);
            resourceProperties = items.get(resourceFileName);
            return;
        }

        try {
            InputStream inputStream = LanguageResource.class.getResourceAsStream(resourceFileName);
            if (inputStream == null) {
                resourceFileName = String.format("/%s.properties", projectId);
                inputStream = LanguageResource.class.getResourceAsStream(resourceFileName);
            }
            if (inputStream != null) {
                this.resourceFileName = resourceFileName;
                log.info("{} is loaded.", resourceFileName);
                resourceProperties = new Properties();
                resourceProperties.load(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                items.put(resourceFileName, resourceProperties);
            } else {
                log.warn("{} does not exist.", resourceFileName);
            }
        } catch (IOException e) {
            log.error("Failed to load the settings from the file: {}", resourceFileName);
        }
    }

    public String getString(String key, String text) {
        if(resourceProperties == null)
            return text;
        
        if (!resourceProperties.containsKey(key)) {
            log.error("resourceFileName {}, appLanguage {}, userLanguage {}, resource key {}, does not exist.",
                    resourceFileName, appLanguage, userLanguage, key);
        }
        
        return resourceProperties.getProperty(key, text);
    }

    public String getCurrentLanguage() {
        return appLanguage;
    }

    public static boolean isLanguageTW() {
        return LANGUAGE_TW.equals(appLanguage);
    }

    public static boolean isLanguageCN() {
        return LANGUAGE_CN.equals(appLanguage);
    }

    public static boolean isLanguageSG() {
        return LANGUAGE_SG.equals(appLanguage);
    }

    public static boolean isLanguageEN() {
        return LANGUAGE_EN.equals(appLanguage);
    }

    public void debugList(Class<?> clazz) {
        int i = 1;
        String key = String.format("%s.%d", clazz.getName(), i);
        while (resourceProperties.containsKey(key)) {
            System.out.println(String.format("%s=%s", key, resourceProperties.getProperty(key)));
            i++;
            key = String.format("%s.%d", clazz.getName(), i);
        }
    }

}

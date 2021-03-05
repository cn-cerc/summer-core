package cn.cerc.core;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

@Slf4j
public class LanguageResource {

    public static final String LANGUAGE_EN = "en";
    public static final String LANGUAGE_CN = "cn";
    public static final String LANGUAGE_TW = "tw";
    public static final String LANGUAGE_SG = "sg";

    private static String currentLanguage = "en";
    private static final Properties resourceProperties = new Properties();

    static {
        final String configFileName = "/application.properties";
        Properties config = new Properties();
        try {
            final InputStream configFile = LanguageResource.class.getResourceAsStream(configFileName);
            if (configFile != null) {
                config.load(configFile);
                log.info("read file: {}", configFileName);
            } else {
                log.warn("{} does not exist.", configFileName);
            }
            currentLanguage = config.getProperty("currentLanguage", currentLanguage);
            log.info("currentLanguage value: {}", currentLanguage);
        } catch (IOException e) {
            log.error("Failed to load the settings from the file: {}", configFileName);
        }

    }

    public LanguageResource(String projectId) {
        initResource(projectId, currentLanguage);
    }

    public LanguageResource(String projectId, String userLanguage) {
        initResource(projectId, userLanguage);
    }

    private void initResource(String projectId, String userLanguage) {
        if (Utils.isEmpty(userLanguage)) {
            userLanguage = currentLanguage;
        }
        String resourceFileName = String.format("/%s-%s.properties", projectId, userLanguage);
        try {
            InputStream resourceString = LanguageResource.class.getResourceAsStream(resourceFileName);
            if (resourceString == null) {
                resourceFileName = String.format("/%s.properties", projectId);
                resourceString = LanguageResource.class.getResourceAsStream(resourceFileName);
            }
            if (resourceString != null) {
                resourceProperties.load(new InputStreamReader(resourceString, StandardCharsets.UTF_8));
            } else {
                log.warn("{} does not exist.", resourceFileName);
            }
        } catch (IOException e) {
            log.error("Failed to load the settings from the file: {} ", resourceFileName);
        }
    }

    public String getString(String key, String text) {
        if (!resourceProperties.containsKey(key)) {
            log.info("Language {} String resource key {} does not exist.", currentLanguage, key);
        }
        String value = resourceProperties.getProperty(key, text);
        log.info("language {}, key {}, input {}, output {}", currentLanguage, key, text, value);
        return value;
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public static boolean isLanguageTW() {
        return LANGUAGE_TW.equals(currentLanguage);
    }

    public static boolean isLanguageCN() {
        return LANGUAGE_CN.equals(currentLanguage);
    }

    public static boolean isLanguageSG() {
        return LANGUAGE_SG.equals(currentLanguage);
    }

    public static boolean isLanguageEN() {
        return LANGUAGE_EN.equals(currentLanguage);
    }

    public static void debugList(Class<?> clazz) {
        int i = 1;
        String key = String.format("%s.%d", clazz.getName(), i);
        while (resourceProperties.containsKey(key)) {
            System.out.println(String.format("%s=%s", key, resourceProperties.getProperty(key)));
            i++;
            key = String.format("%s.%d", clazz.getName(), i);
        }
    }

    public static void main(String[] args) {
        LanguageResource.debugList(DataSet.class);
    }

}

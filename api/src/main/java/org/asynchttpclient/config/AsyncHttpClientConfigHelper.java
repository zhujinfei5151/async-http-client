package org.asynchttpclient.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.asynchttpclient.internal.chmv8.ConcurrentHashMapV8;

public class AsyncHttpClientConfigHelper {

    private static volatile Config config;

    public static Config getAsyncHttpClientConfig() {
        if (config == null) {
            config = new Config();
        }

        return config;
    }

    /**
     * This method invalidates the property caches. So if a system property has
     * been changed and the effect of this change is to be seen then call
     * reloadProperties() and then getAsyncHttpClientConfig() to get the new
     * property values.
     */
    public static void reloadProperties() {
        if (config != null)
            config.reload();
    }

    public static class Config {

        public static final String DEFAULT_AHC_PROPERTIES = "ahc-default.properties";
        public static final String CUSTOM_AHC_PROPERTIES = "ahc.properties";

        private final ConcurrentHashMapV8<String, String> propsCache = new ConcurrentHashMapV8<String, String>();
        private final Properties defaultProperties = parsePropertiesFile(DEFAULT_AHC_PROPERTIES);
        private volatile Properties customProperties = parsePropertiesFile(CUSTOM_AHC_PROPERTIES);

        public void reload() {
            customProperties = parsePropertiesFile(CUSTOM_AHC_PROPERTIES);
            propsCache.clear();
        }

        private Properties parsePropertiesFile(String file) {
            Properties props = new Properties();
            try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(file)) {
                if (is != null) {
                    props.load(is);
                }
            } catch (IOException e) {
                throw new IllegalArgumentException("Can't parse file", e);
            }
            return props;
        }

        public String getString(String key) {
            return propsCache.computeIfAbsent(key, new ConcurrentHashMapV8.Fun<String, String>() {

                @Override
                public String apply(String key) {
                    String value = System.getProperty(key);
                    if (value == null) {
                        value = (String) customProperties.getProperty(key);
                    }
                    if (value == null) {
                        value = (String) defaultProperties.getProperty(key);
                    }

                    return value;
                }
            });
        }

        public int getInt(String key) {
            return Integer.parseInt(getString(key));
        }

        public long getLong(String key) {
            return Long.parseLong(getString(key));
        }
        
        public Integer getInteger(String key) {
            String s = getString(key);
            return s != null ? Integer.valueOf(s) : null;
        }
        
        public boolean getBoolean(String key) {
            return Boolean.parseBoolean(getString(key));
        }
    }
}

package com.mmall.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

public class PropertiesUtil {

    private static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);

    private static Properties props;

    static {
        String fileName = "mmall.properties";
        props = new Properties();

        try {
            props.load(new InputStreamReader(PropertiesUtil.class.getClassLoader().getResourceAsStream(fileName), "UTF-8"));
        } catch (IOException e) {

            logger.error("配置文件获取异常： ", e);
            e.printStackTrace();
        }
    }

    public static String getProperty(String key){
        String value = props.getProperty(key);
        if( !StringUtils.isBlank(value)){
            return value;
        }
        return null;
    }

    public static String getProperty(String key, String defaultValue){
        String value = props.getProperty(key);
        if( !StringUtils.isBlank(value)){
            return value;
        }
        return defaultValue;
    }
}

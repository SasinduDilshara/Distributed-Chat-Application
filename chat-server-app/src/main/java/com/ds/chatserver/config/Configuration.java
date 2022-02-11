package com.ds.chatserver.config;

import com.ds.chatserver.clienthandler.ClientRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

public class Configuration {

    private static Properties properties;
    private static final Logger logger = LoggerFactory.getLogger(ClientRequestHandler.class);

    public Configuration() {
        properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("application.properties"));

        } catch (IOException e) {
            logger.info("IOException Occurred while loading properties file - {}", e.getMessage());
        }
    }

    public static String readProperty(String keyName) {
        return properties.getProperty(keyName, "Incorrect key in the properties file");
    }

}


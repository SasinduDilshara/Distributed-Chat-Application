package com.ds.chatserver.config;

import com.ds.chatserver.clienthandler.ClientRequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Properties;

public class Configuration {

    private static Properties properties;
    private static final Logger logger = LoggerFactory.getLogger(ClientRequestHandler.class);
    private InputStream inputStream;

    public Configuration() {
        properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("application.properties"));
            inputStream = getClass().getClassLoader().getResourceAsStream("banner.txt");
        } catch (IOException e) {
            logger.info("IOException Occurred while loading properties file - {}", e.getMessage());
        }
    }

    public static String readProperty(String keyName) {
        return properties.getProperty(keyName, "Incorrect key in the properties file");
    }

    public void bannerPrinter() throws IOException{
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        while((line = bufferedReader.readLine()) != null) {
            System.out.println(line);
        }
        System.out.println("====================================================================");
        System.out.println("");
    }

}


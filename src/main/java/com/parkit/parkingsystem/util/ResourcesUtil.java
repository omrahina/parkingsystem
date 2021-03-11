package com.parkit.parkingsystem.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.Properties;

public class ResourcesUtil {

    public static final String PROPERTIES_FILE_NAME = "application.properties";

    /**
     * This method looks for a value throughout a properties file
     * @param key the property needed
     * @return the value corresponding to a specified key or throw an exception if the key isn't in the properties
     */
    public String getProperty(String key){
        String value = "";
        Properties properties = new Properties();
        try {
            properties.load(getFileFromResourcesAsStream(PROPERTIES_FILE_NAME));
            value = properties.getProperty(key);
            if (value == null){
                throw new InvalidParameterException("Missing value for key " +key);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * Gets a file as an input stream
     * @param filename a string that represents a file in the resources folder
     * @return a stream retrieved from the classLoader or throw an exception if the file doesn't exist
     * @throws FileNotFoundException
     */
    public InputStream getFileFromResourcesAsStream(String filename) throws FileNotFoundException {
        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        InputStream inputStream = classloader.getResourceAsStream(filename);
        if(inputStream == null){
            throw new FileNotFoundException("File not found "+filename);
        }
        return inputStream;
    }
}

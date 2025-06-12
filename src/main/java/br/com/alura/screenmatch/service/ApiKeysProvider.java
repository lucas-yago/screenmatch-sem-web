package br.com.alura.screenmatch.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class ApiKeysProvider {

    public static String getApiKey(String api) {
        try (FileInputStream input = new FileInputStream("config.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            return prop.getProperty(api);

        } catch (IOException e) {
            throw new RuntimeException("Erro ao carregar config.properties" + e);
        }
    }
}
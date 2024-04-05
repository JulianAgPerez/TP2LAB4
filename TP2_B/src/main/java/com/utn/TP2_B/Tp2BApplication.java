package com.utn.TP2_B;

import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;

import java.io.IOException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

@SpringBootApplication
public class Tp2BApplication {

    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> countriesCollection;

    public Tp2BApplication() {
        // Conexión a la base de datos MongoDB
        String connectionString = "mongodb://localhost:27017";
        mongoClient = MongoClients.create(new ConnectionString(connectionString));
        database = mongoClient.getDatabase("países_db");
        countriesCollection = database.getCollection("países");
    }

    // Método para seleccionar documentos de la colección países donde la región sea Americas
    public void selectCountriesByRegion(String region) {
        System.out.println("Países en la región de " + region + ":");
        countriesCollection.find(Filters.eq("region", region)).forEach((document) -> {
            System.out.println(document.toJson());
        });
    }

    // Método para seleccionar documentos de la colección países donde la región sea Americas y la población sea mayor a 100000000
    public void selectCountriesByRegionAndPopulation(String region, int population) {
        System.out.println("Países en la región de " + region + " con población mayor a " + population + ":");
        countriesCollection.find(Filters.and(Filters.eq("region", region), Filters.gt("población", population))).forEach((document) -> {
            System.out.println(document.toJson());
        });
    }

    // Método para seleccionar documentos de la colección países donde la región sea distinto de Africa
    public void selectCountriesNotInAfrica() {
        System.out.println("Países en regiones distintas de Africa:");
        countriesCollection.find(Filters.ne("region", "Africa")).forEach((document) -> {
            System.out.println(document.toJson());
        });
    }

    // Método para actualizar el documento de la colección países donde el name sea Egypt
    public void updateCountry(String name, String newName, int newPopulation) {
        countriesCollection.updateOne(Filters.eq("name", name), Updates.combine(Updates.set("name", newName), Updates.set("population", newPopulation)));
        System.out.println("Documento actualizado.");
    }

    // Método para eliminar el documento de la colección países donde el código del país sea 258
    public void deleteCountry(int countryCode) {
        countriesCollection.deleteOne(Filters.eq("código", countryCode));
        System.out.println("Documento eliminado.");
    }

    // Método para seleccionar documentos de la colección países cuya población sea mayor a 50000000 y menor a 150000000
    public void selectCountriesByPopulationRange(int minPopulation, int maxPopulation) {
        System.out.println("Países con población entre " + minPopulation + " y " + maxPopulation + ":");
        countriesCollection.find(Filters.and(Filters.gt("población", minPopulation), Filters.lt("población", maxPopulation))).forEach((document) -> {
            System.out.println(document.toJson());
        });
    }

    // Método para seleccionar documentos de la colección países ordenados por nombre en forma ascendente
    public void selectCountriesSortedByName() {
        System.out.println("Países ordenados por nombre:");
        countriesCollection.find().sort(new Document("name", 1)).forEach((document) -> {
            System.out.println(document.toJson());
        });
    }

    // Método para cerrar la conexión a la base de datos MongoDB
    public void closeConnection() {
        mongoClient.close();
    }

    public static void main(String[] args) {
        Tp2BApplication example = new Tp2BApplication();

        // Migración de datos
        for (int codigo = 1; codigo <= 300; codigo++) {
            try {
                // Realizar llamada HTTP para obtener los datos JSON
                String url = "https://restcountries.com/v2/callingcode/" + codigo;
                String datosJSON = obtenerDatosJSON(url);

                // Si hay datos JSON, procesarlos y migrar a la colección paises
                if (datosJSON != null) {
                    JSONObject jsonObject = new JSONObject(datosJSON);
                    String nombrePais = jsonObject.getString("name");
                    String capitalPais = jsonObject.getString("capital");
                    String region = jsonObject.getString("region");
                    int poblacion = jsonObject.getInt("population");
                    JSONArray latlng = jsonObject.getJSONArray("latlng");
                    double latitud = latlng.getDouble(0);
                    double longitud = latlng.getDouble(1);
                    String codigoPais = jsonObject.getJSONArray("callingCodes").getString(0);

                    // Verificar si el país ya existe en la colección paises
                    Document existingCountry = example.countriesCollection.find(Filters.eq("codigoPais", codigoPais)).first();
                    if (existingCountry != null) {
                        // Actualizar documento existente
                        example.countriesCollection.updateOne(Filters.eq("codigoPais", codigoPais), Updates.combine(
                                Updates.set("nombrePais", nombrePais),
                                Updates.set("capitalPais", capitalPais),
                                Updates.set("region", region),
                                Updates.set("población", poblacion),
                                Updates.set("latitud", latitud),
                                Updates.set("longitud", longitud)
                        ));
                        System.out.println("Documento actualizado para código de país: " + codigoPais);
                    } else {
                        // Insertar nuevo documento
                        Document newCountryDocument = new Document("codigoPais", codigoPais)
                                .append("nombrePais", nombrePais)
                                .append("capitalPais", capitalPais)
                                .append("region", region)
                                .append("población", poblacion)
                                .append("latitud", latitud)
                                .append("longitud", longitud);
                        example.countriesCollection.insertOne(newCountryDocument);
                        System.out.println("Nuevo documento insertado para código de país: " + codigoPais);
                    }
                } else {
                    System.out.println("No hay datos para el código de país: " + codigo);
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
        }
        example.selectCountriesByRegion("Americas");
        example.selectCountriesByRegionAndPopulation("Americas", 100000000);
        example.selectCountriesNotInAfrica();
        example.updateCountry("Egypt", "Egipto", 95000000);
        example.deleteCountry(258);
        example.selectCountriesByPopulationRange(50000000, 150000000);
        example.selectCountriesSortedByName();
        example.closeConnection();
    }

    // Método para obtener datos JSON de una URL
    public static String obtenerDatosJSON(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.connect();

        // Leer la respuesta de la conexión
        Scanner scanner = new Scanner(url.openStream());
        StringBuilder response = new StringBuilder();
        while (scanner.hasNext()) {
            response.append(scanner.nextLine());
        }
        scanner.close();

        return response.toString();
    }
}

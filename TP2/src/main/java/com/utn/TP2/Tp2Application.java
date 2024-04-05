package com.utn.TP2;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Tp2Application {


    public static void main(String[] args) {
        Properties properties = new Properties();

        try (FileInputStream fis = new FileInputStream("config.properties")) {
            properties.load(fis);
            String dbUrl = properties.getProperty("db.url");
            String dbUser = properties.getProperty("db.user");
            String dbPassword = properties.getProperty("db.password");
            System.out.println("URL de la base de datos: " + dbUrl);
            System.out.println("Usuario de la base de datos: " + dbUser);


            try {
                Connection conexion = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
                System.out.println("Conexion hecha");
                for (int codigo = 1; codigo <= 300; codigo++) {
                    String urlApi = "https://restcountries.com/v2/callingcode/" + codigo;
                    System.out.println(urlApi);
                    JSONArray datosJson = obtenerDatosJson(urlApi);

                    if (datosJson != null) {
                        for (int i = 0; i < datosJson.length(); i++) {

                            JSONObject dato = datosJson.getJSONObject(i);
                            String nombrePais = dato.getString("name");
                            String capitalPais = dato.optString("capital", "no tiene/no registrado");
                            String region = dato.getString("region");
                            long poblacion = dato.getLong("population");
                            double latitud=0;   //valor predeterminado
                            double longitud=0;  //valor predeterminado

                            if (dato.has("latlng")) {
                                JSONArray latlngArray = dato.getJSONArray("latlng");
                                for (int j = 0; j < latlngArray.length(); j++) {
                                    if (j == 0) {
                                        latitud = latlngArray.getDouble(j);
                                    } else if (j == 1) {
                                        longitud = latlngArray.getDouble(j);
                                    }
                                }
                                // Si solo hay una coordenada (latitud) se asume que la longitud es cero
                                if (latlngArray.length() == 1) {
                                    longitud = 0.0;
                                }
                            }


                            // Uso del método para validar y truncar nombrePais, capitalPais y region
                            nombrePais = truncarCadena(nombrePais);
                            capitalPais = truncarCadena(capitalPais);
                            region = truncarCadena(region);

                            // Buscar país en la base de datos filtrando por código
                            String query = "SELECT * FROM Pais WHERE codigoPais = ?";
                            PreparedStatement consulta = conexion.prepareStatement(query);
                            consulta.setInt(1, codigo);
                            ResultSet resultado = consulta.executeQuery();

                            if (resultado.next()) {
                                // Ejecutar un update a la tabla país
                                query = "UPDATE Pais SET nombrePais = ?, capitalPais = ?, region = ?, poblacion = ?, latitud = ?, longitud = ? WHERE codigoPais = ?";
                                consulta = conexion.prepareStatement(query);
                                consulta.setString(1, nombrePais);
                                consulta.setString(2, capitalPais);
                                consulta.setString(3, region);
                                consulta.setLong(4, poblacion);
                                consulta.setDouble(5, latitud);
                                consulta.setDouble(6, longitud);
                                consulta.setInt(7, codigo);
                                consulta.executeUpdate();
                            } else {
                                // Ejecutar un insert a la tabla país
                                query = "INSERT INTO Pais (codigoPais, nombrePais, capitalPais, region, poblacion, latitud, longitud) VALUES (?, ?, ?, ?, ?, ?, ?)";
                                consulta = conexion.prepareStatement(query);
                                consulta.setInt(1, codigo);
                                consulta.setString(2, nombrePais);
                                consulta.setString(3, capitalPais);
                                consulta.setString(4, region);
                                consulta.setLong(5, poblacion);
                                consulta.setDouble(6, latitud);
                                consulta.setDouble(7, longitud);
                                consulta.executeUpdate();
                            }
                        }
                    }
                }
                conexion.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JSONArray obtenerDatosJson(String urlApi) {
        try {
            URL url = new URL(urlApi);
            HttpURLConnection conexion = (HttpURLConnection) url.openConnection();
            conexion.setRequestMethod("GET");

            BufferedReader lector = new BufferedReader(new InputStreamReader(conexion.getInputStream()));
            StringBuilder respuesta = new StringBuilder();
            String linea;

            while ((linea = lector.readLine()) != null) {
                respuesta.append(linea);
            }

            lector.close();
            conexion.disconnect();

            return new JSONArray(respuesta.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //Verificacion que evita que superen los 50 caracteres
    public static String truncarCadena(String cadena) {
        if (cadena.length() > 50) {
            return cadena.substring(0, 50);
        } else {
            return cadena;
        }
    }

}

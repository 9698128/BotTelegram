import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

public class JDMExpoScraper {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/jdmbot_database";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "";

    public static void main(String[] args) {
        // URL iniziale della prima pagina
        String baseUrl = "https://jdm-expo.com";
        String url = "/4-jdm-sports";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            System.out.println("Connessione al database riuscita!");

            // Disattiva tutte le offerte attive prima dello scraping
            deactivateExistingCars(connection);

            // Loop per navigare tra le pagine
            while (url != null && !url.isEmpty()) {
                // Connessione alla pagina corrente e parsing del suo contenuto HTML
                Document doc = Jsoup.connect(baseUrl + url).get();
                System.out.println("Scraping page: " + baseUrl + url);

                // Selezione dei container principali che contengono i dettagli delle auto
                Elements cars = doc.select("ul.products-grid li.item");

                for (Element car : cars) {
                    // Estrazione info per ogni auto
                    String name = car.select("h2.product-name a").text();
                    if (name.isEmpty()) {
                        name = "No Name Available";
                    }

                    String link = car.select("h2.product-name a").attr("href");
                    if (link.isEmpty()) {
                        link = "No Link Available";
                    }

                    String imageUrl = car.select("a.product-image img").attr("src");
                    if (imageUrl.isEmpty()) {
                        imageUrl = "No Image Available";
                    }

                    // Inserimento o aggiornamento info nella tabella `cars`
                    saveCarToDatabase(connection, name, imageUrl, link);

                    System.out.println("Name: " + name);
                    System.out.println("Link: " + link);
                    System.out.println("Image: " + imageUrl);
                    System.out.println("------------------------------------");
                }

                // Selezione link alla pagina successiva
                Element nextPageElement = doc.select("li#pagination_next_bottom a").first();
                if (nextPageElement != null) {
                    url = nextPageElement.attr("href");
                    if (!url.startsWith("/")) {
                        url = "/" + url;
                    }
                } else {
                    // Fine delle pagine
                    url = null;
                }
            }

            System.out.println("Scraping JDM Expo completato con successo!");
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Disattiva tutte le offerte attive nel database impostando `is_active` a `FALSE`.
     */
    private static void deactivateExistingCars(Connection connection) throws SQLException {
        String deactivateQuery = "UPDATE cars SET is_active = FALSE WHERE is_active = TRUE";
        try (Statement statement = connection.createStatement()) {
            int rowsAffected = statement.executeUpdate(deactivateQuery);
            System.out.println("Offerte disattivate: " + rowsAffected);
        }
    }

    /**
     * Inserisce o aggiorna un'auto nel database.
     */
    private static void saveCarToDatabase(Connection connection, String name, String imageUrl, String detailsUrl) {
        String upsertQuery = "INSERT INTO cars (name, image_url, details_url, is_active) " +
                "VALUES (?, ?, ?, TRUE) " +
                "ON DUPLICATE KEY UPDATE " +
                "name = VALUES(name), image_url = VALUES(image_url), details_url = VALUES(details_url), is_active = TRUE";

        try (PreparedStatement preparedStatement = connection.prepareStatement(upsertQuery)) {
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, imageUrl);
            preparedStatement.setString(3, detailsUrl);

            preparedStatement.executeUpdate();
            System.out.println("Dati salvati/aggiornati nel database: " + name);
        } catch (SQLException e) {
            System.err.println("Errore durante l'inserimento/aggiornamento dei dati: " + e.getMessage());
        }
    }
}

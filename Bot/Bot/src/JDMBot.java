import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JDMBot extends TelegramLongPollingBot {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/jdmbot_database";
    private static final String DB_USERNAME = "root";
    private static final String DB_PASSWORD = "";

    private static final Map<String, List<String>> BRAND_MODELS = Map.ofEntries(
            Map.entry("Eunos", List.of("Roadster")),
            Map.entry("Fairlady", List.of("Z")),
            Map.entry("Honda", List.of("City", "Civic", "CR-X", "Integra", "NSX", "Prelude")),
            Map.entry("Isuzu", List.of("Bighorn")),
            Map.entry("Lancer", List.of("Evolution")),
            Map.entry("Land", List.of("Cruiser")),
            Map.entry("Lexus", List.of("450HL", "LFA")),
            Map.entry("Mazda", List.of("Autozam", "AZ-1", "Cosmo", "Familia", "Roadster", "RX", "RX-7")),
            Map.entry("Mitsubishi", List.of("Bravo", "Delica", "GTO", "Lancer")),
            Map.entry("Nissan", List.of("180SX", "Bluebird", "Cefiro", "Fairlady", "Figaro", "GTR", "Pao", "President", "Pulsar", "S15", "Silvia", "Skyline", "Stagea")),
            Map.entry("Pulsar", List.of("GTi-RB")),
            Map.entry("SILEIGHTY", List.of("for")),
            Map.entry("Skyline", List.of("GTR", "Hakosuka", "R33")),
            Map.entry("Subaru", List.of("Forester", "Impreza", "Legacy", "Sambar")),
            Map.entry("Suzuki", List.of("Alto", "Jimny")),
            Map.entry("Targa", List.of("Top")),
            Map.entry("Toyota", List.of("Aristo", "C-HR", "Celica", "Chaser", "Mark", "MR2", "Soarer", "Starlet", "Supra"))
    );

    @Override
    public String getBotUsername() {
        return "JDMDealsBot";
    }

    @Override
    public String getBotToken() {
        return "7770390288:AAH9H_oPoqIb-npkpd76naiXsj4DsKi8Qq0";
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
        } else if (update.hasMessage() && update.getMessage().isCommand()) {
            String command = update.getMessage().getText().split(" ")[0];

            switch (command) {
                case "/caroffers":
                    handleCarOffersCommand(update);
                    break;
                case "/partoffers":
                    handlePartOffersCommand(update);
                    break;
                case "/favorites":
                    handleFavoritesCommand(update);
                    break;
                case "/help":
                    handleHelpCommand(update);
                    break;
                default:
                    sendTextMessage(update.getMessage().getChatId(), "Comando non riconosciuto.");
            }
        } else {
            processNonCommandUpdate(update);
        }
    }

    // Comando /help
    private void handleHelpCommand(Update update) {
        Long chatId = update.getMessage().getChatId();

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            StringBuilder helpMessage = new StringBuilder("Comandi disponibili:\n");
            helpMessage.append("/caroffers <brand> <modello> - Mostra fino a 3 offerte per il modello dell'auto specificato\n");
            helpMessage.append("/partoffers <categoria> - Mostra fino a 3 offerte per la categoria di ricambi specificata\n");
            helpMessage.append("/favorites - Mostra le tue offerte salvate\n");
            helpMessage.append("/help - Visualizza questa guida\n\n");

            helpMessage.append("ℹ️ Mostriamo solo offerte attive aggiornate dai nostri scraper.\n\n");


            helpMessage.append("Brand e modelli disponibili:\n");
            for (Map.Entry<String, List<String>> entry : BRAND_MODELS.entrySet()) {
                String brand = entry.getKey();
                String models = String.join(", ", entry.getValue());
                helpMessage.append("- ").append(brand).append(": ").append(models).append("\n");
            }


            helpMessage.append("\nCategorie di ricambi disponibili:\n");
            String categoryQuery = "SELECT category FROM products GROUP BY category ORDER BY category";
            try (PreparedStatement categoryStatement = connection.prepareStatement(categoryQuery);
                 ResultSet categoryResultSet = categoryStatement.executeQuery()) {
                while (categoryResultSet.next()) {
                    String category = categoryResultSet.getString("category");
                    helpMessage.append("- ").append(category).append("\n");
                }
            }

            sendTextMessage(chatId, helpMessage.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            sendTextMessage(chatId, "❌ Errore nel recupero delle informazioni.");
        }
    }

    // Comando /caroffers
    private void handleCarOffersCommand(Update update) {
        String[] args = update.getMessage().getText().split(" ");
        if (args.length != 3) {
            sendTextMessage(update.getMessage().getChatId(), "Formato comando errato. Usa: /caroffers <brand> <modello>");
            return;
        }

        String brand = args[1];
        String model = args[2];

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String query = "SELECT id, name, image_url, details_url FROM cars WHERE name LIKE ? AND is_active = TRUE ORDER BY RAND() LIMIT 3";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, brand + " " + model + "%");

                try (ResultSet resultSet = statement.executeQuery()) {
                    List<String[]> offers = new ArrayList<>();

                    while (resultSet.next()) {
                        int offerId = resultSet.getInt("id");
                        String name = resultSet.getString("name");
                        String imageUrl = resultSet.getString("image_url");
                        String detailsUrl = resultSet.getString("details_url");

                        offers.add(new String[]{String.valueOf(offerId), name, imageUrl, detailsUrl});
                    }

                    if (offers.isEmpty()) {
                        sendTextMessage(update.getMessage().getChatId(), "❌ Nessuna offerta trovata per: " + brand + " " + model);
                        return;
                    }

                    for (String[] offer : offers) {
                        int offerId = Integer.parseInt(offer[0]);
                        String name = offer[1];
                        String imageUrl = offer[2];
                        String detailsUrl = offer[3];

                        sendOfferWithFavoriteButton(update.getMessage().getChatId(), imageUrl, name, detailsUrl, offerId, "car");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendTextMessage(update.getMessage().getChatId(), "❌ Errore nel database.");
        }
    }

    // Comando /partoffers
    private void handlePartOffersCommand(Update update) {
        String[] args = update.getMessage().getText().split(" ");
        if (args.length != 2) {
            sendTextMessage(update.getMessage().getChatId(), "Formato comando errato. Usa: /partoffers <categoria>");
            return;
        }

        String category = args[1];

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            String query = "SELECT id, name, price, image_url, detail_page_url, subcategory_url FROM products WHERE category = ? AND is_active = TRUE ORDER BY RAND() LIMIT 3";
            try (PreparedStatement statement = connection.prepareStatement(query)) {
                statement.setString(1, category);

                try (ResultSet resultSet = statement.executeQuery()) {
                    if (!resultSet.isBeforeFirst()) {
                        sendTextMessage(update.getMessage().getChatId(), "Nessuna offerta trovata per la categoria: " + category);
                        return;
                    }

                    while (resultSet.next()) {
                        int offerId = resultSet.getInt("id");
                        String name = resultSet.getString("name");
                        String price = resultSet.getString("price");
                        String imageUrl = resultSet.getString("image_url");
                        String detailsUrl = resultSet.getString("detail_page_url");
                        String subcategoryLink = resultSet.getString("subcategory_url");

                        // Markup inline con pulsanti
                        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

                        InlineKeyboardButton favoriteButton = new InlineKeyboardButton();
                        favoriteButton.setText("⭐ Aggiungi ai preferiti");
                        favoriteButton.setCallbackData("favorite:" + offerId + ":product");

                        InlineKeyboardButton similarPartsButton = new InlineKeyboardButton();
                        similarPartsButton.setText("🔗 Ricambi simili");
                        similarPartsButton.setUrl(subcategoryLink);

                        List<InlineKeyboardButton> buttons = new ArrayList<>();
                        buttons.add(favoriteButton);
                        buttons.add(similarPartsButton);

                        rows.add(buttons);
                        markup.setKeyboard(rows);

                        sendPhotoWithInlineKeyboard(
                                update.getMessage().getChatId(),
                                imageUrl,
                                "*" + name + "*\n💰 Prezzo: " + price + "\n\n🔗 [Dettagli offerta](" + detailsUrl + ")",
                                markup
                        );
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendTextMessage(update.getMessage().getChatId(), "Errore nel database.");
        }
    }

    // Comando /favorites
    private void handleFavoritesCommand(Update update) {
        Long userId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();

        String query = "SELECT f.offer_id, f.offer_type, " +
                "COALESCE(c.name, p.name) AS name, " +
                "COALESCE(c.image_url, p.image_url) AS image_url, " +
                "COALESCE(c.details_url, p.detail_page_url) AS details_url, " +
                "COALESCE(c.is_active, p.is_active) AS is_active " +
                "FROM favorites f " +
                "LEFT JOIN cars c ON f.offer_id = c.id AND f.offer_type = 'car' " +
                "LEFT JOIN products p ON f.offer_id = p.id AND f.offer_type = 'product' " +
                "WHERE f.user_id = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.isBeforeFirst()) {
                    sendTextMessage(chatId, "Non hai ancora salvato preferiti!");
                    return;
                }

                while (resultSet.next()) {
                    int offerId = resultSet.getInt("offer_id");
                    String offerType = resultSet.getString("offer_type");
                    String name = resultSet.getString("name");
                    String imageUrl = resultSet.getString("image_url");
                    String detailsUrl = resultSet.getString("details_url");
                    boolean isActive = resultSet.getBoolean("is_active");

                    if (!isActive) {
                        name += " (⚠️ Offerta non più attiva)";
                    }

                    InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
                    List<List<InlineKeyboardButton>> rows = new ArrayList<>();
                    List<InlineKeyboardButton> buttons = new ArrayList<>();

                    InlineKeyboardButton removeButton = new InlineKeyboardButton();
                    removeButton.setText("❌ Rimuovi dai preferiti");
                    removeButton.setCallbackData("remove_favorite:" + offerId + ":" + offerType);
                    buttons.add(removeButton);

                    rows.add(buttons);
                    markup.setKeyboard(rows);

                    sendPhotoWithInlineKeyboard(chatId, imageUrl, "⭐ *" + name + "*\n\n🔗 [Dettagli offerta](" + detailsUrl + ")", markup);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendTextMessage(chatId, "Errore nel recupero dei preferiti.");
        }
    }

    private void handleCallbackQuery(CallbackQuery callbackQuery) {
        String data = callbackQuery.getData();
        Long userId = callbackQuery.getFrom().getId();
        Long chatId = callbackQuery.getMessage().getChatId();

        if (data.startsWith("favorite:")) {
            String[] parts = data.split(":");
            int offerId = Integer.parseInt(parts[1]);
            String offerType = parts[2];

            boolean added = addOfferToFavorites(userId, offerId, offerType);
            if (added) {
                sendTextMessage(chatId, "⭐ Offerta aggiunta ai preferiti!");
            } else {
                sendTextMessage(chatId, "⚠️ Questa offerta è già nei tuoi preferiti.");
            }
        } else if (data.startsWith("remove_favorite:")) {
            String[] parts = data.split(":");
            int offerId = Integer.parseInt(parts[1]);
            String offerType = parts[2];

            removeOfferFromFavorites(userId, offerId, offerType, chatId);
        }
    }

    private void removeOfferFromFavorites(Long userId, int offerId, String offerType, Long chatId) {
        String query = "DELETE FROM favorites WHERE user_id = ? AND offer_id = ? AND offer_type = ?";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD);
             PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setLong(1, userId);
            statement.setInt(2, offerId);
            statement.setString(3, offerType);

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected > 0) {
                sendTextMessage(chatId, "❌ Offerta rimossa dai preferiti!");
            } else {
                sendTextMessage(chatId, "Non ho trovato questa offerta nei tuoi preferiti.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            sendTextMessage(chatId, "Errore nella rimozione dai preferiti.");
        }
    }

    private boolean addOfferToFavorites(Long userId, int offerId, String offerType) {
        String checkQuery = "SELECT COUNT(*) FROM favorites WHERE user_id = ? AND offer_id = ? AND offer_type = ?";
        String insertQuery = "INSERT INTO favorites (user_id, offer_id, offer_type) VALUES (?, ?, ?)";

        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USERNAME, DB_PASSWORD)) {
            try (PreparedStatement checkStatement = connection.prepareStatement(checkQuery)) {
                checkStatement.setLong(1, userId);
                checkStatement.setInt(2, offerId);
                checkStatement.setString(3, offerType);

                try (ResultSet resultSet = checkStatement.executeQuery()) {
                    if (resultSet.next() && resultSet.getInt(1) > 0) {
                        return false;
                    }
                }
            }

            try (PreparedStatement insertStatement = connection.prepareStatement(insertQuery)) {
                insertStatement.setLong(1, userId);
                insertStatement.setInt(2, offerId);
                insertStatement.setString(3, offerType);
                insertStatement.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    private void sendOfferWithFavoriteButton(Long chatId, String imageUrl, String caption, String detailsUrl, int offerId, String offerType) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> buttons = new ArrayList<>();

        InlineKeyboardButton favoriteButton = new InlineKeyboardButton();
        favoriteButton.setText("⭐ Aggiungi ai preferiti");
        favoriteButton.setCallbackData("favorite:" + offerId + ":" + offerType);
        buttons.add(favoriteButton);

        rows.add(buttons);
        markup.setKeyboard(rows);

        sendPhotoWithInlineKeyboard(chatId, imageUrl, caption + "\n\n🔗 [Dettagli offerta](" + detailsUrl + ")", markup);
    }

    private void sendPhotoWithInlineKeyboard(Long chatId, String photoUrl, String caption, InlineKeyboardMarkup markup) {
        SendPhoto message = new SendPhoto();
        message.setChatId(chatId);
        message.setPhoto(new InputFile(photoUrl));
        message.setCaption(caption);
        message.setReplyMarkup(markup);
        message.setParseMode("Markdown");

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendTextMessage(Long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void processNonCommandUpdate(Update update) {
        sendTextMessage(update.getMessage().getChatId(), "Non ho capito il tuo messaggio.");
    }
}

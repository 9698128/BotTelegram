import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) {
        try {
            // Avvia il bot
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new JDMBot());
            System.out.println("Bot avviato con successo!");

            // Avvia il gestore degli scraper
            ScraperManager scraperManager = new ScraperManager();
            scraperManager.startScrapers();
            System.out.println("Scraper automatici pianificati ogni 12 ore!");

            // hook
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                System.out.println("Chiusura in corso...");
                scraperManager.stopScrapers();
            }));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

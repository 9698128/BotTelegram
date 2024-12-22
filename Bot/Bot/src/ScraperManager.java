import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ScraperManager {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void startScrapers() {
        // exe ogni 12 ore
        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("Esecuzione degli scraper avviata...");
                // Esegui il primo scraper
                JDMExpoScraper.main(new String[]{});
                // Esegui il secondo scraper
                NengunScraper.main(new String[]{});
                System.out.println("Scraping completato con successo!");
            } catch (Exception e) {
                System.err.println("Errore durante l'esecuzione degli scraper: " + e.getMessage());
                e.printStackTrace();
            }
        }, 0, 12, TimeUnit.HOURS);
    }

    public void stopScrapers() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
        }
    }
}

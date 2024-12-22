import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.*;
import java.util.List;

public class NengunScraper {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/jdmbot_database";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";

    public static void main(String[] args) {
        List<Category> categories = List.of(
                new Category("Electronics", List.of(
                        "https://www.nengun.com/electronics-standalone-ecus/",
                        "https://www.nengun.com/electronics-piggyback-ecus/",
                        "https://www.nengun.com/electronics-knock-monitors/",
                        "https://www.nengun.com/electronics-ecu-harness-options/",
                        "https://www.nengun.com/electronics-electronic-gauges/",
                        "https://www.nengun.com/electronics-gauge-controllers/",
                        "https://www.nengun.com/electronics-combination-meters/",
                        "https://www.nengun.com/electronics-mechanical-gauges/",
                        "https://www.nengun.com/electronics-shift-lights/",
                        "https://www.nengun.com/electronics-gauge-fittings/",
                        "https://www.nengun.com/electronics-mounts-hoods-stands/",
                        "https://www.nengun.com/electronics-gauge-repair-parts/",
                        "https://www.nengun.com/electronics-performance-meters/",
                        "https://www.nengun.com/electronics-throttle-controllers/",
                        "https://www.nengun.com/electronics-damper-controllers/",
                        "https://www.nengun.com/electronics-torque-controllers/",
                        "https://www.nengun.com/electronics-start-buttons/",
                        "https://www.nengun.com/electronics-entertainment/",
                        "https://www.nengun.com/electronics-antennas/",
                        "https://www.nengun.com/electronics-other-electronics/"
                )),
                new Category("Engine", List.of(
                        "https://www.nengun.com/engine-stroker-kits/",
                        "https://www.nengun.com/engine-forged-pistons/",
                        "https://www.nengun.com/engine-piston-rings/",
                        "https://www.nengun.com/engine-connecting-rods/",
                        "https://www.nengun.com/engine-crankshafts/",
                        "https://www.nengun.com/engine-camshafts/",
                        "https://www.nengun.com/engine-valvetrain/",
                        "https://www.nengun.com/engine-belts-chains/",
                        "https://www.nengun.com/engine-pulleys/",
                        "https://www.nengun.com/engine-mounts-dampers/"
                )),
                new Category("Ignition", List.of(
                        "https://www.nengun.com/ignition-coil-packs/",
                        "https://www.nengun.com/ignition-plug-leads/",
                        "https://www.nengun.com/ignition-spark-plugs/",
                        "https://www.nengun.com/ignition-earthing-kits/",
                        "https://www.nengun.com/ignition-alternators/",
                        "https://www.nengun.com/ignition-starter-motors/",
                        "https://www.nengun.com/ignition-battery-parts/"
                )),
                new Category("Oil-Fuel", List.of(
                        "https://www.nengun.com/oil-fuel-oil-pumps/",
                        "https://www.nengun.com/oil-fuel-oil-pans-baffles/",
                        "https://www.nengun.com/oil-fuel-catch-tanks/",
                        "https://www.nengun.com/oil-fuel-oil-filter-relocation/",
                        "https://www.nengun.com/oil-fuel-oil-filters/",
                        "https://www.nengun.com/oil-fuel-engine-oil/",
                        "https://www.nengun.com/oil-fuel-oil-caps/",
                        "https://www.nengun.com/oil-fuel-oil-gauge-holders/",
                        "https://www.nengun.com/oil-fuel-oil-drain-bolts/",
                        "https://www.nengun.com/oil-fuel-injectors/",
                        "https://www.nengun.com/oil-fuel-fuel-pumps/",
                        "https://www.nengun.com/oil-fuel-fuel-rails/",
                        "https://www.nengun.com/oil-fuel-fuel-pressure-regulators/",
                        "https://www.nengun.com/oil-fuel-fuel-caps/",
                        "https://www.nengun.com/oil-fuel-fuel-option-parts/"
                )),
                new Category("Intake", List.of(
                        "https://www.nengun.com/intake-pod-filters/",
                        "https://www.nengun.com/intake-induction-boxes/",
                        "https://www.nengun.com/intake-panel-filters/",
                        "https://www.nengun.com/intake-replacement-filters/",
                        "https://www.nengun.com/intake-surge-tanks/",
                        "https://www.nengun.com/intake-throttle-systems/",
                        "https://www.nengun.com/intake-piping/",
                        "https://www.nengun.com/intake-air-flow-meters/",
                        "https://www.nengun.com/intake-intakes-ducts/",
                        "https://www.nengun.com/intake-ducting/"
                )),
                new Category("Forced-Induction", List.of(
                        "https://www.nengun.com/forced-induction-turbo-kits/",
                        "https://www.nengun.com/forced-induction-turbos/",
                        "https://www.nengun.com/forced-induction-actuators/",
                        "https://www.nengun.com/forced-induction-wastegates/",
                        "https://www.nengun.com/forced-induction-boost-controllers/",
                        "https://www.nengun.com/forced-induction-turbo-timers/",
                        "https://www.nengun.com/forced-induction-blow-off-valves/",
                        "https://www.nengun.com/forced-induction-bov-option-parts/",
                        "https://www.nengun.com/forced-induction-turbo-accessories/",
                        "https://www.nengun.com/forced-induction-supercharger-kits/",
                        "https://www.nengun.com/forced-induction-supercharger/",
                        "https://www.nengun.com/forced-induction-supercharger-repair-parts/"
                )),
                new Category("Cooling", List.of(
                        "https://www.nengun.com/cooling-intercooler-kits/",
                        "https://www.nengun.com/cooling-radiators/",
                        "https://www.nengun.com/cooling-oil-coolers/",
                        "https://www.nengun.com/cooling-transmission-lsd-coolers/",
                        "https://www.nengun.com/cooling-intercooler-piping/",
                        "https://www.nengun.com/cooling-radiator-hoses/",
                        "https://www.nengun.com/cooling-radiator-caps/",
                        "https://www.nengun.com/cooling-thermostats/",
                        "https://www.nengun.com/cooling-cooling-panels/",
                        "https://www.nengun.com/cooling-breather-tanks/",
                        "https://www.nengun.com/cooling-water-pumps/",
                        "https://www.nengun.com/cooling-fans-accessories/"
                )),
                new Category("Exhaust", List.of(
                        "https://www.nengun.com/exhaust-exhaust-systems/",
                        "https://www.nengun.com/exhaust-headers-manifolds/",
                        "https://www.nengun.com/exhaust-heat-shields/",
                        "https://www.nengun.com/exhaust-extension-dump-pipes/",
                        "https://www.nengun.com/exhaust-front-pipes/",
                        "https://www.nengun.com/exhaust-center-mid-pipes/",
                        "https://www.nengun.com/exhaust-catalytic-converters/",
                        "https://www.nengun.com/exhaust-straight-pipes/",
                        "https://www.nengun.com/exhaust-tail-finishers/",
                        "https://www.nengun.com/exhaust-silencers/",
                        "https://www.nengun.com/exhaust-hangers/",
                        "https://www.nengun.com/exhaust-flanges-gaskets/"
                )),
                new Category("Drivetrain", List.of(
                        "https://www.nengun.com/drivetrain-clutch-kits/",
                        "https://www.nengun.com/drivetrain-flywheels/",
                        "https://www.nengun.com/drivetrain-clutch-lines/",
                        "https://www.nengun.com/drivetrain-clutch-overhaul-repair/",
                        "https://www.nengun.com/drivetrain-lsd-upgrade-kits/",
                        "https://www.nengun.com/drivetrain-lsd-covers/",
                        "https://www.nengun.com/drivetrain-lsd-repair-parts/",
                        "https://www.nengun.com/drivetrain-lsd-gear-oil/",
                        "https://www.nengun.com/drivetrain-axles-driveshafts/",
                        "https://www.nengun.com/drivetrain-shift-kits/",
                        "https://www.nengun.com/drivetrain-transmissions/",
                        "https://www.nengun.com/drivetrain-gear-sets/",
                        "https://www.nengun.com/drivetrain-master-slave-cylinders/",
                        "https://www.nengun.com/drivetrain-bearings-sleeves/",
                        "https://www.nengun.com/drivetrain-bushes-mounts-collars/",
                        "https://www.nengun.com/drivetrain-other/"
                )),
                new Category("Handling", List.of(
                        "https://www.nengun.com/handling-suspension-kits/",
                        "https://www.nengun.com/handling-springs/",
                        "https://www.nengun.com/handling-upper-mounts/",
                        "https://www.nengun.com/handling-strut-braces/",
                        "https://www.nengun.com/handling-stabilizers/",
                        "https://www.nengun.com/handling-arms/",
                        "https://www.nengun.com/handling-camber/",
                        "https://www.nengun.com/handling-tie-rods-and-ends/",
                        "https://www.nengun.com/handling-roll-center-adjusters/",
                        "https://www.nengun.com/handling-chassis-bracing/",
                        "https://www.nengun.com/handling-rods/",
                        "https://www.nengun.com/handling-links/",
                        "https://www.nengun.com/handling-steering/",
                        "https://www.nengun.com/handling-hicas/",
                        "https://www.nengun.com/handling-rear-member/",
                        "https://www.nengun.com/handling-bushes-collars/",
                        "https://www.nengun.com/handling-competition-tire/",
                        "https://www.nengun.com/handling-street-tires/"
                )),
                new Category("Brakes", List.of(
                        "https://www.nengun.com/brakes-upgrade-kits/",
                        "https://www.nengun.com/brakes-calipers/",
                        "https://www.nengun.com/brakes-disc-rotors-drums/",
                        "https://www.nengun.com/brakes-pads-shoes/",
                        "https://www.nengun.com/brakes-brake-lines/",
                        "https://www.nengun.com/brakes-brake-cylinder-stoppers/",
                        "https://www.nengun.com/brakes-repair-parts/",
                        "https://www.nengun.com/brakes-other/"
                )),
                new Category("Interior", List.of(
                        "https://www.nengun.com/interior-fixed-seats/",
                        "https://www.nengun.com/interior-reclinable-seats/",
                        "https://www.nengun.com/interior-seat-rails/",
                        "https://www.nengun.com/interior-seat-covers/",
                        "https://www.nengun.com/interior-seat-accessories/",
                        "https://www.nengun.com/interior-steering-wheels/",
                        "https://www.nengun.com/interior-horn-buttons/",
                        "https://www.nengun.com/interior-boss-kits-quick-release/",
                        "https://www.nengun.com/interior-shift-knobs/",
                        "https://www.nengun.com/interior-hand-brakes-buttons/",
                        "https://www.nengun.com/interior-dashboards-panels/",
                        "https://www.nengun.com/interior-floor-mats/",
                        "https://www.nengun.com/interior-harnesses/",
                        "https://www.nengun.com/interior-kick-plates/",
                        "https://www.nengun.com/interior-mirrors/",
                        "https://www.nengun.com/interior-pedals/",
                        "https://www.nengun.com/interior-roll-cages/",
                        "https://www.nengun.com/interior-trim/",
                        "https://www.nengun.com/interior-lights/"
                )),
                new Category("Exterior", List.of(
                        "https://www.nengun.com/exterior-aero-kits/",
                        "https://www.nengun.com/exterior-front-bumpers-grilles/",
                        "https://www.nengun.com/exterior-front-lips-spoilers/",
                        "https://www.nengun.com/exterior-fenders/",
                        "https://www.nengun.com/exterior-canards/",
                        "https://www.nengun.com/exterior-bonnets/",
                        "https://www.nengun.com/exterior-bonnet-pins-dampers/",
                        "https://www.nengun.com/exterior-side-mirrors/",
                        "https://www.nengun.com/exterior-doors/",
                        "https://www.nengun.com/exterior-rear-bumpers/",
                        "https://www.nengun.com/exterior-side-pillars/",
                        "https://www.nengun.com/exterior-trunks-tails/",
                        "https://www.nengun.com/exterior-roofs-racks-hard-tops/",
                        "https://www.nengun.com/exterior-rear-bumpers/",
                        "https://www.nengun.com/exterior-underpanels-diffusers/",
                        "https://www.nengun.com/exterior-trunks-tails/",
                        "https://www.nengun.com/exterior-rear-wings-spoilers/",
                        "https://www.nengun.com/exterior-tow-hooks-straps/",
                        "https://www.nengun.com/exterior-license-plates/",
                        "https://www.nengun.com/exterior-emblems/"
                )),
                new Category("Lights", List.of(
                        "https://www.nengun.com/lights-head-lights/",
                        "https://www.nengun.com/lights-bulbs/",
                        "https://www.nengun.com/lights-hid/",
                        "https://www.nengun.com/lights-fog-lamps/",
                        "https://www.nengun.com/lights-indicators/",
                        "https://www.nengun.com/lights-led/",
                        "https://www.nengun.com/lights-tail-lights/"
                ))
        );

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("Connessione al database riuscita!");

            // Disattiva tutte le offerte attive nella tabella `products`
            deactivateExistingProducts(conn);


            for (Category category : categories) {
                for (String subCategoryUrl : category.getSubCategoryUrls()) {
                    try {
                        System.out.println("Processando URL: " + subCategoryUrl);
                        processSubCategory(conn, category.getName(), subCategoryUrl);
                    } catch (Exception e) {
                        System.err.println("Errore con la sottocategoria: " + subCategoryUrl);
                        e.printStackTrace();
                    }
                }
            }

            System.out.println("Scraping completato con successo!");
        } catch (SQLException e) {
            System.err.println("Errore di connessione al database.");
            e.printStackTrace();
        }
    }

    /**
     * Disattiva tutte le offerte attive impostando `is_active = FALSE`.
     */
    private static void deactivateExistingProducts(Connection conn) throws SQLException {
        String deactivateQuery = "UPDATE products SET is_active = FALSE WHERE is_active = TRUE";
        try (Statement stmt = conn.createStatement()) {
            int rowsAffected = stmt.executeUpdate(deactivateQuery);
            System.out.println("Prodotti disattivati: " + rowsAffected);
        }
    }

    /**
     * Processa una sottocategoria e inserisce o aggiorna i prodotti.
     */
    private static void processSubCategory(Connection conn, String categoryName, String subCategoryUrl) throws IOException, SQLException {
        Document doc = Jsoup.connect(subCategoryUrl).get();
        Elements products = doc.select("div.product");

        if (products.isEmpty()) {
            System.out.println("Nessun prodotto trovato per la URL: " + subCategoryUrl);
            return;
        }

        String subCategoryName = extractSubCategoryName(subCategoryUrl);

        for (Element product : products) {
            String name = product.select("div.info h2 a").attr("title");
            String imageUrl = product.select("div.image img").attr("src");
            String price = product.select("div.price").text();
            String detailPageUrl = product.select("div.image a").attr("href");

            upsertProduct(conn, name, imageUrl, price, detailPageUrl, categoryName, subCategoryName, subCategoryUrl);
        }
    }

    /**
     * Inserisce o aggiorna un prodotto nel database.
     */
    private static void upsertProduct(Connection conn, String name, String imageUrl, String price, String detailPageUrl, String categoryName, String subCategoryName, String subCategoryUrl) throws SQLException {
        String upsertQuery = "INSERT INTO products (name, image_url, price, detail_page_url, category, subcategory, subcategory_url, is_active) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, TRUE) " +
                "ON DUPLICATE KEY UPDATE " +
                "name = VALUES(name), image_url = VALUES(image_url), price = VALUES(price), " +
                "detail_page_url = VALUES(detail_page_url), category = VALUES(category), " +
                "subcategory = VALUES(subcategory), subcategory_url = VALUES(subcategory_url), is_active = TRUE";

        try (PreparedStatement stmt = conn.prepareStatement(upsertQuery)) {
            stmt.setString(1, name);
            stmt.setString(2, imageUrl);
            stmt.setString(3, price);
            stmt.setString(4, detailPageUrl);
            stmt.setString(5, categoryName);
            stmt.setString(6, subCategoryName);
            stmt.setString(7, subCategoryUrl);

            stmt.executeUpdate();
            System.out.println("Prodotto inserito/aggiornato: " + name);
        }
    }

    /**
     * Estrae il nome della sottocategoria dall'URL.
     */
    private static String extractSubCategoryName(String subCategoryUrl) {
        return subCategoryUrl.substring(subCategoryUrl.lastIndexOf('/') + 1).replace("-", " ");
    }

    /**
     * Classe per rappresentare una categoria e le sue sottocategorie.
     */
    static class Category {
        private final String name;
        private final List<String> subCategoryUrls;

        public Category(String name, List<String> subCategoryUrls) {
            this.name = name;
            this.subCategoryUrls = subCategoryUrls;
        }

        public String getName() {
            return name;
        }

        public List<String> getSubCategoryUrls() {
            return subCategoryUrls;
        }
    }
}

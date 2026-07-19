package fr.xefreh.todoapp.backend;

import fr.xefreh.todoapp.backend.config.JpaConfig;
import io.javalin.Javalin;

/**
 * Point d'entrée du serveur REST Javalin.
 *
 * Pour l'instant seules l'unité de persistance JPA (Hibernate + H2) et une sonde
 * {@code GET /api/health} sont en place. Les routes d'authentification et de CRUD
 * notes arrivent dans les commits suivants.
 *
 * Démarrage : {@code ./gradlew :backend:run} — écoute sur le port 7000 (toutes interfaces).
 * Depuis l'émulateur Android : {@code http://10.0.2.2:7000}.
 */
public final class Main {

    /** Port d'écoute du serveur. {@code 10.0.2.2:7000} depuis l'émulateur Android. */
    public static final int PORT = 7000;

    private Main() {
    }

    public static void main(String[] args) {
        // Initialise l'EntityManagerFactory au démarrage (crée/maj le schéma H2).
        JpaConfig.entityManagerFactory();

        Javalin.create(config -> {
            // CORS permissif en développement (tests curl/browser depuis le host).
            config.bundledPlugins.enableCors(cors -> cors.addRule(rule -> rule.anyHost()));

            // Sonde de connectivité (utile notamment pour vérifier 10.0.2.2 depuis l'émulateur).
            config.routes.get("/api/health", ctx -> ctx.json(new HealthResponse("ok")));

            // Gestion d'erreurs uniforme : toute exception non gérée -> 500 JSON.
            config.routes.exception(Exception.class, (e, ctx) -> {
                ctx.status(500);
                ctx.json(new ErrorResponse("INTERNAL_ERROR", e.getMessage()));
            });
        }).start(PORT);

        Runtime.getRuntime().addShutdownHook(new Thread(JpaConfig::shutdown));

        System.out.println("TodoApp backend démarré sur http://0.0.0.0:" + PORT);
    }

    /** Corps de réponse de la sonde de santé. */
    public record HealthResponse(String status) {
    }

    /** Corps d'erreur JSON standard. */
    public record ErrorResponse(String error, String message) {
    }
}

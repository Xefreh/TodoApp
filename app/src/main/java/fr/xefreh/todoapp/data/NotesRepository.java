package fr.xefreh.todoapp.data;

import fr.xefreh.todoapp.Note;
import java.util.List;

/**
 * Accès unifié aux notes : source de vérité = serveur (via {@link TodoApi}), cache local
 * (via Room). Interface isolée de {@link NotesRepositoryImpl} afin d'être testée avec des
 * mocks Mockito ({@code TodoApi} + {@code NoteDao} sont eux-mêmes des interfaces).
 *
 * <p>Les méthodes sont <b>bloquantes</b> et doivent être appelées hors du thread principal
 * (par exemple via un {@code ExecutorService}). Elles lèvent une {@link ApiException} en
 * cas d'échec réseau ou d'erreur HTTP.</p>
 */
public interface NotesRepository {

    /**
     * Recharge toutes les notes depuis le serveur et remplace le cache local.
     * @return la liste des notes (jamais null).
     */
    List<Note> fetchAll();

    /**
     * Crée une note sur le serveur puis l'insère dans le cache local.
     * @return la note créée (avec id et createdAt serveur).
     */
    Note create(String title, String body, String imageUri);

    /**
     * Met à jour une note sur le serveur puis dans le cache local.
     */
    void update(Note note);

    /**
     * Supprime une note sur le serveur puis du cache local.
     */
    void delete(long serverId);
}

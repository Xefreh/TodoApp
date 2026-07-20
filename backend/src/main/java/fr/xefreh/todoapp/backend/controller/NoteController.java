package fr.xefreh.todoapp.backend.controller;

import fr.xefreh.todoapp.backend.dto.NoteDto;
import fr.xefreh.todoapp.backend.security.AuthFilter;
import fr.xefreh.todoapp.backend.service.NoteNotFoundException;
import fr.xefreh.todoapp.backend.service.NoteService;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import java.util.List;
import org.jetbrains.annotations.NotNull;

/**
 * Note CRUD endpoints. All assume an authenticated owner (the {@link AuthFilter}
 * has set {@link AuthFilter#USER_ID_KEY} in the context).
 *
 * <ul>
 *   <li>{@link #ListNotes}   {@code GET    /api/notes}</li>
 *   <li>{@link #CreateNote}  {@code POST   /api/notes}</li>
 *   <li>{@link #GetNote}     {@code GET    /api/notes/{id}}</li>
 *   <li>{@link #UpdateNote}  {@code PUT    /api/notes/{id}}</li>
 *   <li>{@link #DeleteNote}  {@code DELETE /api/notes/{id}}</li>
 * </ul>
 */
public final class NoteController {

    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    private static long ownerId(Context ctx) {
        Long id = ctx.attribute(AuthFilter.USER_ID_KEY);
        if (id == null) {
            // Should not happen: the before filter already returned 401.
            throw new IllegalStateException("No authenticated user on a protected route");
        }
        return id;
    }

    /** {@code GET /api/notes} — list of the owner's notes. */
    public final Handler ListNotes = new Handler() {
        @Override
        public void handle(@NotNull Context ctx) {
            List<NoteDto> notes = noteService.listForOwner(ownerId(ctx));
            ctx.json(notes);
        }
    };

    /** {@code POST /api/notes} — creates a note. */
    public final Handler CreateNote = new Handler() {
        @Override
        public void handle(@NotNull Context ctx) {
            NoteDto input = ctx.bodyAsClass(NoteDto.class);
            if (input == null || input.title == null || input.title.isBlank()) {
                ctx.status(400).json(new ErrorBody("BAD_REQUEST", "title is required"));
                return;
            }
            NoteDto created = noteService.create(ownerId(ctx), input);
            ctx.status(201).json(created);
        }
    };

    /** {@code GET /api/notes/{id}} — one of the owner's notes. */
    public final Handler GetNote = new Handler() {
        @Override
        public void handle(@NotNull Context ctx) {
            long id = ctx.pathParamAsClass("id", Long.class).get();
            try {
                ctx.json(noteService.getForOwner(id, ownerId(ctx)));
            } catch (NoteNotFoundException e) {
                ctx.status(404).json(new ErrorBody("NOT_FOUND", e.getMessage()));
            }
        }
    };

    /** {@code PUT /api/notes/{id}} — updates one of the owner's notes. */
    public final Handler UpdateNote = new Handler() {
        @Override
        public void handle(@NotNull Context ctx) {
            long id = ctx.pathParamAsClass("id", Long.class).get();
            NoteDto input = ctx.bodyAsClass(NoteDto.class);
            if (input == null || input.title == null || input.title.isBlank()) {
                ctx.status(400).json(new ErrorBody("BAD_REQUEST", "title is required"));
                return;
            }
            try {
                ctx.json(noteService.update(id, ownerId(ctx), input));
            } catch (NoteNotFoundException e) {
                ctx.status(404).json(new ErrorBody("NOT_FOUND", e.getMessage()));
            }
        }
    };

    /** {@code DELETE /api/notes/{id}} — deletes one of the owner's notes. */
    public final Handler DeleteNote = new Handler() {
        @Override
        public void handle(@NotNull Context ctx) {
            long id = ctx.pathParamAsClass("id", Long.class).get();
            try {
                noteService.delete(id, ownerId(ctx));
                ctx.status(204);
            } catch (NoteNotFoundException e) {
                ctx.status(404).json(new ErrorBody("NOT_FOUND", e.getMessage()));
            }
        }
    };

    /** Standard JSON error body. */
    public record ErrorBody(String error, String message) {
    }
}

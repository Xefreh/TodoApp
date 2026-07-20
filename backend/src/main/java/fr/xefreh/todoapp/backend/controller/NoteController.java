package fr.xefreh.todoapp.backend.controller;

import fr.xefreh.todoapp.backend.dto.NoteDto;
import fr.xefreh.todoapp.backend.security.AuthFilter;
import fr.xefreh.todoapp.backend.service.NoteNotFoundException;
import fr.xefreh.todoapp.backend.service.NoteService;
import io.javalin.http.BadRequestResponse;
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
            NoteDto input = parseNote(ctx);
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
            NoteDto input = parseNote(ctx);
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

    /**
     * Parses and validates the note body. Throws {@link BadRequestResponse} (400) on
     * missing/malformed JSON or a blank title — {@code bodyAsClass} throws on an empty body,
     * so a plain null-check is never enough (it previously ended in a 500).
     *
     * <p>Note: catches {@link Exception} and not just {@link RuntimeException}: Javalin is
     * written in Kotlin, which does not enforce checked exceptions — Jackson's
     * {@code JsonProcessingException} (an {@code IOException}) escapes {@code bodyAsClass}
     * undeclared.</p>
     */
    private static NoteDto parseNote(Context ctx) {
        NoteDto input;
        try {
            input = ctx.bodyAsClass(NoteDto.class);
        } catch (Exception e) {
            throw new BadRequestResponse("Request body must be a JSON note object");
        }
        if (input == null || input.title == null || input.title.isBlank()) {
            throw new BadRequestResponse("title is required");
        }
        return input;
    }

    /** Standard JSON error body. */
    public record ErrorBody(String error, String message) {
    }
}

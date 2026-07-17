package fr.xefreh.todoapp;

import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import fr.xefreh.todoapp.ui.NoteItemView;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

	private final List<Note> notes;

	public NotesAdapter(List<Note> notes) {
		this.notes = notes;
	}

	public static class NoteViewHolder extends RecyclerView.ViewHolder {
		NoteItemView item;

		public NoteViewHolder(NoteItemView item) {
			super(item);
			this.item = item;
		}
	}

	@Override
	public NoteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		return new NoteViewHolder(new NoteItemView(parent.getContext()));
	}

	@Override
	public int getItemCount() {
		return notes.size();
	}

	@Override
	public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
		Note note = notes.get(position);
		holder.item.titleText.setText(note.getTitle());
		holder.item.bodyText.setText(note.getBody());

		String imageUri = note.getImageUri();
		if (imageUri != null) {
			holder.item.photoPreview.setVisibility(View.VISIBLE);
			Glide.with(holder.itemView)
					.load(Uri.parse(imageUri))
					.centerCrop()
					.into(holder.item.photoPreview);
		} else {
			Glide.with(holder.itemView).clear(holder.item.photoPreview);
			holder.item.photoPreview.setVisibility(View.GONE);
		}
	}
}

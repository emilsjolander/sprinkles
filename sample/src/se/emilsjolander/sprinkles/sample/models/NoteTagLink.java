package se.emilsjolander.sprinkles.sample.models;

import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.annotations.Table;

@Table("NoteTagLinks")
public class NoteTagLink extends Model {

	@Key
	@Column("note_id") private long noteId;

	@Key
	@Column("tag_id") private long tagId;

	public NoteTagLink() {
        // default constructor
	}
	
	public NoteTagLink(long noteId, long tagId) {
		this();
		this.noteId = noteId;
		this.tagId = tagId;
	}
	
	public void setNoteId(long noteId) {
		this.noteId = noteId;
	}

	public void setTagId(long tagId) {
		this.tagId = tagId;
	}
	
	public long getNoteId() {
		return noteId;
	}

	public long getTagId() {
		return tagId;
	}
	
}

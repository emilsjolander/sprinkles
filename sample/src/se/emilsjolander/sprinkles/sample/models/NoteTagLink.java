package se.emilsjolander.sprinkles.sample.models;

import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.CascadeDelete;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.ForeignKey;
import se.emilsjolander.sprinkles.annotations.PrimaryKey;
import se.emilsjolander.sprinkles.annotations.Table;

@Table("NoteTagLinks")
public class NoteTagLink extends Model {

	@PrimaryKey
	@CascadeDelete
	@ForeignKey("Notes(id)")
	@Column("note_id") private long noteId;

	@PrimaryKey
	@CascadeDelete
	@ForeignKey("Tags(id)")
	@Column("tag_id") private long tagId;
	
	public NoteTagLink() {
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

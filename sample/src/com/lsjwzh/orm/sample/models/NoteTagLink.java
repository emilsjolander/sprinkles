package com.lsjwzh.orm.sample.models;

import com.lsjwzh.orm.Model;
import com.lsjwzh.orm.annotations.Column;
import com.lsjwzh.orm.annotations.Key;
import com.lsjwzh.orm.annotations.Table;

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

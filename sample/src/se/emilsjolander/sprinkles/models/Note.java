package se.emilsjolander.sprinkles.models;

import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.AutoIncrementPrimaryKey;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Table;

@Table("Notes")
public class Note extends Model {
	
	@AutoIncrementPrimaryKey
	@Column("id") private long id;

	@Column("content") private String content;
	@Column("created_at") private long createdAt;
	@Column("updated_at") private long updatedAt;
	
	public long getId() {
		return id;
	}
	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public long getCreatedAt() {
		return createdAt;
	}
	
	public long getUpdatedAt() {
		return updatedAt;
	}
	
	@Override
	protected void beforeCreate() {
		super.beforeCreate();
		createdAt = System.currentTimeMillis();
	}
	
	@Override
	protected void beforeSave() {
		super.beforeSave();
		updatedAt = System.currentTimeMillis();
	}

}

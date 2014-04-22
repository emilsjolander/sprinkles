package se.emilsjolander.sprinkles.sample.models;

import java.util.Date;

import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.AutoIncrement;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.DynamicColumn;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.annotations.Table;

@Table("Notes")
public class Note extends Model {

	@AutoIncrement
    @Key @Column("id") private long id;
	@Column("content") private String content;
	@Column("created_at") private Date createdAt;
	@Column("updated_at") private Date updatedAt;
    @DynamicColumn("tag_count") private int tagCount;
	
	public long getId() {
		return id;
	}
	
	public String getContent() {
		return content;
	}
	
	public void setContent(String content) {
		this.content = content;
	}
	
	public Date getCreatedAt() {
		return createdAt;
	}
	
	public Date getUpdatedAt() {
		return updatedAt;
	}

    public int getTagCount() {
        return tagCount;
    }
	
	@Override
	protected void beforeCreate() {
		super.beforeCreate();
		createdAt = new Date();
	}
	
	@Override
	protected void beforeSave() {
		super.beforeSave();
		updatedAt = new Date();
	}

}

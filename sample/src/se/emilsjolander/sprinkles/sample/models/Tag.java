package se.emilsjolander.sprinkles.sample.models;

import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.AutoIncrement;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.annotations.Table;

@Table("Tags")
public class Tag extends Model {

    @AutoIncrement
    @Key @Column("id") private long id;
	@Column("name") private String name;
	
	public long getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public boolean isValid() {
		return name != null && !name.isEmpty();
	}
	
}

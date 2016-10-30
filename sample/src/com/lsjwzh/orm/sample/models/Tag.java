package com.lsjwzh.orm.sample.models;

import com.lsjwzh.orm.Model;
import com.lsjwzh.orm.annotations.AutoIncrement;
import com.lsjwzh.orm.annotations.Column;
import com.lsjwzh.orm.annotations.Key;
import com.lsjwzh.orm.annotations.Table;

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

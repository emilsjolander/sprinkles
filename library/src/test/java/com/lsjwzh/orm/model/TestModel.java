package com.lsjwzh.orm.model;



import java.util.Date;

import com.lsjwzh.orm.Model;
import com.lsjwzh.orm.annotations.AutoIncrement;
import com.lsjwzh.orm.annotations.Column;
import com.lsjwzh.orm.annotations.DynamicColumn;
import com.lsjwzh.orm.annotations.Key;
import com.lsjwzh.orm.annotations.Table;

@Table("Tests")
public class TestModel extends Model {

    @Key
    @AutoIncrement
    @Column("id") public long id;
    @Column("title") public String title;
    @Column("created_at") public Date createdAt;
    @Column("sn") public int sn;
    @DynamicColumn("count") public int count;

    public boolean valid = true;
    public boolean created;
    public boolean saved;
    public boolean deleted;

}
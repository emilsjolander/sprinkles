package se.emilsjolander.sprinkles.model;



import java.util.Date;

import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.AutoIncrement;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.DynamicColumn;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.annotations.Table;

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
package se.emilsjolander.sprinkles;


import java.util.Date;

import se.emilsjolander.sprinkles.annotations.AutoGen;
import se.emilsjolander.sprinkles.annotations.AutoIncrement;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.annotations.ManyToOne;
import se.emilsjolander.sprinkles.annotations.Table;

@Table
@AutoGen
public class Email extends Model {

    @Key
    @AutoIncrement
    public long id;

    public String address;

    @ManyToOne(manyColumn = "owner_id",oneColumn = "id")
    public Person owner;
}
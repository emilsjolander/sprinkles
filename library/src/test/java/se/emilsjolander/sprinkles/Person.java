package se.emilsjolander.sprinkles;


import java.util.Date;

import se.emilsjolander.sprinkles.annotations.AutoGen;
import se.emilsjolander.sprinkles.annotations.AutoIncrement;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.annotations.ManyToOne;
import se.emilsjolander.sprinkles.annotations.OneToMany;
import se.emilsjolander.sprinkles.annotations.Table;

@Table
@AutoGen
public class Person extends Model {

    @Key
    @AutoIncrement
    public long id;
    public String name;

    @OneToMany(manyColumn = "owner_id",oneColumn = "id",manyModelClass = Email.class)
    public ModelList<Email> emails;
}
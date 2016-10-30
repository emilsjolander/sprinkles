package se.emilsjolander.sprinkles.model;


import se.emilsjolander.sprinkles.LazyModel;
import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.ModelList;
import se.emilsjolander.sprinkles.Sprinkles;
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
    public long company_id;


    @OneToMany(manyColumn = "owner_id",oneColumn = "id")
    public ModelList<Email> emails;

    @ManyToOne(manyColumn = "company_id",oneColumn = "id")
    public LazyModel<Company> company;

    public Person(Sprinkles sprinkles) {
        super(sprinkles);
    }
}
package se.emilsjolander.sprinkles.model;


import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.LazyModelList;
import se.emilsjolander.sprinkles.Sprinkles;
import se.emilsjolander.sprinkles.annotations.AutoGen;
import se.emilsjolander.sprinkles.annotations.AutoIncrement;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.annotations.OneToMany;
import se.emilsjolander.sprinkles.annotations.Table;

@Table
@AutoGen
public class Company extends Model {

    @Key
    @AutoIncrement
    public long id;
    public String name;

    @OneToMany(manyColumn = "company_id",oneColumn = "id")
    public LazyModelList<Person> Staffs;

    public Company(Sprinkles sprinkles) {
        super(sprinkles);
    }
}
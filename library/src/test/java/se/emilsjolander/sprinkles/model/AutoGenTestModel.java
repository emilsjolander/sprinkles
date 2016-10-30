package se.emilsjolander.sprinkles.model;


import java.util.Date;

import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.Sprinkles;
import se.emilsjolander.sprinkles.annotations.AutoGen;
import se.emilsjolander.sprinkles.annotations.AutoIncrement;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.annotations.Table;

@Table
@AutoGen
public class AutoGenTestModel extends Model {

    @Key
    @AutoIncrement
    public long id;
    public String title;
    public Date createdAt;

    public boolean valid = true;
    public boolean created;
    public boolean saved;
    public boolean deleted;

    public AutoGenTestModel(Sprinkles sprinkles) {
        super(sprinkles);
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public void beforeCreate() {
//        createdAt = new Date();
        created = true;
    }

    @Override
    public void beforeSave() {
        saved = true;
    }

    @Override
    public void afterDelete() {
        deleted = true;
    }

}
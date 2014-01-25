package se.emilsjolander.sprinkles.models;

import se.emilsjolander.sprinkles.Model;
import se.emilsjolander.sprinkles.annotations.AutoIncrementPrimaryKey;
import se.emilsjolander.sprinkles.annotations.Column;

public class AbsTestModel extends Model {

    @AutoIncrementPrimaryKey
    @Column("id") private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

}

package se.emilsjolander.sprinkles;

import se.emilsjolander.sprinkles.annotations.AutoIncrementPrimaryKey;
import se.emilsjolander.sprinkles.annotations.Column;

public class TestModel extends Model {

    @AutoIncrementPrimaryKey
    @Column("id") private long id;

    public long getId() {
        return id;
    }

}

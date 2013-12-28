package se.emilsjolander.sprinkles;

import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Table;

@Table("Tests")
public class ImprovedTestModel extends TestModel {

    @Column("title") private String title;

    public String getTitle() {
        return title;
    }

}

package se.emilsjolander.sprinkles.models;

import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.Table;

@Table("Tests")
public class TestModel extends AbsTestModel {

    @Column("title") private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}

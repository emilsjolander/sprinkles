package se.emilsjolander.sprinkles.models;

import se.emilsjolander.sprinkles.annotations.Table;

@Table("CallbackTests")
public class CallbackTestModel extends TestModel {

    private boolean valid = true;
    public boolean created;
    public boolean saved;
    public boolean deleted;

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    @Override
    public boolean isValid() {
        return valid;
    }

    @Override
    public void beforeCreate() {
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

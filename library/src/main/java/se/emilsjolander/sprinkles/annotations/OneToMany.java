package se.emilsjolander.sprinkles.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import se.emilsjolander.sprinkles.Model;

/**
 * Specify that a One Model contains a Many Model list,their relationship depends on OneColumn and ManyColumn.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface OneToMany {
    /**
     * the column name that store the one Model id in Many Model
     * @return
     */
    String manyColumn();
    /**
     * the column name that store the one Model id in One Model
     * @return
     */
    String oneColumn();

    Class<? extends Model> manyModelClass();

}

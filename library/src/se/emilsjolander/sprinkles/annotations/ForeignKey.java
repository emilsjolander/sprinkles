package se.emilsjolander.sprinkles.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adds an FOREIGN KEY(column_name) REFERENCES foreign_table(column_name) modifier to the underlying database column.
 * column_name is taken from the @Column annotations parameter while
 * foreign_table(column_name) is taken from this annotations parameter.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ForeignKey {
    String value();
}

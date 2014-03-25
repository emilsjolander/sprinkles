package se.emilsjolander.sprinkles.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adds an UNIQUE table constraint for the group of unique database columns. To specify a conflict-clause,
 * include it as a {@link se.emilsjolander.sprinkles.annotations.ConflictClause} in the parentheses. For
 * example, {@code
 *
 * @UniqueCombo(ConflictClause.ROLLBACK) private String name;
 * }. If no {@code ConflictClause} is specified, the default behavior will be used.
 * @see <a href="http://www.sqlite.org/syntaxdiagrams.html#conflict-clause">http://www.sqlite.org/syntaxdiagrams.html#conflict-clause</a>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueCombo {
	ConflictClause value() default ConflictClause.ROLLBACK;
}

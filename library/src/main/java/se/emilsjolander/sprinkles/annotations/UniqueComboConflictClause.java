package se.emilsjolander.sprinkles.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adds an UNIQUE conflict cause modifier to the table. To specify a conflict-clause,
 * include it as a {@link ConflictClause} in the parentheses. For
 * example, {@code
 *
 * @UniqueComboConflictClause(ConflictClause.ROLLBACK) private String name;
 * }. If no {@code UniqueComboConflictClause} is specified, the default behavior will be used.
 * @see <a href="http://www.sqlite.org/syntaxdiagrams.html#conflict-clause">http://www.sqlite.org/syntaxdiagrams.html#conflict-clause</a>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface UniqueComboConflictClause {
	ConflictClause value() default ConflictClause.ROLLBACK;
}

package se.emilsjolander.sprinkles;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import se.emilsjolander.sprinkles.model.Email;
import se.emilsjolander.sprinkles.model.Person;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class RelationshipTest {

    private Sprinkles sprinkles;

    @Before
    public void initTables() {
        sprinkles = Sprinkles.init(Robolectric.application);
//        sprinkles.addMigration(AutoGenTestModel.MIGRATION);
        Person p = new Person(sprinkles);
        p.name="goodman";
        Email email1 = new Email(sprinkles);
        email1.address = "1@gmail.com";
        email1.owner = p;

        Email email2 = new Email(sprinkles);
        email2.address = "1@gmail.com";
        email2.owner = p;


        p.emails.add(email1);
        p.emails.add(email2);

        assertEquals(2,p.emails.size());

        p.save();
        p.emails.saveAll();
        sprinkles.dataResolver.resetRecordCache();
    }

    @Test
    public void constructor() {
        Person p = new Person(sprinkles);
        assertNotNull(p.emails);
    }

    @Test
    public void oneToMany() {

        OneQuery<Person> query = Query.one(sprinkles, Person.class,"SELECT * FROM "+DataResolver.getTableName(Person.class)+" where name='goodman'");
        Person pFromQuery = query.get();
        assertEquals(2,pFromQuery.emails.size());
    }

    @Test
    public void manyToOne() {
        Person p = new Person(sprinkles);
        Email email1 = new Email(sprinkles);
        email1.address = "1@gmail.com";
        email1.owner = p;

        Email email2 = new Email(sprinkles);
        email2.address = "1@gmail.com";
        email2.owner = p;


        p.emails.add(email1);
        p.emails.add(email2);

        assertEquals(2,p.emails.size());

        p.save();
        p.emails.saveAll();

        OneQuery<Email> query = Query.one(sprinkles, Email.class,"SELECT * FROM "+DataResolver.getTableName(Email.class)+" where id="+email1.id);
        Email eFromQuery = query.get();
        assertNotNull(eFromQuery.owner);
    }


}

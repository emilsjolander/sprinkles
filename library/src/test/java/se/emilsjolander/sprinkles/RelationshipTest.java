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

    @Before
    public void initTables() {
        Sprinkles.dropInstances();
        ModelInfo.clearCache();
        Sprinkles sprinkles = Sprinkles.init(Robolectric.application);
//        sprinkles.addMigration(AutoGenTestModel.MIGRATION);
    }

    @Test
    public void constructor() {
        Person p = new Person();
        assertNotNull(p.emails);
    }

    @Test
    public void oneToMany() {
        Person p = new Person();
        Email email1 = new Email();
        email1.address = "1@gmail.com";
        email1.owner = p;

        Email email2 = new Email();
        email2.address = "1@gmail.com";
        email2.owner = p;


        p.emails.add(email1);
        p.emails.add(email2);

        assertEquals(2,p.emails.size());

        p.save();
        p.emails.saveAll();

        OneQuery<Person> query = Query.one(Person.class,"SELECT * FROM "+Utils.getTableName(Person.class)+" where id="+p.id);
        Person pFromQuery = query.get();
        assertEquals(2,pFromQuery.emails.size());
    }

    @Test
    public void manyToOne() {
        Person p = new Person();
        Email email1 = new Email();
        email1.address = "1@gmail.com";
        email1.owner = p;

        Email email2 = new Email();
        email2.address = "1@gmail.com";
        email2.owner = p;


        p.emails.add(email1);
        p.emails.add(email2);

        assertEquals(2,p.emails.size());

        p.save();
        p.emails.saveAll();

        OneQuery<Email> query = Query.one(Email.class,"SELECT * FROM "+Utils.getTableName(Email.class)+" where id="+email1.id);
        Email eFromQuery = query.get();
        assertNotNull(eFromQuery.owner);
    }


}

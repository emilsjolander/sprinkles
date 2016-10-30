package com.lsjwzh.orm;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import com.lsjwzh.orm.model.Email;
import com.lsjwzh.orm.model.Person;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class RelationshipTest {

    private Sprinkles sprinkles;

    @Before
    public void initTables() {
        sprinkles = Sprinkles.init(Robolectric.application);
        Person p = new Person();
        p.name = "goodman";
        Email email1 = new Email();
        email1.address = "1@gmail.com";
        email1.owner = p;

        Email email2 = new Email();
        email2.address = "1@gmail.com";
        email2.owner = p;


        p.emails.add(email1);
        p.emails.add(email2);

        assertEquals(2, p.emails.size());

        sprinkles.save(p);
        sprinkles.saveAll(p.emails);

        sprinkles.dataResolver.resetRecordCache();
    }

    @Test
    public void constructor() {
        Person p = new Person();
        assertNotNull(p.emails);
    }

    @Test
    public void oneToMany() {
        OneQuery<Person> query = Query.one(sprinkles, Person.class, "SELECT * FROM " + DataResolver.getTableName(Person.class) + " where name='goodman'");
        Person pFromQuery = query.get();
        assertEquals(2, pFromQuery.emails.size());
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

        assertEquals(2, p.emails.size());

        sprinkles.save(p);
        sprinkles.saveAll(p.emails);

        OneQuery<Email> query = Query.one(sprinkles, Email.class, "SELECT * FROM " + DataResolver.getTableName(Email.class) + " where id=" + email1.id);
        Email eFromQuery = query.get();
        assertNotNull(eFromQuery.owner);
    }


}

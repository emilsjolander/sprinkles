package com.lsjwzh.orm;

import com.lsjwzh.orm.model.Company;
import com.lsjwzh.orm.model.Person;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class LazyLoadTest {

    private Sprinkles sprinkles;

    @Before
    public void initTables() {
        sprinkles = Sprinkles.init(Robolectric.application, "sprinkle.db", 1);
        Company company = new Company();
        company.name = "google";
        sprinkles.save(company);

        Person staff = new Person();
        staff.name = "goodman";
        staff.company_id = company.id;
        sprinkles.save(staff);
    }

    @Test
    public void lazyModel() {
        Person staff = new Query(sprinkles).findSingle(QueryBuilder.from(Person.class).where().equalTo("name", "goodman").end());
        Company company = new Query(sprinkles).findSingle(QueryBuilder.from(Company.class).where().equalTo("name", "google").end());
        assertNotNull(staff.company);
        assertEquals(company, staff.company.load());

    }

    @Test
    public void lazyModelList() {
        Person staff = new Query(sprinkles).findSingle(QueryBuilder.from(Person.class).where().equalTo("name", "goodman").end());
        Company company = new Query(sprinkles).findSingle(QueryBuilder.from(Company.class).where().equalTo("name", "google").end());
        assertNotNull(company.Staffs);
        assertEquals(1, company.Staffs.load().size());

    }


}

package se.emilsjolander.sprinkles;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import se.emilsjolander.sprinkles.model.Company;
import se.emilsjolander.sprinkles.model.Person;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class LazyLoadTest {

    private Sprinkles sprinkles;

    @Before
    public void initTables() {
        sprinkles = Sprinkles.init(Robolectric.application,"sprinkle.db",1);
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
        Person staff = Query.where(sprinkles, Person.class).equalTo("name","goodman").findSingle();
        Company company =  Query.where(sprinkles, Company.class).equalTo("name","google").findSingle();
        assertNotNull(staff.company);
        assertEquals(company,staff.company.load());

    }

    @Test
    public void lazyModelList() {
        Person staff = Query.where(sprinkles, Person.class).equalTo("name","goodman").findSingle();
        Company company =  Query.where(sprinkles, Company.class).equalTo("name","google").findSingle();
        assertNotNull(company.Staffs);
        assertEquals(1,company.Staffs.load().size());

    }


}

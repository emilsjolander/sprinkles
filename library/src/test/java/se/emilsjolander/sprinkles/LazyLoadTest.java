package se.emilsjolander.sprinkles;

import android.content.ContentValues;
import android.database.ContentObserver;
import android.os.Handler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import se.emilsjolander.sprinkles.model.Company;
import se.emilsjolander.sprinkles.model.Person;
import se.emilsjolander.sprinkles.model.TestModel;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class LazyLoadTest {

    @Before
    public void initTables() {
        Sprinkles.dropInstances();
        ModelInfo.clearCache();
        Sprinkles sprinkles = Sprinkles.init(Robolectric.application,"sprinkle.db",1);
//        sprinkles.addMigration(TestModel.MIGRATION);
    }

    @Test
    public void lazyModel() {
        Company company = new Company();
        company.name = "google";
        company.save();

        Person staff = new Person();
        staff.company_id = company.id;
        staff.save();

        assertNotNull(staff.company);
        assertEquals(company,staff.company.load());
        assertEquals(1,company.Staffs.load().size());

    }


}

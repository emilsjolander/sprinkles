package se.emilsjolander.sprinkles;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.*;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class TransactionTest {

    @Before
    public void initTables() {
        Sprinkles.dropInstances();
        Sprinkles sprinkles = Sprinkles.init(Robolectric.application);
        sprinkles.addMigration(TestModel.MIGRATION);
    }

    @Test
    public void commit() throws InterruptedException {
        Transaction t = new Transaction();
        TestModel m = new TestModel();
        m.title = "hej";
        m.save(t);
        t.setSuccessful(true);
        t.finish();
        assertTrue(m.exists());
    }

    @Test
    public void rollback() {
        Transaction t = new Transaction();
        TestModel m = new TestModel();
        m.title = "hej";
        m.save(t);
        t.setSuccessful(false);
        t.finish();
        assertFalse(m.exists());
    }

    @Test
    public void listenerCalledOnCommit() {
        Transaction t = new Transaction();
        final boolean[] called = new boolean[1];
        t.addOnTransactionCommittedListener(new Transaction.OnTransactionCommittedListener() {
            @Override
            public void onTransactionCommitted() {
                called[0] = true;
            }
        });
        TestModel m = new TestModel();
        m.title = "hej";
        m.save(t);
        t.setSuccessful(true);
        t.finish();
        assertTrue(called[0]);
    }

    @Test
    public void listenerNotCalledOnRollback() {
        Transaction t = new Transaction();
        final boolean[] called = new boolean[1];
        t.addOnTransactionCommittedListener(new Transaction.OnTransactionCommittedListener() {
            @Override
            public void onTransactionCommitted() {
                called[0] = true;
            }
        });
        TestModel m = new TestModel();
        m.title = "hej";
        m.save(t);
        t.setSuccessful(false);
        t.finish();
        assertFalse(called[0]);
    }

}

package se.emilsjolander.sprinkles;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import se.emilsjolander.sprinkles.model.TestModel;

import static junit.framework.Assert.*;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class TransactionTest {

    private Sprinkles sprinkles;

    @Before
    public void initTables() {
        sprinkles = Sprinkles.init(Robolectric.application);
    }

    @Test
    public void commit() throws InterruptedException {
        Transaction t = new Transaction(sprinkles);
        TestModel m = new TestModel();
        m.title = "hej";
        sprinkles.save(m, t);
        t.setSuccessful(true);
        t.finish();
        assertTrue(sprinkles.exists(m));
    }

    @Test
    public void rollback() {
        Transaction t = new Transaction(sprinkles);
        TestModel m = new TestModel();
        m.title = "hej";
        sprinkles.save(m, t);
        t.setSuccessful(false);
        t.finish();
        assertFalse(sprinkles.exists(m));
    }

    @Test
    public void listenerCalledOnCommit() {
        Transaction t = new Transaction(sprinkles);
        final boolean[] called = new boolean[1];
        t.addOnTransactionCommittedListener(new Transaction.OnTransactionCommittedListener() {
            @Override
            public void onTransactionCommitted() {
                called[0] = true;
            }

            @Override
            public void onTransactionRollback() {

            }
        });
        TestModel m = new TestModel();
        m.title = "hej";
        sprinkles.save(m, t);
        t.setSuccessful(true);
        t.finish();
        assertTrue(called[0]);
    }

    @Test
    public void listenerNotCalledOnRollback() {
        Transaction t = new Transaction(sprinkles);
        final boolean[] called = new boolean[1];
        t.addOnTransactionCommittedListener(new Transaction.OnTransactionCommittedListener() {
            @Override
            public void onTransactionCommitted() {
                called[0] = true;
            }

            @Override
            public void onTransactionRollback() {

            }
        });
        TestModel m = new TestModel();
        m.title = "hej";
        sprinkles.save(m, t);
        t.setSuccessful(false);
        t.finish();
        assertFalse(called[0]);
    }

}

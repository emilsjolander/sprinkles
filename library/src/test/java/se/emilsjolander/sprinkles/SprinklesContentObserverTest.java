package se.emilsjolander.sprinkles;

import android.annotation.TargetApi;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.*;

@Config(emulateSdk = 18)
@RunWith(RobolectricTestRunner.class)
public class SprinklesContentObserverTest {

    @Test
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public void delegateToWrappedObserver() {
        ContentObserver observer = mock(ContentObserver.class);
        SprinklesContentObserver sprinklesContentObserver = new SprinklesContentObserver(observer);

        sprinklesContentObserver.onChange(true);
        verify(observer).onChange(true);

        reset(observer);
        sprinklesContentObserver.onChange(true, Uri.EMPTY);
        verify(observer).onChange(true, Uri.EMPTY);

        reset(observer);
        sprinklesContentObserver.onChange(false, Uri.EMPTY);
        verify(observer).onChange(false, Uri.EMPTY);
    }
    
}

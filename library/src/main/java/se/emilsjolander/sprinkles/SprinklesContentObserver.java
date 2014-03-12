package se.emilsjolander.sprinkles;

import android.annotation.TargetApi;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;

/**
 * Created by zsiegel on 2/25/14.
 */
public class SprinklesContentObserver extends ContentObserver {

    ContentObserver observer;

    public SprinklesContentObserver(ContentObserver observer) {
        super(null);
        if (observer == null) {
            throw new NullPointerException("ContentObserver may not be null");
        }
        this.observer = observer;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        observer.onChange(selfChange);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        observer.onChange(selfChange, uri);
    }

    public void register(Class<? extends Model> clazz, boolean notifyDescendants) {
        Sprinkles.sInstance.mContext.getContentResolver().registerContentObserver(Utils.getNotificationUri(clazz), notifyDescendants, this);
    }

    public void unregister() {
        Sprinkles.sInstance.mContext.getContentResolver().unregisterContentObserver(this);
    }
    
}

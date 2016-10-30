package se.emilsjolander.sprinkles;

import android.annotation.TargetApi;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;

/**
 * Created by zsiegel on 2/25/14.
 */
public class SprinklesContentObserver extends ContentObserver {

    final Sprinkles sprinkles;
    ContentObserver observer;

    public SprinklesContentObserver(@NonNull Sprinkles sprinkles, @NonNull ContentObserver observer) {
        super(null);
        if (observer == null) {
            throw new NullPointerException("ContentObserver may not be null");
        }
        this.sprinkles = sprinkles;
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
        sprinkles.mContext.getContentResolver().registerContentObserver(Utils.getNotificationUri(clazz), notifyDescendants, this);
    }

    public void unregister() {
        sprinkles.mContext.getContentResolver().unregisterContentObserver(this);
    }
    
}

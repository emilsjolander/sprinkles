package se.emilsjolander.sprinkles;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by zsiegel on 2/25/14.
 */
public class SprinklesContentObserver {

    /**
     * Returns a new content observer
     * @param account the account to be bound to
     * @param authority the authority of the ContentProvider
     * @return a ContentObserver
     */
    public static ContentObserver observer(final Account account, final String authority) {
        return new ContentObserver(null) {

            @Override
            public boolean deliverSelfNotifications() {
                return true;
            }

            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);
                sync();
            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onChange(boolean selfChange, Uri uri) {
                super.onChange(selfChange, uri);
            }

            private void sync() {
                if (account != null) {
                    Bundle extras = new Bundle();
                    extras.putBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD, true);
                    ContentResolver.requestSync(account, authority, extras);
                } else {
                    Log.e("Sprinkles", "ContentObserver has a null account");
                }
            }
        };
    }
}

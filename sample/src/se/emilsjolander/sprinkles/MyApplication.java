package se.emilsjolander.sprinkles;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.ContentResolver;
import android.util.Log;
import se.emilsjolander.sprinkles.auth.AuthConstants;
import se.emilsjolander.sprinkles.models.Note;
import se.emilsjolander.sprinkles.models.NoteTagLink;
import se.emilsjolander.sprinkles.models.Tag;

public class MyApplication extends Application {

    public static final String TAG = "MyApplication";
    public Account syncAccount;

	@Override
	public void onCreate() {
		super.onCreate();
		
		Sprinkles sprinkles = Sprinkles.init(getApplicationContext());
		
		Migration initialMigration = new Migration();
		initialMigration.createTable(Note.class);
		initialMigration.createTable(Tag.class);
		initialMigration.createTable(NoteTagLink.class);
		sprinkles.addMigration(initialMigration);

        //Creates a generic user for syncing if one has not been created already
        syncAccount = getSyncUser();

        //Register any number of models for syncing
        sprinkles.addContentObserver(Note.class, syncAccount, AuthConstants.CONTENT_AUTHORITY);
    }

    /**
     * Typically you would use the AccountAuthenticator to create your own accounts
     *
     * For this sample application we are providing a stubbed user to everyone
     */
    private Account getSyncUser() {

        // Create the account type and default account
        Account newAccount = new Account(AuthConstants.ACCOUNT, AuthConstants.ACCOUNT_TYPE);

        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) this.getSystemService(ACCOUNT_SERVICE);

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            ContentResolver.setIsSyncable(newAccount, AuthConstants.CONTENT_AUTHORITY, 1);
            Log.d(TAG, "Account created for syncing");
        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
            Log.e(TAG, "Account creation error");
        }
        ContentResolver.setSyncAutomatically(newAccount, AuthConstants.CONTENT_AUTHORITY, true);
        return newAccount;
    }
}

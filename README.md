![Icon](https://github.com/emilsjolander/sprinkles/raw/master/sprinkles.png) Sprinkles [![Build Status](https://travis-ci.org/emilsjolander/sprinkles.png)](https://travis-ci.org/emilsjolander/sprinkles)
=========
Sprinkles is a boiler-plate-reduction-library for dealing with databases in android applications. Some would call it a kind of ORM but I don't see it that way. Sprinkles lets SQL do what it is good at, making complex queries. SQL however is a mess (in my opinion) when is comes to everything else. This is why sprinkles helps you with things such as inserting, updating, and destroying models. Sprinkles will also help you with the tedious task of unpacking a cursor into a model. Sprinkles actively supports version 2.3 of Android and above but it should work on older versions as well.

Download
--------
Using gradle, add the following to your `build.gradle`. Just replace `x.x.x` with the correct version of the library (found under the releases tab).

```Groovy
dependencies {
    compile 'se.emilsjolander:sprinkles:x.x.x'
}
```

If you are not using gradle for whatever reason i suggest you clone the repository and check out the latest tag.

Getting started
---------------
When you have added the library to your project add a model class to it. I will demonstrate this with a `Note.java` class. I have omitted the import statements to keep it brief.
```java
@Table("Notes")
public class Note extends Model {

    @Key
	@AutoIncrement
	@Column("id")
	private long id;

	@Column("title")
	public String title;

	@Column("body")
	public String body;

	public long getId() {
		return id;
	}

}
```
Ok, a lot of important stuff in this short class. First of all, a model must subclass `se.emilsjolander.sprinkles.Model` and it also must have a `@Table` annotations specifying the table name that the model corresponds to. After the class declaration we have declared three members: `id`, `title` and `body`. Notice how all of them have a `@Column` annotation to mark that they are not only a member of this class but also a column of the table that this class represents. We have one last annotation in the above example. The `@AutoIncrement` annotation tells sprinkles that the field should automatically be set upon the creation of its corresponding row in the table. Key columns are the columns that are used to decide whether a model is already stored in the database when using methods such as `delete()` and `save()`.

Before using this class you must migrate it into the database. I recommend doing this in the `onCreate()` method of an `Application` subclass like this:
```java
public class MyApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		Sprinkles sprinkles = Sprinkles.init(getApplicationContext());

        sprinkles.addMigration(new Migration() {
            @Override
            protected void onPreMigrate() {
                // do nothing
            }

            @Override
            protected void doMigration(SQLiteDatabase db) {
                db.execSQL(
                        "CREATE TABLE Notes (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                                "title TEXT,"+
                                "body TEXT"+
                        ")"
                );
            }

            @Override
            protected void onPostMigrate() {
                // do nothing
            }
        });
	}

}
```

Now you can happily create new instances of this class and save it to the database like so:
```java
public void saveStuff() {
	Note n = new Note();
	n.title = "Sprinkles is awesome!";
	n.body = "yup, sure is!";
	n.save(); // when this call finishes n.getId() will return a valid id
}
```

You can also query for this note like this:
```java
public void queryStuff() {
	Note n = Query.one(Note.class, "select * from Notes where title=?", "Sprinkles is awesome!").get();
}
```

There is a lot more you can do with sprinkles so please read the next section which covers the whole API!

API
---
###Annotations
- `@Table` Used to associate a model class with a SQL table.
- `@AutoIncrement` Used to mark a field as an auto-incrementing. The field must be an `int` or a `long`.
- `@Column` Used to associate a class field with a SQL column.
- `@DynamicColumn` Used to associate a class field with a dynamic SQL column such as an alias in a query.
- `@Key` Used to mark a field as a key. Multiple keys in a class are allowed and will result in a composite key. Keys will most often want to be mapped directly to primary keys in your database.

###Saving
The save method is both an insert and an update method, the correct operation will be done depending on the model's existence in the database. The first two methods below are synchronous, the second is for use together with a transaction (more on that later). There are also two asynchronous methods, one with a callback and one without. The synchronous methods will return a boolean indicating if the model was saved or not. The asynchronous method with a callback will just not invoke the callback if saving failed.
```java
boolean save();
boolean save(Transaction t);
void saveAsync();
void saveAsync(OnSavedCallback callback);
```

All the save methods use this method to check if a model exists in the database. You are free to use it as well.
```java
boolean exists();
```

###Deleting
Similar to saving there are four methods that let you delete a model. These work in the same way as save but will not return a boolean indicating the result.
```java
void delete();
void delete(Transaction t);
void deleteAsync();
void deleteAsync(OnDeletedCallback callback);
```

###Querying
Start a query with on of the following static methods:
```java
Query.one(Class<? extends QueryResult> clazz, String sql, Object[] args);
Query.many(Class<? extends QueryResult> clazz, String sql, Object[] args);
Query.all(Class<? extends Model> clazz);
```
Notice that unlike androids built in query methods you can send in an array of objects instead of an array of strings.

Once the query has been started you can get the result with two different methods:
```java
<T extends QueryResult> get();
boolean getAsync(LoaderManager lm, ResultHandler<? extends Model> handler, Class<? extends Model>... respondsToUpdatedOf);
```

`get()` returns either the `QueryResult` or a list of the `QueryResult` represented by the `Class` you sent in as the first argument to the query method. `getAsync()` is the same only that the result is delivered on a callback function after executing `get()` on another thread. `getAsync()` also delivers updated results once the backing model of the query is updated if you return `true` indicating you want further updates. `getAsync()` uses loaders and therefore needs a `LoaderManager` instance. `getAsync()` also takes an optional array of classes which is used when the query relies on more models than the one you are querying for and you want the query to be updated when those models change as well.

###CursorList
All Queries return a `CursorList` subclass. This is a `Iterable` subclass which lazily unpacks a cursor into its corresponding model when you ask for the next item. This leads to having the efficiency of a `Cursor` but without the pain. Excluding the `Iterable` methods `CursorList` also provides the following methods.
```java
public int size();
public T get(int pos);
public List<T> asList();
```
Remember to always call `close()` on a `CursorList` instance! This will close the underlying cursor.

###ModelList
For mass saving/deletion of models you can use the `ModelList` class. It extends `ArrayList` and has the following additional methods:
```java
public static <E extends Model> ModelList<E> from(CursorList<E> cursorList);
public boolean saveAll();
public boolean saveAll(Transaction t);
public void saveAllAsync();
public void saveAllAsync(OnAllSavedCallback callback);
public void deleteAll();
public void deleteAll(Transaction t);
public void deleteAllAsync();
public void deleteAllAsync(OnAllDeletedCallback callback);
```

`from(CursorList<E extends Model> cursorList)` is a helper method which creates a `ModelList` from a `CursorList`, so you can e.g. delete all models from a previous query in one batch. Be aware, that the cursor is not closed for you when calling this method and you have to do it yourself!

###Transactions
Both `save()` and `delete()` methods exist which take in a `Transaction`. Here is a quick example on how to use them. If any exception is thrown while saving a model or if any model fails to save the transaction will be rolled back.
```java
public void doTransaction(List<Note> notes) {
	Transaction t = new Transaction();
	try {
		for (Note n : notes) {
			if (!n.save(t)) {
				return;
			}
		}
		t.setSuccessful(true);
	} finally {
		t.finish();
	}
}
```

###Callbacks
Each model subclass can override a couple of callbacks.

Use the following callback to ensure that your model is not saved in an invalid state.
```java
@Override
public boolean isValid() {
	// check model validity
}
```

Use the following callback to update a variable before the model is created
```java
@Override
protected void beforeCreate() {
	mCreatedAt = System.currentTimeMillis();
}
```

Use the following callback to update a variable before the model is saved. This is called directly before `beforeCreate()` if the model is saved for the first time.
```java
@Override
protected void beforeSave() {
	mUpdatedAt = System.currentTimeMillis();
}
```

Use the following callback to clean up things related to the model but not stored in the database. Perhaps a file on the internal storage?
```java
@Override
protected void afterDelete() {
	// clean up some things?
}
```

###Migrations
Migrations are the way you add things to your database. I suggest putting all your migrations in the `onCreate()` method of a `Application` subclass. Here is a quick example of how that would look:
```java
public class MyApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();

		Sprinkles sprinkles = Sprinkles.init(getApplicationContext());

        sprinkles.addMigration(new Migration() {
            @Override
            protected void onPreMigrate() {
                // do nothing
            }

            @Override
            protected void doMigration(SQLiteDatabase db) {
                db.execSQL(
                        "CREATE TABLE Notes (" +
                                "id INTEGER PRIMARY KEY AUTOINCREMENT,"+
                                "title TEXT,"+
                                "body TEXT"+
                        ")"
                );
            }

            @Override
            protected void onPostMigrate() {
                // do nothing
            }
        });
	}

}
```
Migrations are performed using raw SQL, this allowes full freedom to use all of the powerfull contraints that are possible to put on columns. Two optional methods are provided that allow you do some form of processing of your data before and after a migration, this can be usefull when recreating a table with different properties but you want to keep the data that was previously stored in the now deleted table. Once a migration has been added with `sprinkles.addMigration()` it should NEVER be changed, and all new migrations should be added after the previous migration. This ensures both old and new clients will have a consistent database and you will not need to care about database versioning.

###Type serializers
Through an instance of `Sprinkles` you can register your own `TypeSerializer` instances via `registerType()` for serializing an object in your model into a column in the database. Sprinkles uses a `TypeSerializer` implementation internally for all the different data types that it supports. So check out the `se.emilsjolander.sprinkles.typeserializers` package for example implementations. These serializers will be used both when saving a model and when querying rows from the database.

###ContentObservers
Sprinkles supports ContentObservers for change notifications. By registering your models for observation you can ensure your ContentObserver will be notified of changes.
```java
SprinklesContentObserver observer;
ContentObserver myCustomObserver = ...;

this.observer = new SprinklesContentObserver(myCustomObserver);

@Override
public void onResume() {
    super.onResume();
    this.observer.register(Note.class, true); // true/false for notify descendants
}

@Override
public void onPause() {
    super.onPause();
    this.observer.unregister();
}
```

###Relationships
Sprinkles does nothing to handle relationships for you; this is by design. You will have to use the regular ways to handle relationships in SQL. Sprinkles gives you all the tools needed for this and it works very well.

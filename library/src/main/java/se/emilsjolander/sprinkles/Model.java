package se.emilsjolander.sprinkles;

import android.content.ContentValues;
import android.os.AsyncTask;

import java.util.HashMap;

import se.emilsjolander.sprinkles.Transaction.OnTransactionCommittedListener;
import se.emilsjolander.sprinkles.annotations.Ignore;
import se.emilsjolander.sprinkles.exceptions.ContentValuesEmptyException;
import se.emilsjolander.sprinkles.exceptions.IllegalOneToManyColumnException;

public abstract class Model implements QueryResult {

    /**
     * Notifies you when a model has been saved
     */
	public interface OnSavedCallback {
		void onSaved();
	}

    /**
     * Notifies you when a model has been deleted
     */
	public interface OnDeletedCallback {
		void onDeleted();
	}

    /**
     * Notifies you when a model has been saved
     */
    public interface IFieldCopyAction {
        void doCopy(ModelInfo.ColumnField columnField,Object from,Object to);
    }

    /**
     * store extra data ex: foreign key value
     */
    @Ignore
    HashMap<String,Object> mHiddenFieldsMap = new HashMap<String, Object>();

    public Model(){
        try {
            //check relationship of model
            final ModelInfo info = ModelInfo.from(getClass());
            for (ModelInfo.OneToManyColumnField columnField : info.oneToManyColumns){
                columnField.field.setAccessible(true);
                Class one2ManyContainerType = columnField.field.getType();
                if(LazyModelList.class.isAssignableFrom(one2ManyContainerType)){
                    columnField.field.set(this,new LazyModelList(columnField.manyModelClass,this,columnField));
                }else if(ModelList.class.isAssignableFrom(one2ManyContainerType)){
                    columnField.field.set(this,one2ManyContainerType.newInstance());
                }else {
                    throw new IllegalOneToManyColumnException();
                }
            }
            for (ModelInfo.ManyToOneColumnField columnField : info.manyToOneColumns) {
                columnField.field.setAccessible(true);
                Class one2ManyContainerType = columnField.field.getType();
                if(LazyModel.class.isAssignableFrom(one2ManyContainerType)){
                    columnField.field.set(this,new LazyModel(columnField.oneModelClass,this,columnField));
                }
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

//    public static <T extends QueryResult> T createModel(Class<T> clazz){
//        if(clazz==null){
//            throw new IllegalArgumentException("clazz must not be null");
//        }
//        T instance = null;
//        try {
//            instance = clazz.newInstance();
//            //check relationship of model
//            final ModelInfo info = ModelInfo.from(clazz);
//            for (ModelInfo.OneToManyColumnField columnField : info.oneToManyColumns){
//                columnField.field.setAccessible(true);
//                Class one2ManyContainerType = columnField.field.getType();
//                if(!ModelList.class.isAssignableFrom(one2ManyContainerType)){
//                    throw new IllegalOneToManyColumnException();
//                }
//                columnField.field.set(instance,one2ManyContainerType.newInstance());
//            }
//        } catch (InstantiationException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
//        return instance;
//    }

    /**
     * Check if this model is valid. Returning false will not allow this model to be saved.
     *
     * @return whether or not this model is valid.
     */
	public boolean isValid() {
		// optionally implemented by subclass
		return true;
	}

    /**
     * Override to perform an action before this model is created
     */
	protected void beforeCreate() {
		// optionally implemented by subclass
	}

    /**
     * Override to perform an action before this model is saved
     */
	protected void beforeSave() {
		// optionally implemented by subclass
	}

    /**
     * Override to perform an action before this model is deleted
     */
	protected void afterDelete() {
		// optionally implemented by subclass
	}

    /**
     * Check whether this model exists in the database
     *
     * @return true if this model is currently saved in the database (could be an older version)
     */
	final public boolean exists() {
        if(!DataResolver.isTableExist(ModelInfo.from(getClass()))){
            return false;
        }
		return getOlderModel() != null;
	}

    /**
     * get an older version of this model exists in the database
     *
     * @return true if this model is currently saved in the database (could be an older version)
     */
    final public Model getOlderModel() {
        if(!DataResolver.isTableExist(ModelInfo.from(getClass()))){
            return null;
        }
        final Model m = Query.one(
                getClass(),
                String.format("SELECT * FROM %s WHERE %s LIMIT 1",
                        Utils.getTableName(getClass()),
                        Utils.getWhereStatement(this))).get();
        return m;
    }

    /**
     * Copy this model to another model.
     * @param m
     */
    final public void copyTo(Model m){
        copyTo(m,new IFieldCopyAction() {
            @Override
            public void doCopy(ModelInfo.ColumnField columnField, Object from, Object to) {
                try {
                    columnField.field.setAccessible(true);
                    Object valueInThis = columnField.field.get(from);
                    Object valueInTarget = columnField.field.get(to);
                    //if the target has not null value , skip it or over write it
                    if(valueInTarget!=null&&!valueInTarget.equals(valueInThis)){
                        columnField.field.set(to, valueInThis);
                    }else {
                        columnField.field.set(to, valueInThis);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    /**
     * Copy this model to another model.
     * @param m
     */
    final public void copyTo(Model m,IFieldCopyAction fieldCopyAction) {
        ModelInfo table = ModelInfo.from(getClass());
        //copy normal columns
        for (ModelInfo.ColumnField columnField:table.columns){
            if(fieldCopyAction!=null){
                fieldCopyAction.doCopy(columnField, this, m);
            }
        };
        //copy  manyToOneColumns
        for (ModelInfo.ColumnField columnField:table.manyToOneColumns){
            if(fieldCopyAction!=null){
                fieldCopyAction.doCopy(columnField,this,m);
            }
        };
        //copy  oneToManyColumns
        for (ModelInfo.ColumnField columnField:table.oneToManyColumns){
            if(fieldCopyAction!=null){
                fieldCopyAction.doCopy(columnField,this,m);
            }
        };
    }

    /**
     * Save this model to the database.
     * If this model has an @AutoIncrement annotation on a property
     * than that property will be set when this method returns.
     *
     * @return whether or not the save was successful.
     */
	final public boolean save() {
		Transaction t = new Transaction();
		try {
			t.setSuccessful(save(t));
		} finally {
			t.finish();
		}
		return t.isSuccessful();
	}

    /**
     * Save this model to the database within the given transaction.
     * If this model has an @AutoIncrement annotation on a property
     * than that property will be set when this method returns.
     *
     * @param t
     *      The transaction to save this model in
     * @return whether or not the save was successful.
     */
    final public boolean save(Transaction t) {
        return save(t,true);
    }

    /**
     * Save this model to the database within the given transaction.
     * If this model has an @AutoIncrement annotation on a property
     * than that property will be set when this method returns.
     *
     * @param t
     *      The transaction to save this model in
     * @param checkOlder
     *      whether to check and update older model
     * @return whether or not the save was successful.
     */
	final public boolean save(Transaction t,boolean checkOlder) {
		if (!isValid()) {
			return false;
		}

        if(checkOlder&&DataResolver.isCached(this)){
            //if the model has been cached,just update the older model and update the order model to db
            Model olderModel = getOlderModel();
//            throw new IllegalStateException(""+DataResolver.getKeyValueTag(olderModel));
            if(olderModel!=this&&olderModel!=null){
                //sync changes to older model
                copyTo(olderModel,new IFieldCopyAction() {
                    @Override
                    public void doCopy(ModelInfo.ColumnField columnField, Object from, Object to) {
                        try {
                            //if field is a lazy load field, skip it
                            if(LazyModel.class.isAssignableFrom(columnField.field.getType())
                                    ||LazyModelList.class.isAssignableFrom(columnField.field.getType())){
                                return;
                            }
                            columnField.field.setAccessible(true);
                            Object valueInThis = columnField.field.get(from);
                            Object valueInTarget = columnField.field.get(to);
                            //if field is One2Many Field,then check the modellist
                            //if the modellist is empty,add modellist of this model to target model
                            if(columnField instanceof ModelInfo.OneToManyColumnField
                                    &&valueInTarget instanceof ModelList){
                                if(((ModelList)valueInTarget).size()==0){
                                    ((ModelList)valueInTarget).addAll(((ModelList)valueInThis));
                                }
                            }
                            //if the target has not null value , skip it or over write it
                            if(valueInTarget!=null&&!valueInTarget.equals(valueInThis)){
                                columnField.field.set(to, valueInThis);
                            }else {
                                columnField.field.set(to, valueInThis);
                            }
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                });
                return olderModel.save(t,false);
            }
        }

        boolean doesExist = exists();
        if (!doesExist) {
            beforeCreate();
        }

        beforeSave();
        final ContentValues cv = Utils.getContentValues(this);
        if (cv.size() == 0) {
            throw new ContentValuesEmptyException();
        }
//        final String tableName = Utils.getTableName(getClass());
        ModelInfo table = ModelInfo.from(getClass());
        if (doesExist) {
            if (t.update(table, cv, Utils.getWhereStatement(this)) == 0) {
                return false;
            }
        } else {
            long id = t.insert(table, cv);
            if (id == -1) {
                return false;
            }

            // set the @AutoIncrement column if one exists
            if (table.autoIncrementField != null) {
                table.autoIncrementField.field.setAccessible(true);
                try {
                    if(table.autoIncrementField.field.getType()==Integer.class){
                        table.autoIncrementField.field.set(this, (int)id);
                    }else {
                        table.autoIncrementField.field.set(this, id);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
		t.addOnTransactionCommittedListener(new OnTransactionCommittedListener() {

			@Override
			public void onTransactionCommitted() {
                DataResolver.updateRecordCache(Model.this);
				Sprinkles.sInstance.mContext.getContentResolver().notifyChange(
						Utils.getNotificationUri(Model.this.getClass()), null, true);
			}

            @Override
            public void onTransactionRollback() {
                DataResolver.removeRecordCache(Model.this);
            }
        });

		return true;
	}

    /**
     * Call save() asynchronously
     */
	final public void saveAsync() {
		saveAsync(null);
	}

    /**
     * Call save() asynchronously
     *
     * @param callback
     *      The callback to invoke when this model has been saved.
     */
	final public void saveAsync(final OnSavedCallback callback) {
		new AsyncTask<Model, Void, Boolean>() {

			protected Boolean doInBackground(Model... params) {
				return params[0].save();
			}

			protected void onPostExecute(Boolean result) {
				if (result && callback != null) {
					callback.onSaved();
				}
			}

		}.execute(this);
	}

    /**
     * Delete this model
     */
	final public void delete() {
		Transaction t = new Transaction();
		try {
			delete(t);
			t.setSuccessful(true);
		} finally {
			t.finish();
		}
	}

    /**
     * Delete this model within the given transaction
     *
     * @param t
     *      The transaction to delete this model in
     */
	final public void delete(Transaction t) {
		t.delete(ModelInfo.from(getClass()), Utils.getWhereStatement(this));
        t.addOnTransactionCommittedListener(new OnTransactionCommittedListener() {

            @Override
            public void onTransactionCommitted() {
                DataResolver.removeRecordCache(Model.this);
                Sprinkles.sInstance.mContext.getContentResolver().notifyChange(
                        Utils.getNotificationUri(Model.this.getClass()), null);
            }

            @Override
            public void onTransactionRollback() {

            }
        });
		afterDelete();
	}

    /**
     * Call delete() asynchronously
     */
	final public void deleteAsync() {
		deleteAsync(null);
	}

    /**
     * Call delete() asynchronously
     *
     * @param callback
     *      The callback to invoke when this model has been deleted.
     */
	final public void deleteAsync(final OnDeletedCallback callback) {
		new AsyncTask<Model, Void, Void>() {

			protected Void doInBackground(Model... params) {
				params[0].delete();
				return null;
			}

			protected void onPostExecute(Void result) {
				if (callback != null) {
					callback.onDeleted();
				}
			}

		}.execute(this);
	}

}

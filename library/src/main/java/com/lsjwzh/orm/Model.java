package com.lsjwzh.orm;

import java.util.HashMap;

import com.lsjwzh.orm.annotations.Ignore;

public abstract class Model implements QueryResult {

    /**
     * Notifies you when a model has been saved
     */
    public interface IFieldCopyAction {
        void doCopy(ModelInfo.ColumnField columnField, Object from, Object to);
    }

    /**
     * store extra data ex: foreign key value
     */
    @Ignore
    HashMap<String, Object> mHiddenFieldsMap = new HashMap<>();


}

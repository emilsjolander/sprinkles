package com.lsjwzh.orm;

import java.util.ArrayList;
import java.util.Collection;

public class ModelList<E extends Model> extends ArrayList<E> {

    private static final long serialVersionUID = 9111033070491580889L;

    public static <E extends Model> ModelList<E> from(CursorList<E> cursorList) {
        return new ModelList<>(cursorList.asList());
    }

    public static <E extends Model> ModelList<E> from(CursorList<E> cursorList, int skip) {
        ModelList<E> modelList = new ModelList<>(cursorList.size() > skip ? cursorList.size() - skip : 0);
        for (int i = skip; i < cursorList.size(); i++) {
            modelList.add(cursorList.get(i));
        }
        return modelList;
    }

    public ModelList() {
        super();
    }

    public ModelList(int capacity) {
        super(capacity);
    }

    public ModelList(Collection<? extends E> collection) {
        super(collection);
    }

}

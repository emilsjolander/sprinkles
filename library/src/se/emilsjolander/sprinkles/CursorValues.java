package se.emilsjolander.sprinkles;

import android.database.Cursor;

import java.util.HashMap;
import java.util.Map;

/**
 * Class used to retrieve arbitrary values from a query.
 * Use this instead of a model as an argument to the query methods
 */
public class CursorValues {

    private Map<String, Object> valueMap = new HashMap<String, Object>();

    CursorValues (Cursor c) {
        // TODO fill valueMap from cursor
    }

    /**
     * @return The column value as a string. Will throw exception if not a string.
     */
    public String getString(String column) {
        Object value = valueMap.get(column);
        if (value instanceof String) {
            return (String) value;
        } else {
            throw new ClassCastException("Column was not of string type");
        }
    }

    /**
     * @return The column value as a byte. Will throw exception if not a byte.
     */
    public byte getByte(String column) {
        Object value = valueMap.get(column);
        if (value instanceof Byte) {
            return (Byte) value;
        } else {
            throw new ClassCastException("Column was not of byte type");
        }
    }

    /**
     * @return The column value as a int. Will throw exception if not a int.
     */
    public int getInt(String column) {
        Object value = valueMap.get(column);
        if (value instanceof Integer) {
            return (Integer) value;
        } else {
            throw new ClassCastException("Column was not of int type");
        }
    }

    /**
     * @return The column value as a long. Will throw exception if not a long.
     */
    public long getLong(String column) {
        Object value = valueMap.get(column);
        if (value instanceof Long) {
            return (Long) value;
        } else {
            throw new ClassCastException("Column was not of long type");
        }
    }

    /**
     * @return The column value as a float. Will throw exception if not a float.
     */
    public float getFloat(String column) {
        Object value = valueMap.get(column);
        if (value instanceof Float) {
            return (Float) value;
        } else {
            throw new ClassCastException("Column was not of float type");
        }
    }

    /**
     * @return The column value as a double. Will throw exception if not a double.
     */
    public double getDouble(String column) {
        Object value = valueMap.get(column);
        if (value instanceof Double) {
            return (Double) value;
        } else {
            throw new ClassCastException("Column was not of double type");
        }
    }

    /**
     * @return The column value as a boolean. Will throw exception if not a boolean.
     */
    public boolean getBoolean(String column) {
        Object value = valueMap.get(column);
        if (value instanceof Integer) {
            return (Integer)value > 0;
        } else {
            throw new ClassCastException("Column was not of boolean type");
        }
    }

}

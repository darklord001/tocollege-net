package com.apress.progwt.client.json;

import com.google.gwt.json.client.JSONBoolean;
import com.google.gwt.json.client.JSONNumber;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONString;
import com.google.gwt.json.client.JSONValue;

/**
 * Handy JSON wrapper class. The basic google JSON classes are pretty
 * spare and it can make for verbose serialization/deserialization code.
 * 
 * @author Jeff Dwyer
 * 
 */
public class JSONWrapper {

    private JSONObject object;

    public JSONWrapper(JSONObject object) {
        this.object = object;
    }

    public JSONWrapper(JSONValue value) {
        this.object = value.isObject();
        if (object == null) {
            throw new UnsupportedOperationException("value not object");
        }
    }

    public JSONWrapper() {
        this(new JSONObject());
    }

    /**
     * Odd - Boolean.parseBoolean() is not implemented by GWT
     * 
     * @param key
     * @return
     */
    public boolean getBoolean(String key) {
        String boolS = object.get(key).isBoolean().toString();
        return boolS.equals("true") ? true : false;
    }

    public int getInt(String key) {
        return Integer.parseInt(object.get(key).isNumber().toString());
    }

    public long getLong(String key) {
        return Long.parseLong(object.get(key).isNumber().toString());
    }

    public JSONObject getObject() {
        return object;
    }

    /**
     * Avoid the escaping that toString() does.
     * 
     * @param key
     * @return
     */
    public String getString(String key) {
        return object.get(key).isString().stringValue();
    }

    public void put(String key, boolean value) {
        object.put(key, JSONBoolean.getInstance(value));
    }

    public void put(String key, int value) {
        object.put(key, new JSONNumber(value));
    }

    public void put(String key, long value) {
        object.put(key, new JSONNumber(value));
    }

    public void put(String key, String value) {
        object.put(key, new JSONString(value));
    }

    public JSONObject getJSONObject(String key) {
        return object.get(key).isObject();
    }
}

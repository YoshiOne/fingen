package com.yoshione.fingen.model;

/**
 * Created by slv on 18.05.2016.
 *
 */
public class EntityToFieldLink {
    public static final int ENTITY_TYPE_DATE        = 0;
    public static final int ENTITY_TYPE_TIME        = 1;
    public static final int ENTITY_TYPE_ACCOUNT     = 2;
    public static final int ENTITY_TYPE_CURRENCY    = 3;
    public static final int ENTITY_TYPE_PAYEE       = 4;
    public static final int ENTITY_TYPE_CATEGORY    = 5;
    public static final int ENTITY_TYPE_AMOUNT      = 6;
    public static final int ENTITY_TYPE_LOCATION    = 7;
    public static final int ENTITY_TYPE_PROJECT     = 8;
    public static final int ENTITY_TYPE_DEPARTMENT     = 9;
    public static final int ENTITY_TYPE_COMMENT     = 10;

    public static final int[] ENTITY_TYPES = new int[]{
            ENTITY_TYPE_DATE,
            ENTITY_TYPE_TIME,
            ENTITY_TYPE_ACCOUNT,
            ENTITY_TYPE_CURRENCY,
            ENTITY_TYPE_PAYEE,
            ENTITY_TYPE_CATEGORY,
            ENTITY_TYPE_AMOUNT,
            ENTITY_TYPE_LOCATION,
            ENTITY_TYPE_PROJECT,
            ENTITY_TYPE_DEPARTMENT,
            ENTITY_TYPE_COMMENT};
    
    private int type;
    private String field = "-";

    public EntityToFieldLink(int type) {
        this.type = type;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}

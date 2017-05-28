package com.mercy.compiler.Type;

import com.mercy.compiler.Entity.ClassEntity;

/**
 * Created by mercy on 17-3-18.
 */
public class ClassType extends Type {
    static final int DEFAULT_SIZE = 8;
    static public final String CONSTRUCTOR_NAME = "__constructor_";

    protected String name;
    protected ClassEntity entity;

    public ClassType(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public ClassEntity entity() {
        return entity;
    }

    public void setEntity(ClassEntity entity) {
        this.entity = entity;
    }

    @Override
    public boolean isClass() {
        return true;
    }

    @Override
    public boolean isCompatible(Type other) {
        if (other.isNull())    return true;
        if (!other.isClass())  return false;
        return entity.equals(((ClassType)other).entity);
    }

    @Override
    public int size() {
        return DEFAULT_SIZE;
    }

    @Override
    public String toString() {
        return "class " + name;
    }
}

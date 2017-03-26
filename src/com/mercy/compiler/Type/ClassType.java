package com.mercy.compiler.Type;

import com.mercy.compiler.Entity.ClassEntity;

/**
 * Created by mercy on 17-3-18.
 */
public class ClassType extends Type {
    static final long DEFAULT_SIZE = 4;

    protected String name;
    protected ClassEntity entity;

    public ClassType(String name) {
        this.name = name;
    }

    public ClassType(ClassEntity entity) {
        this.name = entity.name();
        this.entity = entity;
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
        if (!other.isClass())
            return false;
        if (other.isNull())
            return true;
        return entity.equals(((ClassType)other).entity);
    }

    @Override
    public boolean isHalfComparable() {
        return true;
    }

    @Override
    public long size() {
        return DEFAULT_SIZE;
    }

    @Override
    public long alignment() {
        return DEFAULT_SIZE;
    }

    @Override
    public String toString() {
        return "class " + name;
    }
}

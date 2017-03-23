package com.mercy.compiler.Type;

import com.mercy.compiler.Entity.ClassEntity;

/**
 * Created by mercy on 17-3-18.
 */
public class ClassType extends Type {
    private String name;
    private ClassEntity entity;
    private long cachedSize, cachedAlign;

    public ClassType(String name) {
        this.name = name;
    }

    public ClassType(String name, ClassEntity classEntity) {
        this.name = name;
        this.entity = classEntity;
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

    public long cachedSize() {
        return cachedSize;
    }

    public long cachedAlign() {
        return cachedAlign;
    }

    @Override
    public boolean isClass() {
        return true;
    }

    @Override
    public long size() {
        return 0;
    }

    @Override
    public long alignment() {
        return 0;
    }

    @Override
    public String toString() {
        return "class " + name;
    }
}

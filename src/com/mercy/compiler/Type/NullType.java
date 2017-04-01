package com.mercy.compiler.Type;

import com.mercy.compiler.Entity.ClassEntity;

/**
 * Created by mercy on 17-3-25.
 */
public class NullType extends ClassType {
    public NullType() {
        super("null");
    }

    @Override
    public boolean isNull() {
        return true;
    }

    @Override
    public boolean isHalfComparable() {
        return true;
    }

    @Override
    public boolean isCompatible(Type other) {
        return other.isArray() || other.isClass() || other.isNull();
    }
}

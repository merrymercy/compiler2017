package com.mercy.compiler.Type;

import com.mercy.compiler.Entity.Scope;
import com.mercy.compiler.Utility.LibFunction;

/**
 * Created by mercy on 17-3-23.
 */
public class StringType extends Type {
    static final long DEFAULT_SIZE = 4;
    static private Scope scope;

    static public void initializeBuiltinFunction() {
        scope = new Scope(true);
        scope.insert(new LibFunction(integerType, "length", new Type[]{stringType}).getEntity());
        scope.insert(new LibFunction(stringType, "substring", new Type[]{stringType, integerType, integerType}).getEntity());
        scope.insert(new LibFunction(integerType, "parseInt", new Type[]{stringType}).getEntity());
        scope.insert(new LibFunction(integerType, "ord", new Type[]{stringType, integerType}).getEntity());
    }

    static public Scope scope() {
        return scope;
    }

    @Override
    public boolean isString() {
        return true;
    }

    @Override
    public boolean isCompatible(Type other) {
        return other.isString();
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
    public String toString() {
        return "string";
    }
}

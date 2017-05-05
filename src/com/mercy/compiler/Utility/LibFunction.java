package com.mercy.compiler.Utility;

import com.mercy.compiler.Entity.FunctionEntity;
import com.mercy.compiler.Entity.ParameterEntity;
import com.mercy.compiler.Type.Type;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by mercy on 17-3-25.
 */
public class LibFunction {
    FunctionEntity entity;

    public LibFunction(Type returnType, String name, Type [] paramTypes) {
        List<ParameterEntity> paramEntities = new LinkedList<>();
        if (paramTypes != null) {
            for (Type paramType : paramTypes) {
                paramEntities.add(new ParameterEntity(null, paramType, null));
            }
        }
        entity = new FunctionEntity(null, returnType, name, paramEntities, null);

    }

    public FunctionEntity getEntity() {
        return entity;
    }
}

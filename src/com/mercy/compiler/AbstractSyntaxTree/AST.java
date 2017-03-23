package com.mercy.compiler.AbstractSyntaxTree;

import com.mercy.compiler.Entity.ClassEntity;
import com.mercy.compiler.Entity.FunctionEntity;
import com.mercy.compiler.Entity.Scope;

import java.util.List;

/**
 * Created by mercy on 17-3-18.
 */
public class AST {
    private Scope scope;
    private List<FunctionEntity> functionEntities;
    private List<ClassEntity> classEntitsies;

    public AST(List<FunctionEntity> definedFunction, List<ClassEntity> definedClass) {
        super();
        this.functionEntities = definedFunction;
        this.classEntitsies = definedClass;
    }
}

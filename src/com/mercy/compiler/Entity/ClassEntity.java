package com.mercy.compiler.Entity;

import com.mercy.compiler.AbstractSyntaxTree.ParameterDefNode;
import com.mercy.compiler.AbstractSyntaxTree.VariableDefNode;
import com.mercy.compiler.Type.Type;

import java.util.List;

/**
 * Created by mercy on 17-3-23.
 */
public class ClassEntity extends Entity {
    private List<VariableDefNode> memberVars;
    private List<FunctionEntity> memberFuncs;
    private Scope scope;

    public ClassEntity (Type type, String name, List<VariableDefNode> memberVars, List<FunctionEntity> memberFuncs) {
        super(type, name);
        this.memberVars = memberVars;
        this.memberFuncs = memberFuncs;
    }

    public List<VariableDefNode> memberVars() {
        return memberVars;
    }

    public List<FunctionEntity> memberFuncs() {
        return memberFuncs;
    }

    @Override
    public <T> T accept(EntityVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "class entity : " + name;
    }
}

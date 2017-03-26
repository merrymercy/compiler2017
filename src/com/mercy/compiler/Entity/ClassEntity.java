package com.mercy.compiler.Entity;

import com.mercy.compiler.AST.FunctionDefNode;
import com.mercy.compiler.AST.Location;
import com.mercy.compiler.AST.VariableDefNode;
import com.mercy.compiler.Type.ClassType;

import java.util.List;

/**
 * Created by mercy on 17-3-23.
 */
public class ClassEntity extends Entity {
    private List<VariableDefNode> memberVars;
    private List<FunctionDefNode> memberFuncs;
    private Scope scope;
    private ClassType classType; // for add "this" pointer

    public ClassEntity (Location loc, String name, List<VariableDefNode> memberVars, List<FunctionDefNode> memberFuncs) {
        super(loc, new ClassType(name), name);
        this.memberVars = memberVars;
        this.memberFuncs = memberFuncs;
        ((ClassType)this.type).setEntity(this);
    }

    public List<VariableDefNode> memberVars() {
        return memberVars;
    }

    public List<FunctionDefNode> memberFuncs() {
        return memberFuncs;
    }

    public Scope scope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
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

package com.mercy.compiler.Entity;

import com.mercy.compiler.AbstractSyntaxTree.BlockNode;
import com.mercy.compiler.AbstractSyntaxTree.ParameterDefNode;
import com.mercy.compiler.Type.Type;

import java.util.List;

/**
 * Created by mercy on 17-3-20.
 */
public class FunctionEntity extends Entity {
    private List<ParameterDefNode> params;
    private BlockNode body;
    private Scope scope;

    public FunctionEntity(Type returnType, String name, List<ParameterDefNode> params, BlockNode body) {
        super(returnType, name);
        this.params = params;
        this.body = body;
    }

    public List<ParameterDefNode> params() {
        return params;
    }

    public BlockNode body() {
        return body;
    }

    public Scope scope() {
        return scope;
    }

    @Override
    public <T> T accept(EntityVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "function entity : " + name;
    }
}

package com.mercy.compiler.AbstractSyntaxTree;

import com.mercy.compiler.Type.FunctionType;
import com.mercy.compiler.Type.Type;

import java.util.List;

/**
 * Created by mercy on 17-3-18.
 */
public class FuncallNode extends ExprNode {
    private ExprNode expr;
    private List<ExprNode> args;

    public FuncallNode(ExprNode expr, List<ExprNode> args) {
        this.expr = expr;
        this.args = args;
    }

    public ExprNode expr() {
        return expr;
    }

    public List<ExprNode> args() {
        return args;
    }

    public void addThisPointer(ExprNode expr) {
        args.add(0, expr);
    }

    @Override
    public Type type() {
        return functionType().entity().returnType();
    }

    public FunctionType functionType() {
        return (FunctionType)expr.type();
    }

    @Override
    public Location location() {
        return expr.location();
    }

    @Override
    public <S,E> E accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}
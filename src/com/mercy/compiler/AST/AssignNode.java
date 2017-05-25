package com.mercy.compiler.AST;

import com.mercy.compiler.FrontEnd.ASTVisitor;
import com.mercy.compiler.Type.Type;

/**
 * Created by mercy on 17-3-18.
 */
public class AssignNode extends ExprNode {
    private ExprNode lhs, rhs;

    public AssignNode(ExprNode lhs, ExprNode rhs) {
        super();
        this.lhs = lhs;
        this.rhs = rhs;
        this.isOutputIrrevelant = false;
    }

    public ExprNode lhs() {
        return lhs;
    }

    public ExprNode rhs() {
        return rhs;
    }

    public void setLhs(ExprNode lhs) {
        this.lhs = lhs;
    }

    public void setRhs(ExprNode rhs) {
        this.rhs = rhs;
    }

    @Override
    public Type type() {
        return lhs.type();
    }

    @Override
    public Location location() {
        return lhs.location();
    }

    @Override
    public <S, E> E accept(ASTVisitor<S, E> visitor) {
        return visitor.visit(this);
    }
}

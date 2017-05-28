package com.mercy.compiler.AST;

import com.mercy.compiler.FrontEnd.ASTVisitor;
import com.mercy.compiler.Type.Type;

/**
 * Created by mercy on 17-3-21.
 */
public class ArefNode extends LHSNode {
    private ExprNode expr, index;

    public ArefNode(ExprNode expr, ExprNode index) {
        this.expr = expr;
        this.index = index;
    }

    public ArefNode(ExprNode expr, ExprNode index, Type type) {
        this.expr = expr;
        this.index = index;
        this.type = type;
    }

    public ExprNode expr() { return expr; }
    public ExprNode index() { return index; }

    public boolean isMultiDimension() {
        return (expr instanceof ArefNode);
    }

    public ExprNode baseExpr() {
        return isMultiDimension() ? ((ArefNode)expr).baseExpr() : expr;
    }

    public Location location() {
        return expr.location();
    }

    public <S, E> E accept(ASTVisitor<S,E> visitor) {
        return visitor.visit(this);
    }
}

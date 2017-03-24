package com.mercy.compiler.AbstractSyntaxTree;

import com.mercy.compiler.Type.ClassType;
import com.mercy.compiler.Type.Type;

/**
 * Created by mercy on 17-3-18.
 */
public class MemberNode extends LHSNode {
    private ExprNode expr;
    private String member;

    public MemberNode(ExprNode expr, String member) {
        this.expr = expr;
        this.member = member;
    }

    public ExprNode expr() {
        return expr;
    }

    public String member() {
        return member;
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

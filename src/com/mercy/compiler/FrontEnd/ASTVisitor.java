package com.mercy.compiler.FrontEnd;

import com.mercy.compiler.AST.*;

/**
 * Created by mercy on 17-3-21.
 */
public interface ASTVisitor<S, E> {
    // Statements
    S visit(BlockNode node);
    S visit(ExprStmtNode node);
    S visit(IfNode node);
    S visit(WhileNode node);
    S visit(ForNode node);
    S visit(BreakNode node);
    S visit(ContinueNode node);
    S visit(ReturnNode node);
    // Definitions
    S visit(VariableDefNode node);
    S visit(FunctionDefNode node);
    S visit(ClassDefNode node);

    // Expressions
    E visit(AssignNode node);
    E visit(LogicalOrNode node);
    E visit(LogicalAndNode node);
    E visit(BinaryOpNode node);
    E visit(UnaryOpNode node);
    E visit(CreatorNode node);
    E visit(PrefixOpNode node);
    E visit(SuffixOpNode node);
    E visit(ArefNode node);
    E visit(MemberNode node);
    E visit(FuncallNode node);
    E visit(VariableNode node);
    E visit(IntegerLiteralNode node);
    E visit(StringLiteralNode node);
    E visit(BoolLiteralNode node);
}
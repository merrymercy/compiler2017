package com.mercy.compiler.AbstractSyntaxTree;

/**
 * Created by mercy on 17-3-21.
 */
public interface ASTVisitor<S, E> {
    // Statements
    public S visit(BlockNode node);
    public S visit(ExprStmtNode node);
    public S visit(IfNode node);
    public S visit(WhileNode node);
    public S visit(ForNode node);
    public S visit(BreakNode node);
    public S visit(ContinueNode node);
    public S visit(ReturnNode node);
    // Definitions
    public S visit(VariableDefNode node);
    public S visit(FunctionDefNode node);
    public S visit(ClassDefNode node);
    public S visit(ParameterDefNode node);

    // Expressions
    public E visit(AssignNode node);
    public E visit(LogicalOrNode node);
    public E visit(LogicalAndNode node);
    public E visit(BinaryOpNode node);
    public E visit(UnaryOpNode node);
    public E visit(CreatorNode node);
    public E visit(PrefixOpNode node);
    public E visit(SuffixOpNode node);
    public E visit(ArefNode node);
    public E visit(MemberNode node);
    public E visit(FuncallNode node);
    public E visit(VariableNode node);
    public E visit(IntegerLiteralNode node);
    public E visit(StringLiteralNode node);
    public E visit(BoolLiteralNode node);
}
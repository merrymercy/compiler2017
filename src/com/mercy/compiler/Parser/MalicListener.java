// Generated from /home/mercy/project/compiler-2017/src/com/mercy/compiler/Parser/Malic.g4 by ANTLR 4.6
package com.mercy.compiler.Parser;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link MalicParser}.
 */
public interface MalicListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link MalicParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void enterCompilationUnit(MalicParser.CompilationUnitContext ctx);
	/**
	 * Exit a parse tree produced by {@link MalicParser#compilationUnit}.
	 * @param ctx the parse tree
	 */
	void exitCompilationUnit(MalicParser.CompilationUnitContext ctx);
	/**
	 * Enter a parse tree produced by {@link MalicParser#classDefinition}.
	 * @param ctx the parse tree
	 */
	void enterClassDefinition(MalicParser.ClassDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link MalicParser#classDefinition}.
	 * @param ctx the parse tree
	 */
	void exitClassDefinition(MalicParser.ClassDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link MalicParser#functionDefinition}.
	 * @param ctx the parse tree
	 */
	void enterFunctionDefinition(MalicParser.FunctionDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link MalicParser#functionDefinition}.
	 * @param ctx the parse tree
	 */
	void exitFunctionDefinition(MalicParser.FunctionDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link MalicParser#variableDefinition}.
	 * @param ctx the parse tree
	 */
	void enterVariableDefinition(MalicParser.VariableDefinitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link MalicParser#variableDefinition}.
	 * @param ctx the parse tree
	 */
	void exitVariableDefinition(MalicParser.VariableDefinitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link MalicParser#parameter}.
	 * @param ctx the parse tree
	 */
	void enterParameter(MalicParser.ParameterContext ctx);
	/**
	 * Exit a parse tree produced by {@link MalicParser#parameter}.
	 * @param ctx the parse tree
	 */
	void exitParameter(MalicParser.ParameterContext ctx);
	/**
	 * Enter a parse tree produced by {@link MalicParser#primitiveType}.
	 * @param ctx the parse tree
	 */
	void enterPrimitiveType(MalicParser.PrimitiveTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link MalicParser#primitiveType}.
	 * @param ctx the parse tree
	 */
	void exitPrimitiveType(MalicParser.PrimitiveTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link MalicParser#typeType}.
	 * @param ctx the parse tree
	 */
	void enterTypeType(MalicParser.TypeTypeContext ctx);
	/**
	 * Exit a parse tree produced by {@link MalicParser#typeType}.
	 * @param ctx the parse tree
	 */
	void exitTypeType(MalicParser.TypeTypeContext ctx);
	/**
	 * Enter a parse tree produced by {@link MalicParser#block}.
	 * @param ctx the parse tree
	 */
	void enterBlock(MalicParser.BlockContext ctx);
	/**
	 * Exit a parse tree produced by {@link MalicParser#block}.
	 * @param ctx the parse tree
	 */
	void exitBlock(MalicParser.BlockContext ctx);
	/**
	 * Enter a parse tree produced by the {@code blockStmt}
	 * labeled alternative in {@link MalicParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterBlockStmt(MalicParser.BlockStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code blockStmt}
	 * labeled alternative in {@link MalicParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitBlockStmt(MalicParser.BlockStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code varDefStmt}
	 * labeled alternative in {@link MalicParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterVarDefStmt(MalicParser.VarDefStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code varDefStmt}
	 * labeled alternative in {@link MalicParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitVarDefStmt(MalicParser.VarDefStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code ifStmt}
	 * labeled alternative in {@link MalicParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterIfStmt(MalicParser.IfStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code ifStmt}
	 * labeled alternative in {@link MalicParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitIfStmt(MalicParser.IfStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code forStmt}
	 * labeled alternative in {@link MalicParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterForStmt(MalicParser.ForStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code forStmt}
	 * labeled alternative in {@link MalicParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitForStmt(MalicParser.ForStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code whileStmt}
	 * labeled alternative in {@link MalicParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterWhileStmt(MalicParser.WhileStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code whileStmt}
	 * labeled alternative in {@link MalicParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitWhileStmt(MalicParser.WhileStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code returnStmt}
	 * labeled alternative in {@link MalicParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterReturnStmt(MalicParser.ReturnStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code returnStmt}
	 * labeled alternative in {@link MalicParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitReturnStmt(MalicParser.ReturnStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code breakStmt}
	 * labeled alternative in {@link MalicParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterBreakStmt(MalicParser.BreakStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code breakStmt}
	 * labeled alternative in {@link MalicParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitBreakStmt(MalicParser.BreakStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code continueStmt}
	 * labeled alternative in {@link MalicParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterContinueStmt(MalicParser.ContinueStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code continueStmt}
	 * labeled alternative in {@link MalicParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitContinueStmt(MalicParser.ContinueStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code exprStmt}
	 * labeled alternative in {@link MalicParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterExprStmt(MalicParser.ExprStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code exprStmt}
	 * labeled alternative in {@link MalicParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitExprStmt(MalicParser.ExprStmtContext ctx);
	/**
	 * Enter a parse tree produced by the {@code blankStmt}
	 * labeled alternative in {@link MalicParser#statement}.
	 * @param ctx the parse tree
	 */
	void enterBlankStmt(MalicParser.BlankStmtContext ctx);
	/**
	 * Exit a parse tree produced by the {@code blankStmt}
	 * labeled alternative in {@link MalicParser#statement}.
	 * @param ctx the parse tree
	 */
	void exitBlankStmt(MalicParser.BlankStmtContext ctx);
	/**
	 * Enter a parse tree produced by {@link MalicParser#expressionList}.
	 * @param ctx the parse tree
	 */
	void enterExpressionList(MalicParser.ExpressionListContext ctx);
	/**
	 * Exit a parse tree produced by {@link MalicParser#expressionList}.
	 * @param ctx the parse tree
	 */
	void exitExpressionList(MalicParser.ExpressionListContext ctx);
	/**
	 * Enter a parse tree produced by the {@code newExpr}
	 * labeled alternative in {@link MalicParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterNewExpr(MalicParser.NewExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code newExpr}
	 * labeled alternative in {@link MalicParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitNewExpr(MalicParser.NewExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalOrExpr}
	 * labeled alternative in {@link MalicParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalOrExpr(MalicParser.LogicalOrExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalOrExpr}
	 * labeled alternative in {@link MalicParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalOrExpr(MalicParser.LogicalOrExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code prefixExpr}
	 * labeled alternative in {@link MalicParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterPrefixExpr(MalicParser.PrefixExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code prefixExpr}
	 * labeled alternative in {@link MalicParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitPrefixExpr(MalicParser.PrefixExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code primaryExpr}
	 * labeled alternative in {@link MalicParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterPrimaryExpr(MalicParser.PrimaryExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code primaryExpr}
	 * labeled alternative in {@link MalicParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitPrimaryExpr(MalicParser.PrimaryExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code logicalAndExpr}
	 * labeled alternative in {@link MalicParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterLogicalAndExpr(MalicParser.LogicalAndExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code logicalAndExpr}
	 * labeled alternative in {@link MalicParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitLogicalAndExpr(MalicParser.LogicalAndExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code funcallExpr}
	 * labeled alternative in {@link MalicParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterFuncallExpr(MalicParser.FuncallExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code funcallExpr}
	 * labeled alternative in {@link MalicParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitFuncallExpr(MalicParser.FuncallExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code memberExpr}
	 * labeled alternative in {@link MalicParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterMemberExpr(MalicParser.MemberExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code memberExpr}
	 * labeled alternative in {@link MalicParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitMemberExpr(MalicParser.MemberExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code arefExpr}
	 * labeled alternative in {@link MalicParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterArefExpr(MalicParser.ArefExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code arefExpr}
	 * labeled alternative in {@link MalicParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitArefExpr(MalicParser.ArefExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code suffixExpr}
	 * labeled alternative in {@link MalicParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterSuffixExpr(MalicParser.SuffixExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code suffixExpr}
	 * labeled alternative in {@link MalicParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitSuffixExpr(MalicParser.SuffixExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code binaryExpr}
	 * labeled alternative in {@link MalicParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterBinaryExpr(MalicParser.BinaryExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code binaryExpr}
	 * labeled alternative in {@link MalicParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitBinaryExpr(MalicParser.BinaryExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code assignExpr}
	 * labeled alternative in {@link MalicParser#expression}.
	 * @param ctx the parse tree
	 */
	void enterAssignExpr(MalicParser.AssignExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code assignExpr}
	 * labeled alternative in {@link MalicParser#expression}.
	 * @param ctx the parse tree
	 */
	void exitAssignExpr(MalicParser.AssignExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code subExpr}
	 * labeled alternative in {@link MalicParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterSubExpr(MalicParser.SubExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code subExpr}
	 * labeled alternative in {@link MalicParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitSubExpr(MalicParser.SubExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code thisExpr}
	 * labeled alternative in {@link MalicParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterThisExpr(MalicParser.ThisExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code thisExpr}
	 * labeled alternative in {@link MalicParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitThisExpr(MalicParser.ThisExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code variableExpr}
	 * labeled alternative in {@link MalicParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterVariableExpr(MalicParser.VariableExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code variableExpr}
	 * labeled alternative in {@link MalicParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitVariableExpr(MalicParser.VariableExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code literalExpr}
	 * labeled alternative in {@link MalicParser#primary}.
	 * @param ctx the parse tree
	 */
	void enterLiteralExpr(MalicParser.LiteralExprContext ctx);
	/**
	 * Exit a parse tree produced by the {@code literalExpr}
	 * labeled alternative in {@link MalicParser#primary}.
	 * @param ctx the parse tree
	 */
	void exitLiteralExpr(MalicParser.LiteralExprContext ctx);
	/**
	 * Enter a parse tree produced by the {@code DecIntegerConst}
	 * labeled alternative in {@link MalicParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterDecIntegerConst(MalicParser.DecIntegerConstContext ctx);
	/**
	 * Exit a parse tree produced by the {@code DecIntegerConst}
	 * labeled alternative in {@link MalicParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitDecIntegerConst(MalicParser.DecIntegerConstContext ctx);
	/**
	 * Enter a parse tree produced by the {@code StringConst}
	 * labeled alternative in {@link MalicParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterStringConst(MalicParser.StringConstContext ctx);
	/**
	 * Exit a parse tree produced by the {@code StringConst}
	 * labeled alternative in {@link MalicParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitStringConst(MalicParser.StringConstContext ctx);
	/**
	 * Enter a parse tree produced by the {@code boolConst}
	 * labeled alternative in {@link MalicParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterBoolConst(MalicParser.BoolConstContext ctx);
	/**
	 * Exit a parse tree produced by the {@code boolConst}
	 * labeled alternative in {@link MalicParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitBoolConst(MalicParser.BoolConstContext ctx);
	/**
	 * Enter a parse tree produced by the {@code nullConst}
	 * labeled alternative in {@link MalicParser#literal}.
	 * @param ctx the parse tree
	 */
	void enterNullConst(MalicParser.NullConstContext ctx);
	/**
	 * Exit a parse tree produced by the {@code nullConst}
	 * labeled alternative in {@link MalicParser#literal}.
	 * @param ctx the parse tree
	 */
	void exitNullConst(MalicParser.NullConstContext ctx);
	/**
	 * Enter a parse tree produced by the {@code errorCreator}
	 * labeled alternative in {@link MalicParser#creator}.
	 * @param ctx the parse tree
	 */
	void enterErrorCreator(MalicParser.ErrorCreatorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code errorCreator}
	 * labeled alternative in {@link MalicParser#creator}.
	 * @param ctx the parse tree
	 */
	void exitErrorCreator(MalicParser.ErrorCreatorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code arrayCreator}
	 * labeled alternative in {@link MalicParser#creator}.
	 * @param ctx the parse tree
	 */
	void enterArrayCreator(MalicParser.ArrayCreatorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code arrayCreator}
	 * labeled alternative in {@link MalicParser#creator}.
	 * @param ctx the parse tree
	 */
	void exitArrayCreator(MalicParser.ArrayCreatorContext ctx);
	/**
	 * Enter a parse tree produced by the {@code nonarrayCreator}
	 * labeled alternative in {@link MalicParser#creator}.
	 * @param ctx the parse tree
	 */
	void enterNonarrayCreator(MalicParser.NonarrayCreatorContext ctx);
	/**
	 * Exit a parse tree produced by the {@code nonarrayCreator}
	 * labeled alternative in {@link MalicParser#creator}.
	 * @param ctx the parse tree
	 */
	void exitNonarrayCreator(MalicParser.NonarrayCreatorContext ctx);
}
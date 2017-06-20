package com.mercy.compiler.BackEnd;

import com.mercy.compiler.IR.*;

/**
 * Created by mercy on 17-6-10.
 */
public interface IRVisitor<T> {
    public T visit(Addr ir);
    public T visit(Assign ir);
    public T visit(Binary ir);
    public T visit(Call ir);
    public T visit(CJump ir);
    public T visit(IntConst ir);
    public T visit(Jump ir);
    public T visit(Label ir);
    public T visit(Mem ir);
    public T visit(Return ir);
    public T visit(StrConst ir);
    public T visit(Unary ir);
    public T visit(Var ir);
}

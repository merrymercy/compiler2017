package com.mercy.compiler.IR;

import com.mercy.compiler.BackEnd.IRVisitor;

/**
 * Created by mercy on 17-3-30.
 */
abstract public class Expr extends IR {
    @Override
    abstract public <T> T accept(IRVisitor<T> emitter);
}

package com.mercy.compiler.IR;

/**
 * Created by mercy on 17-3-30.
 */
public class Assign extends IR {
    Expr left, right;

    public Assign(Expr left, Expr right) {
        this.left = left;
        this.right = right;
    }
}

package com.mercy.compiler.Entity;

/**
 * Created by mercy on 17-3-20.
 */
public interface EntityVisitor<T> {
    public T visit(VariableEntity var);
    public T visit(FunctionEntity func);
    public T visit(ConstantEntity constant);
    public T visit(ClassEntity c);
}

package com.mercy.compiler.INS.Operand;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by mercy on 17-5-4.
 */
public class Immediate extends Operand {
    enum Type {
        LABEL, INTEGER
    }

    private int value;
    private String label;
    private Type type;


    public Immediate(int value) {
        this.value = value;
        this.type = Type.INTEGER;
    }
    public Immediate(String label) {
        this.label = label;
        this.type = Type.LABEL;
    }

    public int value() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public Type type() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public Operand replace(Operand from, Operand to) {
        return this;
    }

    @Override
    public boolean isDirect() {
        return true;
    }

    @Override
    public Set<Reference> getAllRef() {
        return new HashSet<>();
    }

    @Override
    public String toNASM() {
        if (type == Type.INTEGER)
            return Integer.toString(value);
        else
            return label;
    }

    @Override
    public String toString() {
        if (type == Type.INTEGER)
            return Integer.toString(value);
        else
            return label;
    }
}

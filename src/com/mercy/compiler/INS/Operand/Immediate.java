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

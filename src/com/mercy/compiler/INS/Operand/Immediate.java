package com.mercy.compiler.INS.Operand;

import com.mercy.compiler.INS.Label;
import com.mercy.compiler.Utility.InternalError;

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

    public String label() {
        return label;
    }
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public int hashCode() {
        switch (type) {
            case INTEGER: return value;
            case LABEL:   return label.hashCode();
            default: throw new InternalError("invalid type of immediate");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Immediate) {
            switch (((Immediate)o).type()) {
                case LABEL:   return label.equals(((Immediate) o).label());
                case INTEGER: return value == ((Immediate) o).value();
                default: throw new InternalError("invalid type of immediate");
            }
        }
        return false;
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
    public boolean isConstInt() {
        return type == Type.INTEGER;
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

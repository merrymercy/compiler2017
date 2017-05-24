package com.mercy.compiler.INS.Operand;

import com.mercy.compiler.Entity.Entity;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by mercy on 17-5-4.
 */
public class Address extends Operand {
    Entity entity;
    Operand base = null, index = null;  // however, these two member can only be register/reference, cannot be address!
    int mul = 1, add = 0;

    boolean showSize = true;

    Operand baseNasm, indexNasm;

    public Address(Operand base) {
        this.base = base;
    }

    public Address(Operand base, Operand index, int mul, int add) {
        this.base = base;
        this.index = index;
        this.mul = mul;
        this.add = add;
    }

    @Override
    public Set<Reference> getAllRef() {
        Set<Reference> ret = new HashSet<>();
        if (base != null)
            ret.addAll(base.getAllRef());
        if (index != null) {
            ret.addAll(index.getAllRef());
        }
        return ret;
    }

    /*
     * getter and setter
     */
    public Operand base() {
        return base;
    }

    public Operand index() {
        return index;
    }

    public int mul() {
        return mul;
    }

    public int add() {
        return add;
    }

    public Entity entity() {
        return entity;
    }

    public Operand baseNasm() {
        return baseNasm != null ? baseNasm : base;
    }

    public Operand indexNasm() {
        return indexNasm != null ? indexNasm : index;
    }

    public void setBaseNasm(Operand baseNasm) {
        this.baseNasm = baseNasm;
    }

    public void setIndexNasm(Operand indexNasm) {
        this.indexNasm = indexNasm;
    }

    public void setShowSize(boolean showSize) {
        this.showSize = showSize;
    }

    public boolean baseOnly() {
        return base != null && index == null && mul == 1 && add == 0;
    }


    @Override
    public boolean isDirect() {
        if (base == null) {
            return index().isRegister();
        } if (index == null) {
            return base.isRegister();
        } else
            return index.isRegister() && base().isRegister();
    }

    @Override
    public String toNASM() {
        String ret = showSize ? "qword" + " [" : "[";
        String gap = "";
        if (base != null) {
            ret += gap + baseNasm().toNASM();
            gap = " + ";
        }
        if (index != null) {
            ret += gap + indexNasm().toNASM();
            gap = " + ";
            if (mul != 1) {
                ret += " * " + mul;
            }
        }
        if (add != 0) {
            ret += gap + add;
        }

        return ret + "]";
    }

    @Override
    public String toString() {
        String str = "";

        String gap = "";
        if (base != null) {
            str += gap + base;
            gap = " + ";
        }
        if (index != null) {
            str += gap + index;
            gap = " + ";
            if (mul != 1) {
                str += " * " + mul;
            }
        }
        if (add != 0) {
            str += gap + add;
        }
        return "[" + str + "]";
    }
}

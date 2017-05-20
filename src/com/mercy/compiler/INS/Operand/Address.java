package com.mercy.compiler.INS.Operand;

import com.mercy.compiler.Entity.Entity;
import com.mercy.compiler.Utility.InternalError;

/**
 * Created by mercy on 17-5-4.
 */
public class Address extends Operand {
    public enum Type {
        ENTITY, BASE_OFFSET
    }

    Entity entity;
    Operand base = null, index = null;  // however, these two member can only be register/reference, cannot be address!
    int mul = 1, add = 0;
    Type type;

    boolean showSize = true;

    Operand baseNasm, indexNasm;

    public Address(Entity entity) {
        this.entity = entity;
        this.type = Type.ENTITY;
    }

    public Address(Operand base) {
        this.base = base;
        this.type = Type.BASE_OFFSET;
    }

    public Address(Operand base, Operand index, int mul, int add) {
        this.base = base;
        this.index = index;
        this.mul = mul;
        this.add = add;
        this.type = Type.BASE_OFFSET;
    }

    /*
     * getter and setter
     */

    public Type type() {
        return type;
    }

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

    @Override
    public String toNASM() {
        String ret = showSize ? "qword" + " [" : "[";
        String gap = "";
        switch (type) {
            case ENTITY:
                return entity.reference().toNASM();
            case BASE_OFFSET:
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
            default:
                throw new InternalError("invalid type " + type);
        }
    }

    @Override
    public String toString() {
        String str = "";
        switch (type) {
            case ENTITY:
                str = entity.name(); break;
            case BASE_OFFSET:
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
                break;
            default:
                throw new InternalError("invalid type " + type);
        }
        return "[" + str + "]";
    }
}

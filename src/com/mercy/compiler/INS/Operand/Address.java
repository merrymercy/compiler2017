package com.mercy.compiler.INS.Operand;

import com.mercy.compiler.Entity.Entity;
import com.mercy.compiler.Utility.InternalError;

/**
 * Created by mercy on 17-5-4.
 */
public class Address extends Operand {
    public enum Type {
        ENTITY, BASE_ONLY, BASE_INDEX, BASE_INDEX_MUL
    }

    Entity entity;
    Operand base, index;  // however, these two member can only be register/reference, cannot be address!
    int mul, add;
    Type type;

    boolean showSize = true;

    Operand baseNasm, indexNasm;

    public Address(Entity entity) {
        this.entity = entity;
        this.type = Type.ENTITY;
    }

    public Address(Operand base) {
        this.base = base;
        this.type = Type.BASE_ONLY;
    }

    public Address(Operand base, Operand index) {
        this.base = base;
        this.index = index;
        this.mul = mul;
        this.type = Type.BASE_INDEX;
    }


    public Address(Operand base, Operand index, int mul) {
        this.base = base;
        this.index = index;
        this.mul = mul;
        this.type = Type.BASE_INDEX_MUL;
    }

    public Address(Operand base, Operand index, int mul, int add) {
        this.base = base;
        this.index = index;
        this.mul = mul;
        this.add = add;
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
        String sizePrefix = showSize ? "qword" : "";
        switch (type) {
            case ENTITY:
                return entity.reference().toNASM();
            case BASE_ONLY:
                return sizePrefix + " [" + baseNasm().toNASM() + "]";
            case BASE_INDEX_MUL:
                return sizePrefix + " [" + baseNasm().toNASM() + " + " + indexNasm().toNASM() + " * " + mul + "]";
            default:
                throw new InternalError("invalid type " + type);
        }
    }

    @Override
    public String toString() {
        String str;
        switch (type) {
            case ENTITY:
                str = entity.name(); break;
            case BASE_ONLY:
                str = base.toString(); break;
            case BASE_INDEX_MUL:
                str = base.toString() + " + " + index.toString() + " * " + mul; break;
            default:
                throw new InternalError("invalid type " + type);
        }
        return "[" + str + "]";
    }
}

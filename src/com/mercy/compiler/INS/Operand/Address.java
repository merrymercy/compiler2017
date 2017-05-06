package com.mercy.compiler.INS.Operand;

import com.mercy.compiler.Entity.Entity;
import com.mercy.compiler.Utility.InternalError;

import java.util.List;

/**
 * Created by mercy on 17-5-4.
 */
public class Address extends Operand {
    public enum Type {
        ENTITY, OPERAND
    }

    Entity entity;
    Register base;
    Operand operand;
    Type type;

    public Address(Entity entity) {
        this.entity = entity;
        this.type = Type.ENTITY;
    }

    public Address(Operand operand) {
        this.operand = operand;
        this.type = Type.OPERAND;
    }

    public Type type() {
        return type;
    }

    public Operand operand() {
        return operand;
    }

    public Entity entity() {
        return entity;
    }

    @Override
    public String toNASM() {
        String addr;
        switch (type) {
            case ENTITY:
                return entity.reference().toNASM();
            case OPERAND:
                return "[" + operand.toNASM() + "]";
            default:
                throw new InternalError("invalid type " + type);
        }
    }

    @Override
    public String toString() {
        String name;
        switch (type) {
            case ENTITY:
                name = entity.name(); break;
            case OPERAND:
                name = operand.toString(); break;
            default:
                throw new InternalError("invalid type " + type);
        }
        return "[" + name + "]";
    }
}

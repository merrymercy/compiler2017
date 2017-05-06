package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.Entity.FunctionEntity;
import com.mercy.compiler.INS.Operand.Operand;
import com.mercy.compiler.INS.Operand.Reference;

import java.util.List;

/**
 * Created by mercy on 17-4-25.
 */
public class Call extends Instruction {
    FunctionEntity entity;
    List<Operand> operands;
    Reference ret;

    public Call(FunctionEntity entity, List<Operand> operands) {
        this.entity = entity;
        this.operands = operands;
        this.ret = null;
    }

    public FunctionEntity entity() {
        return entity;
    }

    public List<Operand> operands() {
        return operands;
    }

    public Reference ret() {
        return ret;
    }

    public void setRet(Reference ret) {
        this.ret = ret;
    }

    @Override
    public void accept(Translator translator) {
        translator.visit(this);
    }

    @Override
    public String toString() {
        String args = "";
        for (Operand operand : operands) {
            args += ", " + operand;
        }
        return ret + " = call " + entity.asmName() + args;
    }
}

package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.Entity.FunctionEntity;
import com.mercy.compiler.INS.Operand.Operand;
import com.mercy.compiler.INS.Operand.Reference;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by mercy on 17-4-25.
 */
public class Call extends Instruction {
    FunctionEntity entity;
    List<Operand> operands;
    Operand ret;

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

    public Operand ret() {
        return ret;
    }

    public void setRet(Reference ret) {
        this.ret = ret;
    }

    @Override
    public void replaceUse(Reference from, Reference to) {
        List<Operand> newOperands = new LinkedList<>();
        for (Operand operand : operands) {
            newOperands.add(operand.replace(from, to));
        }
        operands = newOperands;
    }

    @Override
    public void replaceDef(Reference from, Reference to) {
        if (ret != null)
            ret = ret.replace(from, to);
    }

    @Override
    public void calcDefAndUse() {
        if (ret != null)
            def.addAll(ret.getAllRef());
        for (Operand operand : operands) {
            use.addAll(operand.getAllRef());
        }
        allref.addAll(use);
        allref.addAll(def);
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

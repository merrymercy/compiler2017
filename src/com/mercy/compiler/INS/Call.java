package com.mercy.compiler.INS;

import com.mercy.compiler.BackEnd.Translator;
import com.mercy.compiler.Entity.FunctionEntity;
import com.mercy.compiler.INS.Operand.Operand;
import com.mercy.compiler.INS.Operand.Reference;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by mercy on 17-4-25.
 */
public class Call extends Instruction {
    private FunctionEntity entity;
    private List<Operand> operands;
    private Operand ret;
    private Set<Reference> callorsave;
    private Set<Reference> usedParameterRegister;

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

    public Set<Reference> callorsave() {
        return callorsave;
    }

    public void setCallorsave(Set<Reference> callorsave) {
        this.callorsave = callorsave;
    }

    public Set<Reference> usedParameterRegister() {
        return usedParameterRegister;
    }

    public void setUsedParameterRegister(Set<Reference> usedParameterRegister) {
        this.usedParameterRegister = usedParameterRegister;
    }

    @Override
    public void replaceUse(Reference from, Reference to) {
        List<Operand> newOperands = new LinkedList<>();
        for (Operand operand : operands) {
            newOperands.add(operand.replace(from, to));
        }
        operands = newOperands;

        Set<Reference> newParaReg = new HashSet<>();
        for (Reference reference : usedParameterRegister) {
            newParaReg.add((Reference) reference.replace(from, to));
        }
        usedParameterRegister = newParaReg;
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
        if (callorsave != null)
            def.addAll(callorsave);
        for (Operand operand : operands) {
            use.addAll(operand.getAllRef());
        }
        if (usedParameterRegister != null)
            for (Reference parareg : usedParameterRegister) {
                use.addAll(parareg.getAllRef());
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

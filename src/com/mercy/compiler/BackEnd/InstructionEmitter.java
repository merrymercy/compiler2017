package com.mercy.compiler.BackEnd;

import com.mercy.compiler.Entity.FunctionEntity;
import com.mercy.compiler.Entity.Scope;
import com.mercy.compiler.INS.Instruction;
import com.mercy.compiler.IR.IR;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by mercy on 17-4-25.
 */
public class InstructionEmitter {
    List<FunctionEntity> functionEntities;
    Scope globalScope;
    List<IR> globalInitializer;

    public InstructionEmitter(IRBuilder irBuilder) {
        this.globalScope = irBuilder.globalScope();
        this.functionEntities = irBuilder.functionEntities();
        this.globalInitializer = irBuilder.globalInitializer();
    }

    public void emit() {
        for (FunctionEntity functionEntity : functionEntities) {
            functionEntity.setINS(emitFunction(functionEntity));
        }
    }

    public List<Instruction> emitFunction(FunctionEntity entity) {
        List<Instruction> ins = new LinkedList<>();
        for (IR ir : entity.IR()) {
            ir.emit(ins);
        }
        return null;
    }

}

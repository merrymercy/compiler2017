package com.mercy.compiler.BackEnd;

import com.mercy.Option;
import com.mercy.compiler.Entity.FunctionEntity;
import com.mercy.compiler.Entity.ParameterEntity;
import com.mercy.compiler.Entity.VariableEntity;
import com.mercy.compiler.INS.Instruction;
import com.mercy.compiler.INS.Operand.Reference;
import com.mercy.compiler.INS.Operand.Register;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static com.mercy.Option.REG_SIZE;
import static java.lang.System.err;

/**
 * Created by mercy on 17-5-24.
 */
public class NaiveAllocator  {
    List<FunctionEntity> functionEntities;

    private Register rax, rcx, rdx, rbx, rsp, rbp, rsi, rdi;
    private List<Register> registers;
    private List<Register> paraRegister;
    public NaiveAllocator(InstructionEmitter emitter, RegisterConfig registerConfig) {
        functionEntities = emitter.functionEntities();

        // load registers
        registers = registerConfig.registers();
        paraRegister = registerConfig.paraRegister();

        rax = registers.get(0); rbx = registers.get(1);
        rcx = registers.get(2); rdx = registers.get(3);
        rsi = registers.get(4); rdi = registers.get(5);
        rbp = registers.get(6); rsp = registers.get(7);
    }

    public void allocate() {
        for (FunctionEntity functionEntity : functionEntities) {
            allocateFunction(functionEntity);
        }
    }

    private void allocateFunction(FunctionEntity entity){
        if (entity.isInlined())
            return;

        /*************************************************/
        // count times and sort
        Set<Reference> allRef = entity.allReference();
        for (Instruction ins : entity.INS()) {
            for (Reference ref : ins.use()) {
                ref.addRefTime();
                allRef.add(ref);
            }
            for (Reference ref : ins.def()) {
                ref.addRefTime();
                allRef.add(ref);
            }
        }
        List<Reference> tosort = new ArrayList<>(allRef);
        tosort.sort(new Comparator<Reference>() {
            @Override
            public int compare(Reference reference, Reference t1) {
                return -(reference.refTimes() - t1.refTimes());
            }
        });

        // allocate register
        int[] toAllocate = {1, 12, 13, 14, 15};

        if (Option.printNaiveAllocatorInfo)
            err.println("naive allocator : " + entity.name());
        for (int i = 0; i < tosort.size(); i++) {
            if (i < toAllocate.length) {
                Reference ref = tosort.get(i);
                if (ref.type() == Reference.Type.GLOBAL) // skip global variable
                    continue;

                ref.setRegister(registers.get(toAllocate[i]));
                entity.regUsed().add(registers.get(toAllocate[i]));

                if (Option.printNaiveAllocatorInfo)
                    err.printf("%-8s -> %s\n", ref.name(), ref.reg());
            }
        }
        entity.regUsed().add(rbp);
        /*************************************************/

        // locate parameters
        int lvarBase, stackBase, savedTempBase;
        lvarBase = 0;
        List<ParameterEntity> params = entity.params();
        for (int i = 0; i < params.size(); i++) {
            ParameterEntity par = params.get(i);
            Reference ref = par.reference();
            if (i < paraRegister.size()) {
                lvarBase += par.type().size();
                par.reference().setOffset(-lvarBase, rbp);
            } else {
                ref.alias = par.source();
            }
        }

        // locate frame
        stackBase = lvarBase;
        stackBase += entity.scope().locateLocalVariable(lvarBase, Option.STACK_VAR_ALIGNMENT_SIZE);
        for (VariableEntity var : entity.scope().allLocalVariables()) {
            var.reference().setOffset(-var.offset(), rbp);
        }

        // locate tmpStack
        List<Reference> tmpStack = entity.tmpStack();
        savedTempBase = stackBase;
        for (int i = 0; i < tmpStack.size(); i++) {
            if (tmpStack.get(i).isUnknown()) {
                savedTempBase += REG_SIZE;
                tmpStack.get(i).setOffset(-savedTempBase, rbp);
            }
        }

        entity.setLocalVariableOffset(savedTempBase);
    }

}

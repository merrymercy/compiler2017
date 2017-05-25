package com.mercy.compiler.BackEnd;

import com.mercy.Option;
import com.mercy.compiler.Entity.FunctionEntity;
import com.mercy.compiler.INS.Instruction;
import com.mercy.compiler.INS.Operand.Reference;
import com.mercy.compiler.INS.Operand.Register;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

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
            allocateTmpStack(functionEntity);
        }
    }

    public static final class MutableInteger{
        private int val;
        public MutableInteger(int val){
            this.val = val;
        }
        public int get(){
            return this.val;
        }
        public void set(int val){
            this.val = val;
        }

        @Override
        public String toString() {
            return Integer.toString(val);
        }
    }

    private void allocateTmpStack(FunctionEntity entity){
        if (Option.enableInlineFunction && entity.canbeInlined())
            return;

        /*************************************************/
        // count times
        Set<Reference> allRef = entity.allReference();
        for (Instruction ins : entity.ins()) {
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
        int[] toAllocate = {12, 13, 14, 15, 1, 10, 11};

        if (Option.printNaiveAllocatorInfo)
            err.println("naive allocator : " + entity.name());
        for (int i = 0; i < tosort.size(); i++) {
            if (i < toAllocate.length) {
                Reference ref = tosort.get(i);

                if (ref.type() == Reference.Type.GLOBAL)
                    continue;

                ref.setRegister(registers.get(toAllocate[i]));
                entity.regUsed().add(registers.get(toAllocate[i]));
                if (Option.printNaiveAllocatorInfo)
                    err.printf("%-8s -> %s\n", ref.name(), ref.reg());
            }
        }
        entity.regUsed().add(rbp);
    }

}

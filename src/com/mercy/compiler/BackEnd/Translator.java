package com.mercy.compiler.BackEnd;

import com.mercy.compiler.Entity.*;
import com.mercy.compiler.INS.*;
import com.mercy.compiler.INS.Operand.Immediate;
import com.mercy.compiler.INS.Operand.Operand;
import com.mercy.compiler.INS.Operand.Reference;
import com.mercy.compiler.INS.Operand.Register;
import com.mercy.compiler.IR.IR;

import java.io.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by mercy on 17-5-4.
 */
public class Translator {
    public final int VIRTUAL_STACK_REG_SIZE = 8;
    public final int ALIGNMENT = 4;
    public final String END_SUFFIX = "_ret";

    private List<FunctionEntity> functionEntities;
    private Scope globalScope;
    private List<IR> globalInitializer;

    private FunctionEntity currentFunction;

    private Register rax, rcx, rdx, rbx, rsp, rbp, rsi, rdi;
    private List<Register> registers = new ArrayList<>();
    private List<Register> paraRegister = new ArrayList<>();
    private boolean [] regUsed = new boolean[16];
    private List<String> asm = new LinkedList<>();

    public Translator(InstructionEmitter emitter) {
        functionEntities = emitter.functionEntities();
        globalScope = emitter.globalScope();
        globalInitializer = emitter.globalInitializer();

        // init registers
        rax = new Register("rax"); registers.add(rax);
        rcx = new Register("rcx"); registers.add(rcx);
        rdx = new Register("rdx"); registers.add(rdx);
        rbx = new Register("rbx"); registers.add(rbx);
        rsp = new Register("rsp"); registers.add(rsp);
        rbp = new Register("rbp"); registers.add(rbp);
        rsi = new Register("rsi"); registers.add(rsi);
        rdi = new Register("rdi"); registers.add(rdi);

        for (int i = 8; i < 16; i++) {
            registers.add(new Register("r" + i));
        }

        // set registers for parameter
        paraRegister.add(rdi); paraRegister.add(rsi);
        paraRegister.add(rdx); paraRegister.add(rcx);
        paraRegister.add(registers.get(8));
        paraRegister.add(registers.get(9));
    }

    public List<String> translate() {
        // set reference for global variable
        int stringCounter = 1;

        // add extern
        add("global main");
        add("extern printf");
        add("extern puts");
        add("extern scanf");
        add("extern sprintf");
        add("extern strlen");
        add("extern strcpy");
        add("extern malloc");
        add("");

        // add data section
        add("section .data");
        for (Entity entity : globalScope.entities().values()) {
            //System.out.println(entity.name());
            if (entity instanceof VariableEntity) {
                entity.setReference(new Reference(entity.name()));
            } else if (entity instanceof  StringConstantEntity) {
                String name = "__STR_CONST_" + stringCounter;
                String value = ((StringConstantEntity) entity).strValue();
                entity.setReference(new Reference(name));

                add("dd " + value.length());
                addLabel(name);

                StringBuffer sb = new StringBuffer();
                sb.append("db");
                for (int i = 0; i < value.length(); i++) {
                    sb.append(" ");
                    Integer x = new Integer(value.charAt(i));
                    sb.append(x);
                    sb.append(",");
                }
                sb.append(" 0");
                add(sb.toString());
            }
        }
        add("");

        // translate functions
        add("section .text");
        for (FunctionEntity entity : functionEntities) {
            currentFunction = entity;
            int frameSize = locateFrame(entity);
            translateFunction(entity, frameSize);
        }

        pasteLibfunction();
        return asm;
    }

    // virtual stack
    // local variable
    // parameter
    // saved reg
    // ----------------- <- bp
    // old bp
    // return address

    public int locateFrame(FunctionEntity entity) {
        int savedRegBase, paraBase, lvarBase, stackBase, total;

        // locate callee-save register
        savedRegBase = 4;
        paraBase = savedRegBase;

        // locate parameter
        lvarBase = paraBase;
        for (ParameterEntity parameterEntity : entity.params()) {
            lvarBase += parameterEntity.type().size();
            parameterEntity.setReference(new Reference(lvarBase, rbp));
        }

        // locate local variable
        stackBase = lvarBase;
        stackBase += entity.scope().locateLocalVariable(lvarBase, ALIGNMENT);
        for (VariableEntity variableEntity : entity.scope().allLocalVariables()) {
            variableEntity.setReference(new Reference(variableEntity.offset(),
                    rbp));
        }

        Register [] toAllocate = {R(12), R(13), R(14), R(15)};

        // locate tmpStack
        total = stackBase;
        List<Reference> tmpStack = entity.tmpStack();
        for (int i = 0; i < tmpStack.size(); i++) {
            if (i < toAllocate.length) {
                tmpStack.get(i).setRegister(toAllocate[i]);
            } else {
                total += VIRTUAL_STACK_REG_SIZE;
                tmpStack.get(i).setOffset(total, rbp);
            }
        }

        return total;
    }

    public void translateFunction(FunctionEntity entity, int frameSize) {
        addLabel(entity.name());
        add("push", rbp());
        add("mov", rbp(), rsp());
        add("sub", rsp(), new Immediate((frameSize)));
        add("");


        for (Instruction instruction : entity.INS()) {
            instruction.accept(this);
        }

        addLabel(entity.name() + END_SUFFIX);
        add("add", rsp(), new Immediate((frameSize)));
        add("pop", rbp());
        add("ret");
    }

    private void add(String op, Operand l, Operand r) {
        asm.add("\t" + op + " " + l.toNASM() + ", " + r.toNASM());
    }
    private void add(String op, Operand l) {
        asm.add("\t" + op + " " + l.toNASM());
    }
    private void add(String op) {
        asm.add("\t" + op);
    }
    private void addLabel(String name) {
        asm.add(name + ":");
    }

    /*
     * INS visitor
     */
    public void visitBin(Bin ins) {
        Operand left, right;
        String name;
        left = ins.left();
        right= ins.right();
        name = ins.name();
        if (left.isRegister()) {
            if (right.isRegister()) {  // add regl, regr
                add(name, left, right);
            } else {                   // add regl, memr
                add(name, left, right);
            }
        } else {
            if (right.isRegister()) {   // add meml, regr
                add("mov", rax(), left);
                add(name, rax(), right);
                add("mov", left, rax());
            } else {                    // add meml, memr
                add("mov", rax(), left);
                add(name, rax(), right);
                add("mov", left, rax());
            }
        }
    }

    public void visit(Add ins) {
        visitBin(ins);
    }
    public void visit(Sub ins) {
        visitBin(ins);
    }
    public void visit(Mul ins) {
        visitBin(ins);
    }

    public void visit(Div ins) {
        Operand left, right;
        left = ins.left();
        right= ins.right();
        add("mov", rax(), left);
        add("mov", rcx(), right);
        add("cdq");
        add("idiv", rcx());
        add("mov", left, rax());
    }

    public void visit(Mod ins) {
        Operand left, right;
        left = ins.left();
        right= ins.right();
        add("mov", rax(), left);
        add("mov", rcx(), right);
        add("cdq");
        add("idiv", rcx());
        add("mov", left, rdx());
    }

    public void visit(Neg ins) {
        add("neg", ins.operand());
    }

    public void visit(Not ins) {
        add("not", ins.operand());
    }

    public void visit(And ins) {
        visitBin(ins);
    }
    public void visit(Or ins) {
        visitBin(ins);
    }
    public void visit(Sal ins) {
        visitBin(ins);
    }
    public void visit(Sar ins) {
        visitBin(ins);
    }
    public void visit(Xor ins) {
        visitBin(ins);
    }

    public void visit(Cmp ins) {
        Operand left, right;
        left = ins.left();
        right= ins.right();
        add("cmp", left, right);
        String set = "";
        switch (ins.operator()) {
            case EQ: set = "sete";  break;
            case NE: set = "setne"; break;
            case GE: set = "setge"; break;
            case GT: set = "setg";  break;
            case LE: set = "setle"; break;
            case LT: set = "setl";  break;
        }
        add(set + " al");
        add("movzx " +  "rax" + ", " + "al");
        add("mov", left, rax());
    }

    public void visit(Move ins) {
        add("mov", ins.dest(), ins.src());
    }

    public void visit(Call ins) {
        List<Operand> operands = ins.operands();
        for (int i = 0; i < operands.size(); i++) {
            if (i < paraRegister.size()) {
                add("mov", paraRegister.get(i), operands.get(i));
            } else {
                if (operands.get(i).isRegister()) {
                    add("push", operands.get(i));
                } else {
                    add("mov", rax(), operands.get(i));
                    add("push", rax());
                }
            }
        }
        add("call " + ins.entity().name());
        add("mov", ins.ret(), rax());
    }

    public void visit(Return ins) {
        add("mov", rax(), ins.ret());
        add("jmp " + currentFunction.name() + END_SUFFIX);
    }

    public void visit(CJump ins) {
        if (ins.cond().isRegister()) {
            add("test", ins.cond(), ins.cond());
        } else {
            add("mov", rax(), ins.cond());
            add("test", rax(), rax());
        }
        add("jnz " + ins.trueLabel().name());
        add("jmp " + ins.falseLabel().name());
    }

    public void visit(Jmp ins) {
        add("jmp " + ins.dest().name());
    }

    public void visit(Label ins) {
        addLabel(ins.name());
    }

    public void visit(Comment ins) {
        add(";" + ins);
    }

    /*
     * register getter
     */
    private Register rax() {
        regUsed[0] = true; return rax;
    }
    private Register rcx() {
        regUsed[1] = true; return rcx;
    }
    private Register rdx() {
        regUsed[2] = true; return rdx;
    }
    private Register rbx() {
        regUsed[3] = true; return rbx;
    }
    private Register rsp() {
        regUsed[4] = true; return rsp;
    }
    private Register rbp() {
        regUsed[5] = true; return rbp;
    }
    private Register rsi() {
        regUsed[6] = true; return rsi;
    }
    private Register rdi() {
        regUsed[7] = true; return rdi;
    }
    private Register R(int i) {
        regUsed[i] = true; return registers.get(i);
    }

    /********** DEBUG TOOL **********/
    private void printFunction(PrintStream out, FunctionEntity entity) {
        out.println("========== OFFSET ==========");
        for (ParameterEntity parameterEntity : entity.params()) {
            out.println(parameterEntity.name() + " " + parameterEntity.offset());
        }

        for (VariableEntity variableEntity : entity.allLocalVariables()) {
            out.println(variableEntity.name() + " " + variableEntity.offset());
        }
    }

    public void printSelf(PrintStream out) {
        for (FunctionEntity functionEntity : functionEntities) {
            printFunction(out, functionEntity);
        }
        out.println("========== NASM ==========");
        for (String s : asm) {
            out.println(s);
        }
    }

    private void pasteLibfunction() {
        asm.add("\n;========== LIB BEGIN ==========");
        File f = new File("lib.s");
        try {
            //FileWriter fout = new FileWriter(f);
            //fout.write("aha");
            //fout.close();
            BufferedReader fin = new BufferedReader(new FileReader(f));
            String line;
            while((line = fin.readLine()) != null)
                asm.add(line);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


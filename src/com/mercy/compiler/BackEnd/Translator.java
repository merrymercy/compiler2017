package com.mercy.compiler.BackEnd;

import com.mercy.Option;
import com.mercy.compiler.Entity.*;
import com.mercy.compiler.INS.*;
import com.mercy.compiler.INS.Operand.*;
import com.mercy.compiler.IR.IR;
import com.mercy.compiler.Utility.InternalError;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by mercy on 17-5-4.
 */
public class Translator {
    public static int VIRTUAL_STACK_REG_SIZE = 8;
    public static int ALIGNMENT = 4;
    public static String END_SUFFIX = "_ret";
    public static String GLOBAL_PREFIX = "__global_";
    public static String FUNC_SUFFIX = "__func";


    private List<FunctionEntity> functionEntities;
    private Scope globalScope;
    private List<IR> globalInitializer;

    private FunctionEntity currentFunction;

    private Register rax, rcx, rdx, rbx, rsp, rbp, rsi, rdi;
    private List<Register> registers = new ArrayList<>();
    private List<Register> paraRegister = new ArrayList<>();
    private boolean [] regUsed = new boolean[16];
    private boolean [] isCalleeSave = new boolean[16];
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

        // set callee save
        for (int i = 12; i < 16; i++)
            isCalleeSave[i] = true;
        isCalleeSave[3] = isCalleeSave[5] = true;
    }

    public List<String> translate() {
        // set reference for global variable
        int stringCounter = 1;

        // add extern
        add("global main");
        add("extern printf, scanf, puts, gets, sprintf, sscanf, getchar, strlen, strcmp, strcpy, strncpy, malloc");
        add("");

        // add data section
        add("section .data");
        for (Entity entity : globalScope.entities().values()) {
            //System.out.println(entity.name());
            if (entity instanceof VariableEntity) {
                entity.setReference(new Reference(entity.name()));
                addLabel(GLOBAL_PREFIX + entity.name());
                add("dq 0");
            } else if (entity instanceof  StringConstantEntity) {
                String name = "__STR_CONST_" + stringCounter++;
                String value = ((StringConstantEntity) entity).strValue();
                entity.setReference(new Reference(name, true));

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
            if (Option.enableInlineFunction && entity.canbeInlined())
                continue;
            // init
            for (int i = 0; i < regUsed.length; i++)
                regUsed[i] = false;
            currentFunction = entity;
            int frameSize = locateFrame(entity);
            int savedRegNum = translateFunction(entity, frameSize);
            add("");
        }

        pasteLibfunction();
        return asm;
    }

    // virtual stack
    // local variable
    // parameter
    // ----------------- <- bp
    // saved regs
    // return address

    public int locateFrame(FunctionEntity entity) {
        int savedRegBase, paraBase, lvarBase, stackBase, total;

        paraBase = 0;

        // locate parameter
        lvarBase = paraBase;
        List<ParameterEntity> params = entity.params();
        for (int i = 0; i < params.size(); i++) {
            ParameterEntity par = params.get(i);
            if (i < paraRegister.size()) {
                lvarBase += par.type().size();
                par.setOffset(lvarBase);
                par.setReference(new Reference(lvarBase, rbp()));
            }
        }

        // locate local variable
        stackBase = lvarBase;
        stackBase += entity.scope().locateLocalVariable(lvarBase, ALIGNMENT);
        for (VariableEntity variableEntity : entity.scope().allLocalVariables()) {
            variableEntity.setReference(new Reference(variableEntity.offset(),
                    rbp()));
        }

        int[] toAllocate = {12, 13, 14, 15, 3};//, 10, 11};

        // locate tmpStack
        savedRegBase = stackBase;
        List<Reference> tmpStack = entity.tmpStack();
        for (int i = 0; i < tmpStack.size(); i++) {
            if (i < toAllocate.length) {
                tmpStack.get(i).setRegister(registers.get(toAllocate[i]));
                regUsed[toAllocate[i]] = true;
            } else {
                savedRegBase += VIRTUAL_STACK_REG_SIZE;
                tmpStack.get(i).setOffset(savedRegBase, rbp());
            }
        }

        // locate outside parameters
        int savedRegNum = 0;
        for (int i = 0; i < regUsed.length; i++) {
            if (regUsed[i] && isCalleeSave[i])
                savedRegNum++;
        }
        // locate callee-save register
        total = savedRegBase;

        int base = savedRegNum * VIRTUAL_STACK_REG_SIZE + VIRTUAL_STACK_REG_SIZE;  // + return address
        for (int i = 0; i < params.size(); i++) {
            ParameterEntity par = params.get(i);
            if (i >= paraRegister.size()) {
                par.setOffset(-base);
                par.setReference(new Reference(-base, rbp()));
                base += par.type().size();
            }
        }
        return total;
    }

    public int translateFunction(FunctionEntity entity, int frameSize) {
        addLabel(entity.asmName());

        int startPos = asm.size();

        /***** body *****/
        for (Instruction instruction : entity.INS()) {
            instruction.accept(this);
        }

        /***** prologue *****/
        List<String> backup = asm, prologue;
        asm = new LinkedList<>();
        // push and pop callee-save registers
        int calleeSaveRegSize = 0;
        for (int i = 0; i < regUsed.length; i++) {
            if (regUsed[i] && isCalleeSave[i]) {
                add("push", registers.get(i));
                calleeSaveRegSize += VIRTUAL_STACK_REG_SIZE;
            }
        }
        // set rbp and rsp
        if (regUsed[5])
            add("mov", rbp(), rsp());
        frameSize += (16 - (frameSize + 8 + calleeSaveRegSize) % 16) % 16;
        if (entity.calls().size() != 0)   // leaf function optimization
            add("sub", rsp(), new Immediate((frameSize)));
        // store parameters
        List<ParameterEntity> params = entity.params();
        for (int i = 0; i < params.size(); i++) {
            if (i < paraRegister.size()) {
                add("mov", params.get(i).reference(), paraRegister.get(i));
            }
        }
        add("");

        // insert prologue
        prologue = asm;
        asm = backup;
        asm.addAll(startPos, prologue);

        /***** epilogue *****/
        addLabel(entity.endLabelINS().name());
        if (entity.calls().size() != 0)   // leaf function optimization
            add("add", rsp(), new Immediate((frameSize)));
        int savedRegNum = 0;
        for (int i = regUsed.length - 1; i >= 0; i--) {
            if (regUsed[i] && isCalleeSave[i]) {
                add("pop", registers.get(i));
                savedRegNum++;
            }
        }
        add("ret");
        return savedRegNum;
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
    private void addJump(String name) {
        asm.add("\tjmp" + " " + name);
    }
    private void addComment(String comment) {
        asm.add("\t;" + comment);
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
            } else {
                if (right instanceof Address) {    // add regl, [memr]
                    add("mov", rax(), ((Address) right).base());
                    add(name, left, new Address(rax()));
                } else {                           // add regl, memr
                    add(name, left, right);
                }
            }
        } else {
            if (right.isRegister()) {   // add meml, regr
                add("mov", rax(), left);
                add(name, rax(), right);
                add("movm", left, rax());
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

    private void genDivision(Operand left, Operand right, Register res) {
        add("mov", rax(), left);
        add("cqo");
        if (right instanceof Immediate) {
            add("mov", rcx(), right);
            add("idiv", rcx());
        } else {
            if (right instanceof Address)
                ((Address) right).setShowSize(true);
            add("idiv", right);
        }
        add("mov", left, res);
    }

    public void visit(Div ins) {
        genDivision(ins.left(), ins.right(), rax());
    }

    public void visit(Mod ins) {
        genDivision(ins.left(), ins.right(), rdx());
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
        Operand left, right;
        left = ins.left();
        right= ins.right();
        add("mov", rcx(), right);
        add("sal" + " " + left.toNASM() + ", " + "cl");
    }
    public void visit(Sar ins) {
        Operand left, right;
        left = ins.left();
        right= ins.right();
        add("mov", rcx(), right);
        add("sar" + " " + left.toNASM() + ", " + "cl");
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

    private int mem2reg(Address addr, Register reg1, Register reg2) {
        switch (addr.type()) {
            case BASE_OFFSET:
                if (addr.index() == null) {
                    if (addr.base().isRegister()) {
                        return 0;
                    } else {
                        add("mov", reg1, addr.base());
                        addr.setBaseNasm(reg1);
                        return 1;
                    }
                } else {
                    if (addr.base().isRegister()) {
                        if (addr.index().isRegister()) {
                            return 0;
                        } else {
                            add("mov", reg1, addr.index());
                            addr.setIndexNasm(reg1);
                            return 1;
                        }
                    } else {
                        add("mov", reg1, addr.base());
                        addr.setBaseNasm(reg1);
                        if (addr.index().isRegister()) {
                            return 1;
                        } else {
                            add("mov", reg2, addr.index());
                            addr.setIndexNasm(reg2);
                            return 2;
                        }
                    }
                }
            case ENTITY:
                return 0;
            default:
                throw new InternalError("invalid address type " + addr.type());
        }
    }

    private boolean isAddress(Operand operand) {
        if (operand instanceof Address) {
            return true;
        } else if (operand instanceof Reference) {
            return !operand.isRegister();
        } else {
            return false;
        }
    }

    public void visit(Move ins) {
        boolean isAddrLeft  = isAddress(ins.dest());
        boolean isAddrRight = isAddress(ins.src());

        if (isAddrLeft && isAddrRight) {
            if (ins.src() instanceof Address)
                mem2reg((Address)ins.src(), rax(), rdx());
            add("mov", rdx(), ins.src());
            if (ins.dest() instanceof Address)
                mem2reg((Address)ins.dest(), rax(), rcx());
            add("mov", ins.dest(), rdx());
        } else {
            if (ins.dest() instanceof Address)  // cannot both be true
                mem2reg((Address)ins.dest(), rax(), rcx());
            if (ins.src() instanceof Address)   // cannot both be true
                mem2reg((Address)ins.src(), rax(), rcx());
            add("mov", ins.dest(), ins.src());
        }
    }

    public void visit(Lea ins) {
        mem2reg(ins.addr(), rax(), rdx());
        ins.addr().setShowSize(false);
        if (ins.dest().isRegister()) {
            add("lea", ins.dest(), ins.addr());
        } else {
            add("lea", rcx(), ins.addr());
            add("mov", ins.dest(), rcx());
        }
    }

    public void visit(Call ins) {
        List<Operand> operands = ins.operands();
        for (int i = operands.size() - 1; i >= 0; i--) {
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
        if (ins.entity().asmName().equals("printf"))
            add("xor", rax(), rax());
        add("call " + ins.entity().asmName());

        if (ins.ret() != null)
            add("mov", ins.ret(), rax());

        if (operands.size() > paraRegister.size())
            add("add", rsp(), new Immediate(
                    (operands.size() - paraRegister.size()) * VIRTUAL_STACK_REG_SIZE));
    }

    public void visit(Return ins) {
        if (ins.ret() != null)
            add("mov", rax(), ins.ret());
        //addJump(currentFunction.asmName() + END_SUFFIX);
    }

    public void visit(CJump ins) {
        if (ins.cond().isRegister()) {
            add("test", ins.cond(), ins.cond());
        } else {
            add("mov", rax(), ins.cond());
            add("test", rax(), rax());
        }

        add("jz " + ins.falseLabel().name());
        addJump(ins.trueLabel().name());
    }

    public void visit(Jmp ins) {
        addJump(ins.dest().name());
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
        /*out.println("========== NASM ==========");
        for (String s : asm) {
            out.println(s);
        }*/
    }

    private void pasteLibfunction() {
        asm.add("\n;========== LIB BEGIN ==========");
        File f = new File("lib.s");
        try {
            BufferedReader fin = new BufferedReader(new FileReader(f));
            String line;
            while((line = fin.readLine()) != null)
                asm.add(line);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


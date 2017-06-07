package com.mercy;

/**
 * Created by mercy on 17-5-20.
 */
public class Option {
    /***** CONSTANT *****/
    public static int REG_SIZE = 8;
    public static int STACK_VAR_ALIGNMENT_SIZE = 4;
    public static int CLASS_MEMBER_ALIGNMENT_SIZE = 4;
    public static int FRAME_ALIGNMENT_SIZE = 16;

    /***** I/O *****/
    public static String inFile = "testcase/test.c";
    public static String outFile = "out.asm";

    /***** DEBUG *****/
    public static boolean printRemoveInfo = true;
    public static boolean printInlineInfo = true;
    public static boolean printInstruction = false;
    public static boolean printBasicBlocks = false;

    public static boolean printNaiveAllocatorInfo = true;
    public static boolean printGlobalAllocationInfo = false;

    /***** OPTIMIZATION *****/
    public static boolean enableGlobalRegisterAllocation = true;

    // ast-ir level
    public static boolean enablePrintExpanding              = true;
    public static boolean enableFunctionInline              = true;
    public static boolean enableSelfInline                  = true;
    public static boolean enableCommonAssignElimination     = true;
    public static boolean enableInstructionSelection        = true;

    // control flow
    public static boolean enableControlFlowOptimization     = true;

    // data flow
    public static boolean enableCommonExpressionElimination = true;
    public static boolean enableConstantPropagation         = true;
    public static boolean enableDeadcodeElimination         = true;

    // other
    public static boolean enableLeafFunctionOptimization    = true;
    public static boolean enableOutputIrrelevantElimination = true;
}

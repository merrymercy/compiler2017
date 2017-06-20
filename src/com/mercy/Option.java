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
    public static boolean printRemoveInfo         = false;
    public static boolean printInlineInfo         = false;
    public static boolean printIrrelevantMarkInfo = false;
    public static boolean printInstruction        = false;
    public static boolean printBasicBlocks        = false;

    public static boolean printNaiveAllocatorInfo = false;
    public static boolean printGlobalAllocationInfo = false;

    /***** OPTIMIZATION *****/
    public static boolean enableGlobalRegisterAllocation = false;

    // ast-ir level
    public static boolean enableFunctionInline              = false;
    public static boolean enableSelfInline                  = false;
    public static boolean enableInstructionSelection        = false;
    public static boolean enableCommonAssignElimination     = false;
    public static boolean enablePrintExpanding              = false;

    // control flow
    public static boolean enableControlFlowOptimization     = false;

    // data flow
    public static boolean enableCommonExpressionElimination = false;
    public static boolean enableConstantPropagation         = false;
    public static boolean enableDeadcodeElimination         = false;

    // other
    public static boolean enableOutputIrrelevantElimination = false;
    public static boolean enableLeafFunctionOptimization    = false;

    // incorrect buf faster
    public static boolean enableDisableShortCut             = false;

    /***** OTHER *****/
    public static boolean printToC = true;
}
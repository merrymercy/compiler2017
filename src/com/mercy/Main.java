package com.mercy;

import com.mercy.compiler.AST.AST;
import com.mercy.compiler.FrontEnd.BuildListener;
import com.mercy.compiler.Parser.MalicLexer;
import com.mercy.compiler.Parser.MalicParser;
import com.mercy.compiler.Utility.SemanticError;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import java.io.FileInputStream;
import java.io.InputStream;

public class Main {
    public static void main(String[] args) throws Exception {
        InputStream is = new FileInputStream("testcase/test.c");
        try {
            compile(is);
        } catch (SemanticError error) {
            System.err.println(error.getMessage());
        }
    }

    public static void compile(InputStream sourceCode) throws Exception {
        ANTLRInputStream input = new ANTLRInputStream(sourceCode);
        MalicLexer lexer = new MalicLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        MalicParser parser = new MalicParser(tokens);

        parser.setBuildParseTree(true);
        ParseTree tree = parser.compilationUnit();

        ParseTreeWalker walker = new ParseTreeWalker();
        BuildListener listener = new BuildListener();

        walker.walk(listener, tree);

        AST ast  = listener.getAST();
        ast.resolveSymbol();
        ast.checkType();

    }
}

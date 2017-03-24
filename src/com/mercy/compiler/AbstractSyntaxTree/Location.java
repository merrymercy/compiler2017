package com.mercy.compiler.AbstractSyntaxTree;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * Created by mercy on 17-3-18.
 */
public class Location {
    private int line;
    private int column;

    public Location(int line, int column) {
        this.line = line;
        this.column = column;
    }

    public Location(Token token) {
        this.line = token.getLine();
        this.column = token.getCharPositionInLine();
    }

    public Location(ParserRuleContext ctx) {
        this(ctx.start);
    }

    public Location(TerminalNode terminal) {
        this(terminal.getSymbol());
    }

    public int line() {
        return line;
    }

    public int column() {
        return column;
    }

    public String toString() {
        return "line " + line + ":" + column + " ";
    }
}

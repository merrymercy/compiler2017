package com.mercy.compiler.Entity;

import com.mercy.compiler.AST.ExprNode;
import com.mercy.compiler.AST.Location;
import com.mercy.compiler.Type.StringType;
import com.mercy.compiler.Type.Type;

/**
 * Created by mercy on 17-3-20.
 */
public class StringConstantEntity extends Entity {
    private ExprNode expr;
    private String value;

    public StringConstantEntity(Location loc, Type type, String name, ExprNode expr) {
        super(loc, type, StringType.STRING_CONSTANT_PREFIX + name);
        this.expr = expr;
        StringBuffer sb = new StringBuffer();
        name = name.replaceAll("\\\\" + "\"" , "\"");
        name = name.replaceAll("\\\\" + "n" , "\n");
        name = name.replaceAll("\\\\" + "\\\\" , "\\\\");
        this.value = name;
    }

    public String strValue() {
        return value;
    }

    @Override
    public <T> T accept(EntityVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public String toString() {
        return "constant entity : " + name;
    }
}

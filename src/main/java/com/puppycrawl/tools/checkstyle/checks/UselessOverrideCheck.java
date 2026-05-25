package com.puppycrawl.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.api.AbstractCheck;
import com.puppycrawl.tools.checkstyle.api.DetailAST;
import com.puppycrawl.tools.checkstyle.api.TokenTypes;

/**
 * This check is designed to identify overriden methods that only call parents's implementation
 * with no additional logic
 * e.g.
 * public class B extends A {
 *      @Override
 *      public void foo(int bar) {
 *          super.foo(int bar)
 *      }
 * }
 */
public class UselessOverrideCheck extends AbstractCheck {
    String MSG_KEY = "useless.method.override";

    @Override
    public int[] getDefaultTokens() {
        return getRequiredTokens();
    }

    @Override
    public int[] getAcceptableTokens() {
        return getRequiredTokens();
    }

    @Override
    public int[] getRequiredTokens() {
        return new int[] {
                TokenTypes.METHOD_DEF
        };
    }

    @Override
    public void visitToken(DetailAST ast) {
        // if method has more than 1 line of code no applicable
        DetailAST astSList = ast.findFirstToken(TokenTypes.SLIST);
        if (astSList.getChildCount(TokenTypes.EXPR) != 1) {
            return;
        }


        //todo: double check failure cases and double check for super call and check return
        String methodName = ast.findFirstToken(TokenTypes.IDENT).getText();
        String methodCalled = astSList.findFirstToken(TokenTypes.EXPR).findFirstToken(TokenTypes.METHOD_CALL)
                .findFirstToken(TokenTypes.DOT).findFirstToken(TokenTypes.IDENT).getText();
        if (methodName.equals(methodCalled)) {
            final DetailAST classDef = getNearestClassOrEnumDefinition(ast);
            final String className = classDef.findFirstToken(TokenTypes.IDENT).getText();
            log(ast, MSG_KEY, className, methodName);
        }
    }

    /**
     * Returns CLASS_DEF or ENUM_DEF token which is the nearest to the given ast node.
     * Searches the tree towards the root until it finds a CLASS_DEF or ENUM_DEF node.
     *
     * @param ast the start node for searching.
     * @return the CLASS_DEF or ENUM_DEF token.
     */
    private DetailAST getNearestClassOrEnumDefinition(DetailAST ast) {
        DetailAST searchAST = ast;
        while (searchAST.getType() != TokenTypes.CLASS_DEF
                && searchAST.getType() != TokenTypes.ENUM_DEF) {
            searchAST = searchAST.getParent();
        }
        return searchAST;
    }

}

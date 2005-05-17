package com.siyeh.ig.verbose;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.ConstantExpressionUtil;
import com.siyeh.ig.*;

import java.util.Set;
import java.util.HashSet;

import org.jetbrains.annotations.NotNull;

public class PointlessBitwiseExpressionInspection extends ExpressionInspection {

    private final PointlessBitwiseFix fix = new PointlessBitwiseFix();

    public String getDisplayName() {
        return "Pointless bitwise expression";
    }

    public String getGroupDisplayName() {
        return GroupNames.VERBOSE_GROUP_NAME;
    }

    public boolean isEnabledByDefault(){
        return true;
    }
    public String buildErrorString(PsiElement location) {
        return "#ref can be replaced with " +
                calculateReplacementExpression((PsiExpression) location) + " #loc";
    }

    private static String calculateReplacementExpression(PsiExpression expression) {
        final PsiBinaryExpression exp = (PsiBinaryExpression) expression;
        final PsiExpression lhs = exp.getLOperand();
        final PsiExpression rhs = exp.getROperand();
        final PsiJavaToken sign = exp.getOperationSign();
        final IElementType tokenType = sign.getTokenType();
        final PsiType expressionType = exp.getType();
        if (tokenType.equals(JavaTokenType.AND)) {
            if (isZero(lhs, expressionType) || isAllOnes(rhs, expressionType)) {
                return lhs.getText();
            } else {
                return rhs.getText();
            }
        } else if (tokenType.equals(JavaTokenType.OR)) {
            if (isZero(lhs, expressionType) || isAllOnes(rhs, expressionType)) {
                return rhs.getText();
            } else {
                return lhs.getText();
            }
        } else if (tokenType.equals(JavaTokenType.XOR)) {
            if (isAllOnes(lhs, expressionType)) {
                return '~' + rhs.getText();
            } else if (isAllOnes(rhs, expressionType)) {
                return '~' + lhs.getText();
            } else if (isZero(rhs, expressionType)) {
                return lhs.getText();
            } else {
                return rhs.getText();
            }
        } else if (tokenType.equals(JavaTokenType.LTLT) ||
                tokenType.equals(JavaTokenType.GTGT) ||
                tokenType.equals(JavaTokenType.GTGTGT)) {
            return lhs.getText();
        } else {
            return "";
        }
    }

    public BaseInspectionVisitor buildVisitor() {
        return new PointlessBitwiseVisitor();
    }

    public InspectionGadgetsFix buildFix(PsiElement location) {
        return fix;
    }

    private static class PointlessBitwiseFix extends InspectionGadgetsFix {
        public String getName() {
            return "Simplify";
        }

        public void applyFix(Project project, ProblemDescriptor descriptor) {
            if(isQuickFixOnReadOnlyFile(descriptor)) return;
            final PsiExpression expression = (PsiExpression) descriptor.getPsiElement();
            final String newExpression = calculateReplacementExpression(expression);
            replaceExpression(expression, newExpression);
        }

    }

    private static class PointlessBitwiseVisitor extends BaseInspectionVisitor {
        /**
         * @noinspection StaticCollection
         */
        private static final Set<IElementType> bitwiseTokens = new HashSet<IElementType>(4);

        static
        {
            bitwiseTokens.add(JavaTokenType.AND);
            bitwiseTokens.add(JavaTokenType.OR);
            bitwiseTokens.add(JavaTokenType.XOR);
            bitwiseTokens.add(JavaTokenType.LTLT);
            bitwiseTokens.add(JavaTokenType.GTGT);
            bitwiseTokens.add(JavaTokenType.GTGTGT);
        }

        public void visitClass(@NotNull PsiClass aClass) {
            //to avoid drilldown
        }
        public void visitBinaryExpression(@NotNull PsiBinaryExpression expression) {
            super.visitBinaryExpression(expression);
            final PsiJavaToken sign = expression.getOperationSign();
            if(sign == null){
                return;
            }
            final IElementType tokenType = sign.getTokenType();
            if(!bitwiseTokens.contains(tokenType))
            {
                return;
            }

            final PsiType expressionType = expression.getType();
            if (expressionType == null) {
                return;
            }

            final PsiExpression rhs = expression.getROperand();
            if (rhs == null) {
                return;
            }

            final PsiType rhsType = rhs.getType();
            if (rhsType == null) {
                return;
            }
            if (rhsType.equals(PsiType.BOOLEAN) ||
                    rhsType.equalsToText("java.lang.Boolean")) {
                return;
            }
            final PsiExpression lhs = expression.getLOperand();
            if (lhs == null) {
                return;
            }
            final PsiType lhsType = lhs.getType();
            if (lhsType == null) {
                return;
            }
            if (lhsType.equals(PsiType.BOOLEAN) ||
                        lhsType.equalsToText("java.lang.Boolean")) {
                return;
            }
            final boolean isPointless;
            if (tokenType.equals(JavaTokenType.AND)) {
                isPointless = andExpressionIsPointless(lhs, rhs, expressionType);
            } else if (tokenType.equals(JavaTokenType.OR)) {
                isPointless = orExpressionIsPointless(lhs, rhs, expressionType);
            } else if (tokenType.equals(JavaTokenType.XOR)) {
                isPointless = xorExpressionIsPointless(lhs, rhs, expressionType);
            } else if (tokenType.equals(JavaTokenType.LTLT) ||
                    tokenType.equals(JavaTokenType.GTGT) ||
                    tokenType.equals(JavaTokenType.GTGTGT)) {
                isPointless = shiftExpressionIsPointless(rhs, expressionType);
            } else {
                isPointless = false;
            }
            if (!isPointless) {
                return;
            }
            registerError(expression);
        }
    }

    private static boolean shiftExpressionIsPointless(PsiExpression rhs, PsiType expressionType) {
        return isZero(rhs, expressionType);
    }

    private static boolean orExpressionIsPointless(PsiExpression lhs, PsiExpression rhs, PsiType expressionType) {
        return isZero(lhs, expressionType) || isZero(rhs, expressionType) || isAllOnes(lhs, expressionType) || isAllOnes(rhs, expressionType);
    }

    private static boolean xorExpressionIsPointless(PsiExpression lhs, PsiExpression rhs, PsiType expressionType) {
        return isZero(lhs, expressionType) || isZero(rhs, expressionType) || isAllOnes(lhs, expressionType) || isAllOnes(rhs, expressionType);
    }

    private static boolean andExpressionIsPointless(PsiExpression lhs, PsiExpression rhs, PsiType expressionType) {
        return isZero(lhs, expressionType) || isZero(rhs, expressionType) || isAllOnes(lhs, expressionType) || isAllOnes(rhs, expressionType);
    }

    private static boolean isZero(PsiExpression expression, PsiType expressionType) {
        final Object value = ConstantExpressionUtil.computeCastTo(expression, expressionType);
        if (value == null) {
            return false;
        }
        if (value instanceof Integer && ((Integer) value) == 0) {
            return true;
        }
        if (value instanceof Long && ((Long) value) == 0L) {
            return true;
        }
        if (value instanceof Short && ((Short) value) == 0) {
            return true;
        }
        if (value instanceof Character && ((Character) value) == 0) {
            return true;
        }
        return value instanceof Byte && ((Byte) value) == 0;
    }

    private static boolean isAllOnes(PsiExpression expression, PsiType expressionType) {
        final Object value = ConstantExpressionUtil.computeCastTo(expression, expressionType);
        if (value == null) {
            return false;
        }
        if (value instanceof Integer && ((Integer) value) == 0xffffffff) {
            return true;
        }
        if (value instanceof Long && ((Long) value) == 0xffffffffffffffffL) {
            return true;
        }
        if (value instanceof Short && ((Short) value) == (short) 0xffff) {
            return true;
        }
        if (value instanceof Character && ((Character) value) == (char) 0xffff) {
            return true;
        }
        return value instanceof Byte && ((Byte) value) == (byte) 0xff;
    }

}

package com.siyeh.ig.methodmetrics;

import com.intellij.psi.*;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.GroupNames;
import com.siyeh.ig.psiutils.ControlFlowUtils;
import org.jetbrains.annotations.NotNull;

public class MultipleReturnPointsPerMethodInspection extends MethodMetricInspection {
    public String getID(){
        return "MethodWithMultipleReturnPoints";
    }
    public String getDisplayName() {
        return "Method with multiple return points.";
    }

    public String getGroupDisplayName() {
        return GroupNames.METHODMETRICS_GROUP_NAME;
    }

    protected int getDefaultLimit() {
        return 1;
    }

    protected String getConfigurationLabel() {
        return "Return point limit:";
    }

    public String buildErrorString(PsiElement location) {
        final PsiMethod parent = (PsiMethod) location.getParent();
        final int numReturnPoints = calculateNumReturnPoints(parent);
        return "#ref has " + numReturnPoints + " return points #loc";
    }

    public BaseInspectionVisitor buildVisitor() {
        return new MultipleReturnPointsVisitor();
    }

    private class MultipleReturnPointsVisitor extends BaseInspectionVisitor {

        public void visitMethod(@NotNull PsiMethod method) {
            // note: no call to super
            final int numReturnPoints = calculateNumReturnPoints(method);
            if (numReturnPoints <= getLimit()) {
                return;
            }
            registerMethodError(method);
        }

    }

    private static int calculateNumReturnPoints(PsiMethod method) {
        final ReturnPointCountVisitor visitor = new ReturnPointCountVisitor();
        method.accept(visitor);
        final int count = visitor.getCount();

        if (!mayFallThroughBottom(method)) {
            return count;
        }
        final PsiCodeBlock body = method.getBody();
        if (body == null) {
            return count;
        }
        final PsiStatement[] statements = body.getStatements();
        if (statements.length == 0) {
            return count + 1;
        }
        final PsiStatement lastStatement = statements[statements.length - 1];
        if (ControlFlowUtils.statementMayCompleteNormally(lastStatement)) {
            return count + 1;
        }
        return count;
    }

    private static boolean mayFallThroughBottom(PsiMethod method) {
        if (method.isConstructor()) {
            return true;
        }
        final PsiType returnType = method.getReturnType();
        return PsiType.VOID.equals(returnType);
    }
}

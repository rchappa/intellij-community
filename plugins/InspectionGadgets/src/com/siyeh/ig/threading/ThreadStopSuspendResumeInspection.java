package com.siyeh.ig.threading;

import com.intellij.psi.*;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ExpressionInspection;
import com.siyeh.ig.GroupNames;
import org.jetbrains.annotations.NotNull;

public class ThreadStopSuspendResumeInspection extends ExpressionInspection {
    public String getID(){
        return "CallToThreadStopSuspendOrResumeManager";
    }
    public String getDisplayName() {
        return "Call to 'Thread.stop()', '.suspend()' or '.resume()'";
    }

    public String getGroupDisplayName() {
        return GroupNames.THREADING_GROUP_NAME;
    }

    public String buildErrorString(PsiElement location) {

        return "Call to Thread.#ref() #loc";
    }

    public BaseInspectionVisitor buildVisitor() {
        return new ThreadStopSuspendVisitor();
    }

    private static class ThreadStopSuspendVisitor extends BaseInspectionVisitor {

        public void visitMethodCallExpression(@NotNull PsiMethodCallExpression expression) {
            super.visitMethodCallExpression(expression);

            final PsiReferenceExpression methodExpression = expression.getMethodExpression();
            if (methodExpression == null) {
                return;
            }
            if (!isSetSecurityManager(expression)) {
                return;
            }
            registerMethodCallError(expression);
        }

        private static boolean isSetSecurityManager(PsiMethodCallExpression expression) {
            final PsiReferenceExpression methodExpression = expression.getMethodExpression();

            final String methodName = methodExpression.getReferenceName();
            if (!("stop".equals(methodName) || "suspend".equals(methodName) ||
                    "resume".equals(methodName)) ) {
                return false;
            }
            final PsiMethod method = expression.resolveMethod();
            if (method == null) {
                return false;
            }
            final PsiClass aClass = method.getContainingClass();
            if (aClass == null) {
                return false;
            }
            final String className = aClass.getQualifiedName();
            if (className == null) {
                return false;
            }
            return "java.lang.Thread".equals(className);
        }
    }

}

package com.siyeh.ig.cloneable;

import com.intellij.psi.*;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.GroupNames;
import com.siyeh.ig.MethodInspection;
import org.jetbrains.annotations.NotNull;

public class CloneCallsSuperCloneInspection extends MethodInspection {
    public String getID(){
        return "CloneDoesntCallSuperClone";
    }
    public String getDisplayName() {
        return "'clone()' doesn't call 'super.clone()'";
    }

    public String getGroupDisplayName() {
        return GroupNames.CLONEABLE_GROUP_NAME;
    }

    public String buildErrorString(PsiElement location) {
        return "#ref() doesn't call super.clone()";
    }

    public boolean isEnabledByDefault(){
        return true;
    }
    public BaseInspectionVisitor buildVisitor() {
        return new NoExplicitCloneCallsVisitor();
    }

    private static class NoExplicitCloneCallsVisitor extends BaseInspectionVisitor {

        public void visitMethod(@NotNull PsiMethod method) {
            //note: no call to super;
            final String methodName = method.getName();
            if (!"clone".equals(methodName)) {
                return;
            }
            if(method.hasModifierProperty(PsiModifier.ABSTRACT))
            {
                return;
            }
            final PsiParameterList parameterList = method.getParameterList();
            if (parameterList.getParameters().length != 0) {
                return;
            }
            final PsiClass containingClass = method.getContainingClass();
            if (containingClass.isInterface() || containingClass.isAnnotationType()) {
                return;
            }
            final CallToSuperCloneVisitor visitor = new CallToSuperCloneVisitor();
            method.accept(visitor);
            if (visitor.isCallToSuperCloneFound()) {
                return;
            }
            registerMethodError(method);
        }

    }

}

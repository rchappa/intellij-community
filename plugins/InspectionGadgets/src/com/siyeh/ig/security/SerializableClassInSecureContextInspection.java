package com.siyeh.ig.security;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiMethod;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ClassInspection;
import com.siyeh.ig.GroupNames;
import com.siyeh.ig.psiutils.ControlFlowUtils;
import com.siyeh.ig.psiutils.SerializationUtils;
import org.jetbrains.annotations.NotNull;

public class SerializableClassInSecureContextInspection extends ClassInspection {

    public String getDisplayName() {
        return "Serializable class in secure context";
    }

    public String getGroupDisplayName() {
        return GroupNames.SECURITY_GROUP_NAME;
    }

    public String buildErrorString(PsiElement location) {
        return "Class #ref may be serialized, compromising security #loc";
    }

    public BaseInspectionVisitor buildVisitor() {
        return new SerializableClassInSecureContextVisitor();
    }

    private static class SerializableClassInSecureContextVisitor extends BaseInspectionVisitor {
     
        public void visitClass(@NotNull PsiClass aClass) {
            // no call to super, so it doesn't drill down
            if (aClass.isInterface() || aClass.isAnnotationType()) {
                return;
            }
            if (!SerializationUtils.isSerializable(aClass)) {
                return;
            }
            final PsiMethod[] methods = aClass.getMethods();
            for(final PsiMethod method : methods){
                if(SerializationUtils.isWriteObject(method)){
                    if(ControlFlowUtils.methodAlwaysThrowsException(method)){
                        return;
                    }
                }
            }
            registerClassError(aClass);
        }
    }
}

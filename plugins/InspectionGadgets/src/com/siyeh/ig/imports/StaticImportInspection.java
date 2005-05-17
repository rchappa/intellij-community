package com.siyeh.ig.imports;

import com.intellij.psi.*;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ClassInspection;
import com.siyeh.ig.GroupNames;
import org.jetbrains.annotations.NotNull;

public class StaticImportInspection extends ClassInspection {

    public String getDisplayName() {
        return "Static import";
    }

    public String getGroupDisplayName() {
        return GroupNames.IMPORTS_GROUP_NAME;
    }

    public String buildErrorString(PsiElement location) {
        return "Static import #ref  #loc";
    }

    public BaseInspectionVisitor buildVisitor() {
        return new StaticImportVisitor();
    }

    private static class StaticImportVisitor extends BaseInspectionVisitor {

        public void visitClass(@NotNull PsiClass aClass) {
            // no call to super, so it doesn't drill down
            if (!(aClass.getParent() instanceof PsiJavaFile)) {
                return;
            }
            final PsiJavaFile file = (PsiJavaFile) aClass.getParent();
            if (!file.getClasses()[0].equals(aClass)) {
                return;
            }
            final PsiImportList importList = file.getImportList();
            final PsiImportStaticStatement[] importStatements = importList.getImportStaticStatements();
            for(final PsiImportStaticStatement importStatement : importStatements){
                final PsiJavaCodeReferenceElement reference = importStatement.getImportReference();
                if(reference != null){
                    registerError(reference);
                }
            }
        }

    }
}

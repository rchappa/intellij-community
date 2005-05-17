package com.siyeh.ig.classlayout;

import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.*;
import com.siyeh.ig.BaseInspectionVisitor;
import com.siyeh.ig.ClassInspection;
import com.siyeh.ig.GroupNames;
import com.siyeh.ig.psiutils.ClassUtils;
import org.jetbrains.annotations.NotNull;

public class ExtendsAnnotationInspection extends ClassInspection{

    public String getID(){
        return "ClassExplicitlyAnnotation";
    }

    public String getDisplayName(){
        return "Class extends annotation interface";
    }

    public String getGroupDisplayName(){
        return GroupNames.CLASSLAYOUT_GROUP_NAME;
    }

    public boolean isEnabledByDefault(){
        return true;
    }

    public String buildErrorString(PsiElement location){
        final PsiClass containingClass = ClassUtils.getContainingClass(location);
        assert containingClass != null;
        return "Class "+ containingClass.getName()+" explicitly extends annotation interface '#ref' #loc";
    }

    public BaseInspectionVisitor buildVisitor(){
        return new ExtendsAnnotationVisitor();
    }

    private static class ExtendsAnnotationVisitor extends BaseInspectionVisitor{


        public void visitClass(@NotNull PsiClass aClass){
            final PsiManager manager = aClass.getManager();
            final LanguageLevel languageLevel =
                    manager.getEffectiveLanguageLevel();
            if(languageLevel.equals(LanguageLevel.JDK_1_3) ||
                       languageLevel.equals(LanguageLevel.JDK_1_4)){
                return;
            }
            if(aClass.isAnnotationType()){
                return;
            }
            final PsiReferenceList extendsList = aClass.getExtendsList();
            if(extendsList != null){
                final PsiJavaCodeReferenceElement[] elements =
                        extendsList.getReferenceElements();
                for(final PsiJavaCodeReferenceElement element : elements){
                    final PsiElement referent = element.resolve();
                    if(referent instanceof PsiClass){
                        ((PsiClass) referent).isAnnotationType();
                        if(((PsiClass) referent).isAnnotationType()){
                            registerError(element);
                        }
                    }
                }
            }
            final PsiReferenceList implementsList = aClass.getImplementsList();
            if(implementsList != null){
                final PsiJavaCodeReferenceElement[] elements =
                        implementsList.getReferenceElements();
                for(final PsiJavaCodeReferenceElement element : elements){
                    final PsiElement referent = element.resolve();
                    if(referent instanceof PsiClass){
                        ((PsiClass) referent).isAnnotationType();
                        if(((PsiClass) referent).isAnnotationType()){
                            registerError(element);
                        }
                    }
                }
            }
        }
    }
}

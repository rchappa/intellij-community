package com.siyeh.ig;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public abstract class BaseInspection extends LocalInspectionTool{
    private final String m_shortName = null;
    private InspectionRunListener listener = null;
    private boolean telemetryEnabled = true;

    public String getShortName(){
        if(m_shortName == null){
            final Class aClass = getClass();
            final String name = aClass.getName();
            return name.substring(name.lastIndexOf((int) '.') + 1,
                                  name.length() - "Inspection".length());
        }
        return m_shortName;
    }

    protected BaseInspectionVisitor createVisitor(InspectionManager inspectionManager,
                                                  boolean onTheFly){
        final BaseInspectionVisitor visitor = buildVisitor();
        visitor.setInspectionManager(inspectionManager);
        visitor.setOnTheFly(onTheFly);
        visitor.setInspection(this);
        return visitor;
    }

    protected @Nullable String buildErrorString(PsiElement location){
        return null;
    }

    protected @Nullable String buildErrorString(Object arg){
        return null;
    }

    protected boolean buildQuickFixesOnlyForOnTheFlyErrors(){
        return false;
    }

    @Nullable
            protected InspectionGadgetsFix buildFix(PsiElement location){
        return null;
    }

    @Nullable
            public ProblemDescriptor[] checkMethod(PsiMethod method,
                                                   InspectionManager manager,
                                                   boolean isOnTheFly){
        if(telemetryEnabled){
            initializeTelemetryIfNecessary();
            final long start = System.currentTimeMillis();
            try{
                return doCheckMethod(method, manager, isOnTheFly);
            } finally{
                final long end = System.currentTimeMillis();
                final String displayName = getDisplayName();
                listener.reportRun(displayName, end - start);
            }
        } else{
            return doCheckMethod(method, manager, isOnTheFly);
        }
    }

    @Nullable
            protected ProblemDescriptor[] doCheckMethod(PsiMethod method,
                                                        InspectionManager manager,
                                                        boolean isOnTheFly){
        return super.checkMethod(method, manager, isOnTheFly);
    }

    @Nullable
            public ProblemDescriptor[] checkClass(PsiClass aClass,
                                                  InspectionManager manager,
                                                  boolean isOnTheFly){
        initializeTelemetryIfNecessary();
        if(telemetryEnabled){
            final long start = System.currentTimeMillis();
            try{
                return doCheckClass(aClass, manager, isOnTheFly);
            } finally{
                final long end = System.currentTimeMillis();
                final String name = getDisplayName();
                listener.reportRun(name, end - start);
            }
        } else{
            return doCheckClass(aClass, manager, isOnTheFly);
        }
    }

    protected ProblemDescriptor[] doCheckClass(PsiClass aClass,
                                               InspectionManager manager,
                                               boolean isOnTheFly){
        return super.checkClass(aClass, manager, isOnTheFly);
    }

    @Nullable
            public ProblemDescriptor[] checkField(PsiField field,
                                                  InspectionManager manager,
                                                  boolean isOnTheFly){
        if(telemetryEnabled){
            initializeTelemetryIfNecessary();
            final long start = System.currentTimeMillis();
            try{
                return doCheckField(field, manager, isOnTheFly);
            } finally{
                final long end = System.currentTimeMillis();
                final String displayName = getDisplayName();
                listener.reportRun(displayName, end - start);
            }
        } else{
            return doCheckField(field, manager, isOnTheFly);
        }
    }

    private void initializeTelemetryIfNecessary(){
        if(telemetryEnabled && listener == null){
            final Application application = ApplicationManager.getApplication();
            final InspectionGadgetsPlugin plugin =
                    (InspectionGadgetsPlugin) application.getComponent("InspectionGadgets");
            telemetryEnabled = plugin.isTelemetryEnabled();
            listener = plugin.getTelemetry();
        }
    }

    @Nullable
            protected ProblemDescriptor[] doCheckField(PsiField field,
                                                       InspectionManager manager,
                                                       boolean isOnTheFly){
        return super.checkField(field, manager, isOnTheFly);
    }

    public boolean hasQuickFix(){
        final Method[] methods = getClass().getDeclaredMethods();
        for(final Method method : methods){
            final String methodName = method.getName();
            if("buildFix".equals(methodName)){
                return true;
            }
        }
        return false;
    }

    public abstract BaseInspectionVisitor buildVisitor();
}

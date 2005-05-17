package com.siyeh.ig.bugs;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.psi.PsiDoWhileStatement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiForStatement;
import com.intellij.psi.PsiWhileStatement;
import com.siyeh.ig.*;
import com.siyeh.ig.psiutils.ControlFlowUtils;
import org.jetbrains.annotations.NotNull;

public class InfiniteLoopStatementInspection extends StatementInspection {

    public String getDisplayName() {
        return "Infinite loop statement";
    }

    public String getGroupDisplayName() {
        return GroupNames.BUGS_GROUP_NAME;
    }

    public boolean isEnabledByDefault(){
        return true;
    }

    public String buildErrorString(PsiElement location) {
        return "#ref statement cannot complete without throwing an exception #loc";
    }

    public BaseInspectionVisitor buildVisitor() {
        return new InfiniteLoopStatementsVisitor();
    }

    private static class InfiniteLoopStatementsVisitor extends StatementInspectionVisitor {


        public void visitForStatement(@NotNull PsiForStatement statement) {
            super.visitForStatement(statement);
            if (ControlFlowUtils.statementMayCompleteNormally(statement)) {
                return;
            }
            if (ControlFlowUtils.statementContainsReturn(statement)) {
                return;
            }
            registerStatementError(statement);
        }

        public void visitWhileStatement(@NotNull PsiWhileStatement statement) {

            super.visitWhileStatement(statement);
            if (ControlFlowUtils.statementMayCompleteNormally(statement)) {
                return;
            }
            if (ControlFlowUtils.statementContainsReturn(statement)) {
                return;
            }
            registerStatementError(statement);
        }

        public void visitDoWhileStatement(@NotNull PsiDoWhileStatement statement) {
            super.visitDoWhileStatement(statement);
            if (ControlFlowUtils.statementMayCompleteNormally(statement)) {
                return;
            }
            if (ControlFlowUtils.statementContainsReturn(statement)) {
                return;
            }
            registerStatementError(statement);
        }

    }

}

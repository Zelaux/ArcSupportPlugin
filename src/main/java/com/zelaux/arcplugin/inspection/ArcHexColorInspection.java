package com.zelaux.arcplugin.inspection;


import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import com.intellij.uast.*;
import com.zelaux.arcplugin.*;
import com.zelaux.arcplugin.MetaData.*;
import com.zelaux.arcplugin.utils.*;
import org.jetbrains.annotations.*;
import org.jetbrains.uast.*;
import org.jetbrains.uast.visitor.*;

import java.util.*;

public class ArcHexColorInspection extends AbstractBaseUastLocalInspectionTool{
    protected ArcHexColorInspection(){
        super(ULiteralExpression.class);
    }

    @Override
    public PsiNamedElement getProblemElement(@NotNull PsiElement psiElement){
        return super.getProblemElement(psiElement);
    }

    @Override
    @NotNull
    public PsiElementVisitor buildVisitor(@NotNull final ProblemsHolder holder, final boolean isOnTheFly){
        return new UastVisitorAdapter(new AbstractUastNonRecursiveVisitor(){

            @Override
            public boolean visitElement(@NotNull UElement node){
                return false;
            }
private Set<PsiElement> visited=new HashSet<>();
            @Override
            public boolean visitCallExpression(@NotNull UCallExpression node){
//                System.out.println("afterVisitCallExpression: "+node );
                if (visited.add(node.getSourcePsi())) addDescriptors(checkCallExpression(node, holder.getManager(), isOnTheFly));

                return false;
            }
            private void addDescriptors(final ProblemDescriptor[] descriptors){
                if(descriptors != null){
                    for(ProblemDescriptor descriptor : descriptors){
                        holder.registerProblem(descriptor);
                    }
                }
            }
        }, true);
    }

    private ProblemDescriptor[] checkCallExpression(UCallExpression expression, InspectionManager manager, boolean isOnTheFly){

        if (!UExpressionUtils.isStaticMethod(expression, Color.PATH, Color.valueOf)){
            return ProblemDescriptor.EMPTY_ARRAY;
        }
        List<UExpression> typeArguments = expression.getValueArguments();

        UExpression strExpr = typeArguments.get(typeArguments.size() - 1);
        if(!(strExpr instanceof ULiteralExpression && ((ULiteralExpression)strExpr).

        isString())){

            return ProblemDescriptor.EMPTY_ARRAY;
        }

        var problemsHolder = new ProblemsHolder(manager, expression.getSourcePsi().getContainingFile(), isOnTheFly);

        String value = String.valueOf(((ULiteralExpression)strExpr).getValue());

        int offset = value.startsWith("#") ? 1 : 0;
        if((value.length() - offset) != 6 && (value.length() - offset) != 8){
            problemsHolder.registerProblem(manager.createProblemDescriptor(strExpr.getSourcePsi(), "Wrong hex length(Might be 6 or 8)",isOnTheFly,LocalQuickFix.EMPTY_ARRAY, ProblemHighlightType.ERROR));
        }
        if(!value.matches("#?[\\da-fA-F]+")){
            problemsHolder.registerProblem(manager.createProblemDescriptor(strExpr.getSourcePsi(), "Hex has illegal symbols",isOnTheFly,LocalQuickFix.EMPTY_ARRAY, ProblemHighlightType.ERROR));
        }
        return problemsHolder.getResultsArray();
    }
}

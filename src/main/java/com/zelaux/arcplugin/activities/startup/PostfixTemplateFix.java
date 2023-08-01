package com.zelaux.arcplugin.activities.startup;

import arc.struct.*;
import com.intellij.codeInsight.template.postfix.templates.*;
import com.intellij.codeInsight.template.postfix.templates.editable.*;
import com.intellij.codeInsight.template.postfix.templates.editable.JavaPostfixTemplateExpressionCondition.*;
import com.intellij.lang.*;
import com.intellij.openapi.project.*;
import com.intellij.openapi.startup.*;
import com.intellij.psi.*;
import com.intellij.psi.util.*;
import com.zelaux.arcplugin.codeInsight.template.postfix.templates.*;
import org.jetbrains.annotations.*;

import java.util.*;

import static com.intellij.codeInsight.template.postfix.util.JavaPostfixTemplatesUtils.*;

public class PostfixTemplateFix implements StartupActivity{
    private static final Seq<Class<?>> sequences = Seq.with(
    BoolSeq.class,
    ByteSeq.class,
    FloatSeq.class,
    IntSeq.class,
    LongSeq.class,
    ShortSeq.class,
    Seq.class
    );
    private static final Seq<Class<?>> nonIterableQueue = Seq.with(
    LongQueue.class,
    IntQueue.class
    );

    @Contract("null -> false")
    public static boolean isSeq(@Nullable PsiType type){
        if(type == null) return false;
//        JavaPsiFacade.getInstance(psiClass.getProject()).findClass(type.getCanonicalText(), type.getResolveScope())
        if(type instanceof PsiClassType){
            PsiClass psiClass = ((PsiClassType)type).resolve();
            if(psiClass == null) return false;
            //noinspection UnstableApiUsage
           /* Seq<JvmMethod> methodsByName = Seq.with(psiClass.findMethodsByName("size")).select(it -> {
                //noinspection UnstableApiUsage
                return it.getParameters().length == 0 && it.hasModifier(JvmModifier.PUBLIC) && !it.hasModifier(JvmModifier.STATIC);
            });*/
            LinkedHashSet<PsiClass> superClasses = InheritanceUtil.getSuperClasses(psiClass);
            if(Seq.with(superClasses).add(psiClass).contains(it -> it.getQualifiedName().startsWith("arc.struct"))){
                return true;
            }
        }
        return type != null && InheritanceUtil.isInheritor(type, Seq.class.getName());
    }

    @Nullable
    public static String getExpressionBound(@NotNull PsiExpression expr){
        PsiType type = expr.getType();
        if(isNumber(type)){
            return expr.getText();
        }else if(isArray(type)){
            return expr.getText() + ".length";
        }else if(isSeq(type)){

            return expr.getText() + ".size";
        }else if(isIterable(type)){

            return expr.getText() + ".size()";
        }
        return null;
    }

    public static void editConditions(@NotNull Set<JavaPostfixTemplateExpressionCondition> condistion){
        for(Class<?> clazz : nonIterableQueue){
            condistion.add(new JavaPostfixTemplateExpressionFqnCondition(clazz.getCanonicalName()));
        }
        for(Class<?> clazz : sequences){
            condistion.add(new JavaPostfixTemplateExpressionFqnCondition(clazz.getCanonicalName()));
        }
    }

    @Override
    public void runActivity(@NotNull Project project){
        Language java = Language.findLanguageByID("JAVA");
        assert java != null;
        JavaPostfixTemplateProvider provider = (JavaPostfixTemplateProvider)Seq.with(LanguagePostfixTemplate.LANG_EP.allForLanguage(java)).find(it -> it instanceof JavaPostfixTemplateProvider);
        Set<PostfixTemplate> templates = provider.getTemplates();

        for(PostfixTemplate template : Seq.with(templates)){
            PostfixTemplate newTemplate = null;
            if(template.getClass().equals(ForDescendingPostfixTemplate.class)){
                JavaPostfixTemplateProvider templateProvider = (JavaPostfixTemplateProvider)template.getProvider();
                assert templateProvider != null;
                newTemplate = new Fix_ForDescendingPostfixTemplate(templateProvider);
            }else if(template.getClass().equals(ForAscendingPostfixTemplate.class)){
                JavaPostfixTemplateProvider templateProvider = (JavaPostfixTemplateProvider)template.getProvider();
                assert templateProvider != null;
                newTemplate = new Fix_ForAscendingPostfixTemplate(templateProvider);
            }
            if(newTemplate != null){
                templates.remove(template);
                templates.add(newTemplate);
            }
        }

    }
}

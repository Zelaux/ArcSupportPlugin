package com.zelaux.arcplugin.debugger.settings;

import arc.struct.*;
import arc.struct.ObjectMap.*;
import com.intellij.debugger.*;
import com.intellij.debugger.engine.*;
import com.intellij.debugger.engine.evaluation.*;
import com.intellij.debugger.engine.evaluation.expression.*;
import com.intellij.debugger.settings.*;
import com.intellij.debugger.ui.impl.watch.*;
import com.intellij.debugger.ui.tree.*;
import com.intellij.debugger.ui.tree.render.*;
import com.intellij.ide.highlighter.*;
import com.intellij.openapi.project.*;
import com.intellij.openapi.util.*;
import com.intellij.xdebugger.frame.presentation.*;
import com.intellij.xdebugger.impl.ui.tree.nodes.*;
import com.sun.jdi.*;
import com.zelaux.arcplugin.settings.*;
import org.jetbrains.annotations.*;

import static com.intellij.debugger.settings.NodeRendererSettings.createEnumerationChildrenRenderer;

public class CustomNodeRendererSettings{
    public static NodeRenderer seqRenderer, objectMapRenderer, objectMapEntryRenderer;

    private static LabelRenderer createLabelRenderer(@NonNls @Nullable String prefix, @NonNls String expressionText){
        LabelRenderer labelRenderer = new LabelRenderer();
        labelRenderer.setPrefix(prefix);
        labelRenderer.setLabelExpression(new TextWithImportsImpl(CodeFragmentKind.EXPRESSION, expressionText, "", JavaFileType.INSTANCE));
        return labelRenderer;
    }

    private static ExpressionChildrenRenderer createExpressionArrayChildrenRenderer(String expressionText,
                                                                                    String childrenExpandableText,
                                                                                    ArrayRenderer arrayRenderer){
        ExpressionChildrenRenderer renderer = createExpressionChildrenRenderer(expressionText, childrenExpandableText);
        renderer.setPredictedRenderer(arrayRenderer);
        return renderer;
    }

    public static ExpressionChildrenRenderer createExpressionChildrenRenderer(@NonNls String expressionText,
                                                                              @NonNls String childrenExpandableText){
        final ExpressionChildrenRenderer childrenRenderer = new ExpressionChildrenRenderer();
        childrenRenderer.setChildrenExpression(new TextWithImportsImpl(CodeFragmentKind.EXPRESSION, expressionText, "", JavaFileType.INSTANCE));
        if(childrenExpandableText != null){
            childrenRenderer.setChildrenExpandable(new TextWithImportsImpl(CodeFragmentKind.EXPRESSION, childrenExpandableText, "", JavaFileType.INSTANCE));
        }
        return childrenRenderer;
    }

    public static void setup(NodeRendererSettings instance, RendererConfiguration renderers){
//        instance.getCustomRenderers().addRenderer();
        NodeRenderer[] nodeRenderers = {seqRenderer = instance.createCompoundReferenceRenderer(
        "Seq", Seq.class.getName(),
        createLabelRenderer(" size = ", "size"),
        createExpressionArrayChildrenRenderer("toArray()", "any()", instance.getArrayRenderer())
        ), objectMapRenderer = instance.createCompoundReferenceRenderer(
        "ObjectMap", ObjectMap.class.getName(),
        createLabelRenderer(" size = ", "size"),
        createExpressionChildrenRenderer(Seq.class.getName() + ".with(this.entries())", "!isEmpty()")
        ),
        objectMapEntryRenderer = instance.createCompoundReferenceRenderer(
        "ObjectMap.Entry", Entry.class.getName(),
        new ObjectMapEntryLabelRenderer()/*createLabelRenderer(null, "\" \" + getKey() + \" -> \" + getValue()", null)*/,
        createEnumerationChildrenRenderer(new String[][]{{"key", "key"}, {"value", "value"}})
        )};
        for(NodeRenderer renderer : nodeRenderers){
            renderers.addRenderer(renderer);
        }
        updateState();
    }

    public static void updateState(){
        MySettingsState settings = MySettingsState.getInstance();
        CustomNodeRendererSettings.seqRenderer.setEnabled(settings.enabledDebugViewForSeq);
        CustomNodeRendererSettings.objectMapRenderer.setEnabled(settings.enabledDebugViewForObjectMap);
        CustomNodeRendererSettings.objectMapEntryRenderer.setEnabled(settings.enabledDebugViewForObjectMapEntry);
    }

    private static final class ObjectMapEntryLabelRenderer extends LabelRenderer
    implements ValueLabelRenderer, XValuePresentationProvider, OnDemandRenderer{
        private static final Key<Boolean> RENDERER_MUTED = Key.create("RENDERER_MUTED");
        private static final Key<ValueDescriptorImpl> KEY_DESCRIPTOR = Key.create("KEY_DESCRIPTOR");
        private static final Key<ValueDescriptorImpl> VALUE_DESCRIPTOR = Key.create("VALUE_DESCRIPTOR");

        private final ObjectMapEntryLabelRenderer.MyCachedEvaluator myKeyExpression = new ObjectMapEntryLabelRenderer.MyCachedEvaluator();
        private final ObjectMapEntryLabelRenderer.MyCachedEvaluator myValueExpression = new ObjectMapEntryLabelRenderer.MyCachedEvaluator();

        private ObjectMapEntryLabelRenderer(){
            super();
            myKeyExpression.setReferenceExpression(new TextWithImportsImpl(CodeFragmentKind.EXPRESSION, "this.key", "", JavaFileType.INSTANCE));
            myValueExpression.setReferenceExpression(new TextWithImportsImpl(CodeFragmentKind.EXPRESSION, "this.value", "", JavaFileType.INSTANCE));
        }

        @Override
        public String calcLabel(ValueDescriptor descriptor, EvaluationContext evaluationContext, DescriptorLabelListener listener) throws EvaluateException{
            if(!isShowValue(descriptor, evaluationContext)){
                descriptor.putUserData(RENDERER_MUTED, true);
                return "";
            }
            String keyText = calcExpression(evaluationContext, descriptor, myKeyExpression, listener, KEY_DESCRIPTOR);
            String valueText = calcExpression(evaluationContext, descriptor, myValueExpression, listener, VALUE_DESCRIPTOR);
            return keyText + " -> " + valueText;
        }

        private String calcExpression(EvaluationContext evaluationContext,
                                      ValueDescriptor descriptor,
                                      ObjectMapEntryLabelRenderer.MyCachedEvaluator evaluator,
                                      DescriptorLabelListener listener,
                                      Key<ValueDescriptorImpl> key) throws EvaluateException{
            Value eval = doEval(evaluationContext, descriptor.getValue(), evaluator);
            if(eval != null){
                WatchItemDescriptor evalDescriptor = new WatchItemDescriptor(
                evaluationContext.getProject(), evaluator.getReferenceExpression(), eval, (EvaluationContextImpl)evaluationContext){
                    @Override
                    public void updateRepresentation(EvaluationContextImpl context, DescriptorLabelListener labelListener){
                        updateRepresentationNoNotify(context, labelListener);
                    }
                };
                evalDescriptor.updateRepresentation((EvaluationContextImpl)evaluationContext, listener);
                descriptor.putUserData(key, evalDescriptor);
                return evalDescriptor.getValueLabel();
            }
            descriptor.putUserData(key, null);
            return "null";
        }

        @Override
        public String getUniqueId(){
            return "ObjectMapEntry renderer";
        }

        @NotNull
        @Override
        public String getLinkText(){
            return JavaDebuggerBundle.message("message.node.evaluate");
        }

        private Value doEval(EvaluationContext evaluationContext, Value originalValue, ObjectMapEntryLabelRenderer.MyCachedEvaluator cachedEvaluator)
        throws EvaluateException{
            final DebugProcess debugProcess = evaluationContext.getDebugProcess();
            if(originalValue == null){
                return null;
            }
            try{
                final ExpressionEvaluator evaluator = cachedEvaluator.getEvaluator(debugProcess.getProject());
                if(!debugProcess.isAttached()){
                    throw EvaluateExceptionUtil.PROCESS_EXITED;
                }
                final EvaluationContext thisEvaluationContext = evaluationContext.createEvaluationContext(originalValue);
                return evaluator.evaluate(thisEvaluationContext);
            }catch(final EvaluateException ex){
                throw new EvaluateException(JavaDebuggerBundle.message("error.unable.to.evaluate.expression") + " " + ex.getMessage(), ex);
            }
        }

        @NotNull
        @Override
        public XValuePresentation getPresentation(ValueDescriptorImpl descriptor){
            boolean inCollection = descriptor instanceof ArrayElementDescriptor;
            return new JavaValuePresentation(descriptor){
                @Override
                public void renderValue(@NotNull XValueTextRenderer renderer, @Nullable XValueNodeImpl node){
                    if(isMuted()){
                        return;
                    }
                    renderDescriptor(KEY_DESCRIPTOR, renderer, node);
                    renderer.renderComment(" -> ");
                    renderDescriptor(VALUE_DESCRIPTOR, renderer, node);
                }

                private void renderDescriptor(Key<ValueDescriptorImpl> key, @NotNull XValueTextRenderer renderer, @Nullable XValueNodeImpl node){
                    ValueDescriptorImpl valueDescriptor = myValueDescriptor.getUserData(key);
                    if(valueDescriptor != null){
                        String type = valueDescriptor.getIdLabel();
                        if(inCollection && type != null){
                            renderer.renderComment("{" + type + "} ");
                        }
                        new JavaValuePresentation(valueDescriptor).renderValue(renderer, node);
                    }else{
                        renderer.renderValue("null");
                    }
                }

                @NotNull
                @Override
                public String getSeparator(){
                    return inCollection ? "" : super.getSeparator();
                }

                @Override
                public boolean isShowName(){
                    return !inCollection;
                }

                @Nullable
                @Override
                public String getType(){
                    return inCollection && !isMuted() ? null : super.getType();
                }

                private boolean isMuted(){
                    return myValueDescriptor.getUserData(RENDERER_MUTED) != null && !OnDemandRenderer.isCalculated(myValueDescriptor);
                }
            };
        }

        private class MyCachedEvaluator extends CachedEvaluator{
            @Override
            protected String getClassName(){
                return ObjectMapEntryLabelRenderer.this.getClassName();
            }

            @Override
            public ExpressionEvaluator getEvaluator(Project project) throws EvaluateException{
                return super.getEvaluator(project);
            }
        }
    }
}

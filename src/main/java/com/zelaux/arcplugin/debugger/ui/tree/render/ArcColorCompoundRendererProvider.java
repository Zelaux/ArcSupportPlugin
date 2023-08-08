package com.zelaux.arcplugin.debugger.ui.tree.render;

import com.intellij.debugger.engine.*;
import com.intellij.debugger.engine.evaluation.*;
import com.intellij.debugger.ui.tree.render.*;
import com.intellij.openapi.diagnostic.*;
import com.intellij.ui.scale.*;
import com.intellij.util.ui.*;
import com.sun.jdi.*;
import com.zelaux.arcplugin.*;
import com.zelaux.arcplugin.settings.*;

import javax.swing.*;
import java.awt.*;

public class ArcColorCompoundRendererProvider extends CompoundRendererProvider{
    {
        System.out.println(getClass().getName());
    }
    @Override
    protected String getName(){
        return "Color";
    }

    @Override
    protected String getClassName(){
        return MetaData.Color.PATH;
    }

    @Override
    protected ValueIconRenderer getIconRenderer(){
        return (descriptor, evaluationContext, listener) -> {
            Value value = descriptor.getValue();
            if(value instanceof ObjectReference){
                try{
                    ObjectReference objRef = (ObjectReference)value;
                    ReferenceType refType = objRef.referenceType();
                    if(refType instanceof ClassType){
                        Method getRGBMethod = DebuggerUtils.findMethod(refType, "rgba", "()I");
                        if(getRGBMethod != null){
                            ReferenceType rgbMethodDeclaringType = getRGBMethod.declaringType();
                            if(rgbMethodDeclaringType.name().equals(MetaData.Color.PATH)){ // getRGB is not overridden
                                Field rField = rgbMethodDeclaringType.fieldByName("r");
                                Field gField = rgbMethodDeclaringType.fieldByName("g");
                                Field bField = rgbMethodDeclaringType.fieldByName("b");
                                Field aField = rgbMethodDeclaringType.fieldByName("a");
                                if(rField != null && gField != null && bField != null && aField != null){
                                    return createIcon(
                                    (FloatValue)objRef.getValue(rField),
                                    (FloatValue)objRef.getValue(gField),
                                    (FloatValue)objRef.getValue(bField),
                                    (FloatValue)objRef.getValue(aField)
                                    );
                                }
                            }
                        }
                    }
                }catch(Exception e){
                    throw new EvaluateException(e.getMessage(), e);
                }
            }
            return null;
        };
    }

    @Override
    protected boolean isEnabled(){
        return MySettingsState.getInstance().enabledDebugViewForColor;
    }

    private static final Logger LOG = Logger.getInstance(ArcColorCompoundRendererProvider.class);

    private static Icon createIcon(IntegerValue rgbValue){
        int r = ((rgbValue.value() & 0xff000000) >>> 24);
        int g = ((rgbValue.value() & 0x00ff0000) >>> 16);
        int b = ((rgbValue.value() & 0x0000ff00) >>> 8);
        int a = ((rgbValue.value() & 0x000000ff));
        //noinspection UseJBColor
        return JBUIScale.scaleIcon(new ColorIcon(16, 12, new Color(r, g, b, a), true));
    }

    private static Icon createIcon(FloatValue rValue, FloatValue gValue, FloatValue bValue, FloatValue aValue){
        float r = rValue.value();
        float g = gValue.value();
        float b = bValue.value();
        float a = aValue.value();
        //noinspection UseJBColor
        return JBUIScale.scaleIcon(new ColorIcon(16, 12, new Color(r, g, b, a), true));
    }
}

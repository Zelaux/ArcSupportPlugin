package com.zelaux.arcplugin.expressions.resolve.methods;

import arc.func.Cons2;
import arc.graphics.Color;
import com.zelaux.arcplugin.expressions.render.ArcColorExpressionRenderer;
import com.zelaux.arcplugin.expressions.render.exp.ComponentSetterRenderer;
import org.jetbrains.uast.UCallExpression;

public class SetComponentExpression extends ArcColorExpressionUCall {
    public final ComponentType componentType;


    public SetComponentExpression(UCallExpression uElement, ComponentType componentType, String tabTitle) {
        super(uElement, tabTitle);
        this.componentType = componentType;
    }

    public SetComponentExpression(UCallExpression element, ComponentType componentType) {
        this(element, componentType, componentType.title);
    }

    public Float currentValue() {
        return getParam(ParameterType.FLOAT, 0);
    }

    @Override
    public boolean apply(Color target) {
        Float currentValue = currentValue();
        if (currentValue == null) return false;
        componentType.setter.get(target, currentValue);

        return true;
    }

    @Override
    public ArcColorExpressionRenderer createRenderer() {
        return new ComponentSetterRenderer(this);
    }

    public enum ComponentType {
        r(Color::r),
        g(Color::g),
        b(Color::b),
        a(Color::a),

        ;
        public final Cons2<Color, Float> setter;
        public final String title = name() + "()";

        ComponentType(Cons2<Color, Float> setter) {
            this.setter = setter;
        }

    }
}

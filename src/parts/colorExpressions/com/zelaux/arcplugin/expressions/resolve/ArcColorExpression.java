package com.zelaux.arcplugin.expressions.resolve;

import arc.graphics.Color;
import com.intellij.java.JavaBundle;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.util.Alarm;
import com.zelaux.arcplugin.expressions.render.ArcColorExpressionRenderer;
import com.zelaux.arcplugin.utils.LazyValue;
import org.jetbrains.uast.UElement;

public abstract class ArcColorExpression extends Expression<ArcColorExpressionSequence> {

    public static final int ALARM_DELAY_MILLIS = 100;
    private final LazyValue<Alarm> myAlarm = LazyValue.create(Alarm::new);

    public ArcColorExpression(UElement uElement, Class<? extends UElement> uelementClass, String tabTitle) {
        super(uElement, uelementClass, tabTitle);
    }

    protected void executeAlarm(Runnable runnableInWriteContext) {
        Alarm alarm = myAlarm.get();
        alarm.cancelAllRequests();
        alarm.addRequest(() -> WriteAction.run(() -> {
            CommandProcessor.getInstance().executeCommand(getUElement().getSourcePsi().getProject(), runnableInWriteContext, JavaBundle.message("change.color.command.text"), null);
        }), ALARM_DELAY_MILLIS);
    }

    public ArcColorExpressionSequence asSequence() {
        ArcColorExpressionSequence sequence = new ArcColorExpressionSequence();
        sequence.add(this);
        return sequence;
    }

    public abstract boolean apply(Color target);

    public abstract ArcColorExpressionRenderer createRenderer();

}

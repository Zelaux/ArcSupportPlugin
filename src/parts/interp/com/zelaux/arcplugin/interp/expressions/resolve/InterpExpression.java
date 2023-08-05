package com.zelaux.arcplugin.interp.expressions.resolve;

import arc.math.Interp;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.util.Alarm;
import com.zelaux.arcplugin.expressions.resolve.Expression;
import com.zelaux.arcplugin.interp.expressions.render.InterpExpressionRenderer;
import com.zelaux.arcplugin.utils.LazyValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UExpression;

public abstract class InterpExpression extends Expression<InterpContainer> {

    public static final int ALARM_DELAY_MILLIS = 100;
    private final LazyValue<Alarm> myAlarm = LazyValue.create(Alarm::new);

    public InterpExpression(@NotNull UExpression element) {
        super(element, UExpression.class, "");
    }

    @Nullable
    public abstract Interp getInterpolation();

    protected void executeAlarm(Runnable runnableInWriteContext) {
        Alarm alarm = myAlarm.get();
        alarm.cancelAllRequests();
        alarm.addRequest(() -> WriteAction.run(() -> {
            CommandProcessor.getInstance().executeCommand(getUElement().getSourcePsi().getProject(), runnableInWriteContext, /*JavaBundle.message("change.color.command.text")*/"Change Interp", null);
        }), ALARM_DELAY_MILLIS);
    }

    @Override
    public InterpContainer asSequence() {
        return new InterpContainer(this);
    }

    @Override
    public InterpExpressionRenderer createRenderer() {
        return new InterpExpressionRenderer(this);
    }
}

package com.zelaux.arcplugin.expressions.resolve.methods;

import arc.func.Cons2;
import arc.struct.IntMap;
import com.zelaux.arcplugin.expressions.resolve.ArcColorExpression;
import com.zelaux.arcplugin.utils.UastExpressionUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.uast.UCallExpression;
import org.jetbrains.uast.UExpression;

public abstract class ArcColorExpressionUCall extends ArcColorExpression {
    public static final int RECEIVER = -1;
    private final IntMap<MyPair> alarmExecutors = new IntMap<>();
    public int parameterOffset = 0;
    IntMap<Object> tmpValues = new IntMap<>();
    private static class MyPair{
        public Object value;
        public Runnable updater;

        public MyPair set(Object value, Runnable updater) {
            this.value = value;
            this.updater = updater;
            return this;
        }

        public MyPair() {

        }
    }

    public ArcColorExpressionUCall(UCallExpression uElement, String tabTitle) {
        super(uElement, UCallExpression.class, tabTitle);
    }

    @SuppressWarnings("unchecked")
    public <T> T getParam(ParameterType<T> type, int index) {
        if (tmpValues.containsKey(index)) {
            return (T) tmpValues.get(index);
        }
        MyPair pair = alarmExecutors.get(index);
        if(pair!=null)return (T) pair.value;
        return type.calculate(getParamExpression(index));
    }

    public UExpression getParamExpression(int index) {
        index += parameterOffset;
        if (RECEIVER == index) {
            return callExpression().getReceiver();
        }
        return callExpression().getValueArguments().get(index);
    }

    public void setTmpValue(int paramIndex, Object value) {
        tmpValues.put(paramIndex, value);
    }

    public void resetTmpValues() {
        tmpValues.clear();
    }

    public UCallExpression callExpression() {
        return castElement();
    }

    public <T> void replaceParamExpression(int paramIndex, T newValue, Cons2<UExpression, T> updater) {
        alarmExecutors.get(paramIndex, MyPair::new).set(newValue,() -> updater.get(getParamExpression(paramIndex), newValue));
        executeAlarm(() -> {
            for (IntMap.Entry<MyPair> entry : alarmExecutors) {
                entry.value.updater.run();
            }
            alarmExecutors.clear();
            invalidateUElement();
        });
    }

    public interface ParameterType<T> {
        ParameterType<Integer> INT = UastExpressionUtils::getInt;
        ParameterType<Float> FLOAT = UastExpressionUtils::getFloat;
        ParameterType<Object> OBJECT = UastExpressionUtils::getObject;
        ParameterType<String> STRING = UastExpressionUtils::getString;

        @Nullable
        T calculate(UExpression expr);
    }
}

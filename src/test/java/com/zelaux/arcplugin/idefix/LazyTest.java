package com.zelaux.arcplugin.idefix;

import com.intellij.openapi.util.NotNullLazyValue;
import com.zelaux.arcplugin.util.NotNullLazyRecalculatableValue;
import org.junit.Assert;
import org.junit.Test;

public class LazyTest {
    @Test
    public void test() {
        int[] i={0};
        NotNullLazyValue<Integer> value = NotNullLazyRecalculatableValue.create(() -> {
            return ++i[0];
        });
        Assert.assertEquals(value.getValue().intValue(),1);
        Assert.assertEquals(value.getValue().intValue(),2);
        Assert.assertEquals(value.getValue().intValue(),3);
    }
}

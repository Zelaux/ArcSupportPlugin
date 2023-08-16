package com.zelaux.arcplugin.util;

import arc.func.Func;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.NotNullLazyValue;
import jdk.dynalink.beans.StaticClass;
import org.openjdk.nashorn.api.scripting.NashornScriptEngine;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptException;
import java.util.function.Supplier;

public class NotNullLazyRecalculatableValue {
    private static Func<Supplier,NotNullLazyValue> constructor;
    private static final Logger LOG=Logger.getInstance(NotNullLazyRecalculatableValue.class);
    static {

        NashornScriptEngine engine = (NashornScriptEngine) new NashornScriptEngineFactory().getScriptEngine();
        engine.put("Func", StaticClass.forClass(Func.class));
        engine.put("Prov", StaticClass.forClass(Supplier.class));
        engine.put("Lazy", StaticClass.forClass(NotNullLazyValue.class));
        try{
            //noinspection unchecked
            constructor= (Func<Supplier, NotNullLazyValue>) engine.eval("new Func{\n" +
                  "    get: function (sup) {\n" +
                  "        return new Lazy{\n" +
                  "            compute: function () {\n" +
                  "  //\n" +
                  "           return sup.get();\n" +
                  "            },\n" +
                  "            getValue: function (){\n" +
                  "                return sup.get();\n" +
                  "            }\n" +
                  "        };\n" +
                  "    " +
                  "}\n" +
                  "}");
        } catch (ScriptException e) {
            LOG.error(e);
            constructor=NotNullLazyValue::lazy;
        }
    }
    public static <T> NotNullLazyValue<T> create(Supplier<T> supplier){
return constructor.get(supplier);
    }
}

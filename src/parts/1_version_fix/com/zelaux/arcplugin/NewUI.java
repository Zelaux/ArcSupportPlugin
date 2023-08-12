package com.zelaux.arcplugin;

import arc.func.Boolp;
import com.intellij.openapi.diagnostic.Logger;
import org.openjdk.nashorn.api.scripting.NashornScriptEngine;
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory;

import javax.script.ScriptException;

public class NewUI {
    private static final Logger LOG = Logger.getInstance(NewUI.class);
    private static final Boolp checker;

    static {
        NashornScriptEngine engine = (NashornScriptEngine) new NashornScriptEngineFactory().getScriptEngine();
        engine.put("Boolp", jdk.dynalink.beans.StaticClass.forClass(Boolp.class));
        Boolp evaled;
        try {
            engine.put("UI_CLASS", jdk.dynalink.beans.StaticClass.forClass(
                    Class.forName("com.intellij.ui.NewUI")
            ));
            String script = "new Boolp{\n" +
                    "    get: function () {\n" +
                    "        " +
                    "return UI_CLASS.isEnabled();\n" +
                    "    }\n" +
                    "}";
            evaled = evalBoolpOrDefault(engine, script);
        } catch (ClassNotFoundException ignored) {
            try {
                engine.put("UI_CLASS", jdk.dynalink.beans.StaticClass.forClass(
                        Class.forName("com.intellij.ui" + (".") + "ExperimentalUI")
                ));
                String script = "new Boolp{\n" +
                        "    get: function () {\n" +
                        "        " +
                        "return UI_CLASS.isNewUI();\n" +
                        "    }\n" +
                        "}";
                evaled = evalBoolpOrDefault(engine, script);
            } catch (ClassNotFoundException e) {
                LOG.error(e);
                evaled = () -> false;
            }
        }
        checker = evaled;
    }

    public static boolean isEnabled() {
        return checker.get();
    }

    private static Boolp evalBoolpOrDefault(NashornScriptEngine engine, String script) throws ClassNotFoundException {
        try {
            return (Boolp) engine.eval(script);
        } catch (ScriptException e) {
            throw new ClassNotFoundException("", e);
        }
    }
}

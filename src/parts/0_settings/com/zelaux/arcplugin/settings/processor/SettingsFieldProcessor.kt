package com.zelaux.arcplugin.settings.processor

import com.intellij.util.Function
import com.intellij.util.ui.FormBuilder
import com.zelaux.arcplugin.settings.processor.ISettingComponent
import jdk.dynalink.beans.StaticClass
import org.openjdk.nashorn.api.scripting.NashornScriptEngineFactory
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.lang.invoke.MethodHandles
import java.lang.reflect.Modifier
import java.util.function.BiConsumer
import javax.script.ScriptContext
import javax.swing.JComponent

class SettingsFieldProcessor(
    stateClass: Class<*>,
) {
    private val descriptorList: MutableList<FieldDescriptor>

    @JvmField
    val settingComponentProv: ()-> ISettingComponent

    init {
        descriptorList = ArrayList()
        for (field in stateClass.declaredFields) {
            field.isAccessible = true
            val settingField = field.getAnnotation(SettingField::class.java) ?: continue
            require(!Modifier.isStatic(field.modifiers)) { "Setting field cannot be static" }
            require(!Modifier.isPrivate(field.modifiers)) { "Setting field cannot be private" }
            require(!Modifier.isFinal(field.modifiers)) { "Setting field cannot be final" }
            descriptorList.add(FieldDescriptor(field, settingField))
        }
        fun indentString(block: IndentStream.() -> Unit): String {
            val byteArrayOutputStream = ByteArrayOutputStream()
            val stream = IndentStream(PrintStream(byteArrayOutputStream))
            stream.apply(block)
            return byteArrayOutputStream.toString()
        }

        val tmpBinding = engine.createBindings().also{
            it.putAll(engine.getBindings(ScriptContext.ENGINE_SCOPE))
        };
        val binding = engine.createBindings();
        val descriptorPostfix = "\$descriptor"
        val javascript = indentString {
            engine.put("INTER",jdk.dynalink.beans.StaticClass.forClass(ISettingComponent::class.java))
            println("new INTER{")


            indent {
                for (desc in descriptorList) {
                    tmpBinding.clear()
                    tmpBinding.putAll(engine.getBindings(ScriptContext.ENGINE_SCOPE))
                    desc.field.type.let{type->tmpBinding.put(type.simpleName, StaticClass.forClass(type))}
                    binding["this_${desc.name}"] = engine.eval(desc.annotation.component, tmpBinding)
                    println("${desc.name}$descriptorPostfix: this_${desc.name},")
                }

                println("applyOn: function(OBJ){")
                indent {
                    for (desc in descriptorList) {
                        println("OBJ.${desc.name} = this.${desc.name}$descriptorPostfix.get();")
                    }
                }
                println("},")

                println("reset: function(OBJ){")
                indent {
                    for (desc in descriptorList) {
                        println("this.${desc.name}$descriptorPostfix.set(OBJ.${desc.name});")
                    }
                }
                println("},")

                println("panel: (function(){")
                indent {
                    println("return Java.type(\"${FormBuilder::class.java.name}\").createFormBuilder()")
                    indent {
                        for (field in descriptorList) {
                            if(field.separatorBefore)
                                println(".addSeparator()")
                            println(".addLabeledComponent(\"${field.annotation.title}\",this_${field.name}.component)")
                            if(field.separatorAfter)
                                println(".addSeparator()")
                        }
                        println(".addComponentFillVertically(new javax.swing.JPanel(), 0)\n" +
                                ".getPanel();")
                    }
                }
                println("})(),")

                println("getPanel: function(){")
                indent{
                    println("return this.panel;")
                }
                println("},")

                println("isModified: function(OBJ){")
                indent {
                    for (desc in descriptorList) {
                        println("if (this.${desc.name}$descriptorPostfix.get()!=OBJ.${desc.name}) return true;")
                    }
                    println("return false;")
                }
                println("}")
            }
            println("};")
        }
//        println("generated Java script:\n$javascript")
        engine.getBindings(ScriptContext.ENGINE_SCOPE).putAll(binding)
        settingComponentProv = { engine.eval(javascript) as ISettingComponent }
    }


    private fun interface CreateDescriptorI {
        fun create(
            component: JComponent?,
            getter: Function<JComponent?, Any?>?,
            setter: BiConsumer<JComponent?, Any?>?,
        ): JComponentDescriptor?
    }

    companion object {
        val engine = NashornScriptEngineFactory().scriptEngine
        private val lookup = MethodHandles.lookup()

        init {
            engine.put(
                "descriptor",
                CreateDescriptorI { component: JComponent?, getter: Function<JComponent?, Any?>?, setter: BiConsumer<JComponent?, Any?>? ->
                    JComponentDescriptor(
                        component,
                        getter,
                        setter
                    )
                })
            DefaultDescriptors.register(engine)
        }
    }
}

package com.zelaux.arcplugin.colorExpression.methods

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.ui.colorpicker.LightCalloutPopup
import com.zelaux.arcplugin.MetaData
import com.zelaux.arcplugin.psi.PrimitiveType
import com.zelaux.arcplugin.ui.JPanelBuilder
import com.zelaux.arcplugin.ui.components.GradientComponent
import com.zelaux.arcplugin.utils.CustomUastTreeUtil
import com.zelaux.arcplugin.utils.UastExpressionUtils
import com.zelaux.arcplugin.utils.WrongExpressionTypeException
import org.jetbrains.uast.*

object gradient : ParserFunc() {
    override fun load() {
        "lerp"(MetaData.Color.PATH, PrimitiveType.FLOAT.canonicalText) { GradientColorParser("lerp(Color,float)", it) }
    }
}

open class GradientColorParser(override val tabTitle: String, expression: UCallExpression) : ColorCallExpressionParser(expression), OffsetableParser {
    protected open val innerColor by lazy<ColorExpressionParserWrapper?> {
        if (offset == 0) {
            return@lazy null
        } else {
            var tabTitle = "first color"
            expression.resolve()?.let {
                tabTitle = it.parameterList.parameters[offset - 1].name
            }
            return@lazy StaticSetColorParser(expression).offset<ColorExpressionParser>(offset - 1).wrap(tabTitle)
        }
    }


    private val secondColor by lazy<ColorExpressionParserWrapper> {
        var tabTitle = "second color"
        expression.resolve()?.let {
            tabTitle = it.parameterList.parameters[offset].name
        }
        StaticSetColorParser(expression).offset<ColorExpressionParser>(offset).wrap(tabTitle)
    }

    companion object {
        private val tmpColor = arcColor()
    }

    override fun apply(color: arcColor): Boolean {
        innerColor?.apply(color)
        val rgba = color.rgba()
        secondColor.apply(tmpColor)
        val progress = progress() ?: return false
        color.set(rgba).lerp(tmpColor, progress)
        return true;
    }

    protected fun progress() = try {
        UastExpressionUtils.getFloat(expression.valueArguments[offset + 1])
    } catch (wrongExpressionType: WrongExpressionTypeException) {
        null
    }

    private val innerSequence by lazy<ColorExpParserSequence> {
        ColorExpParserSequence().also {
            val innerColor = innerColor
            if (innerColor != null) {
                it.list.add(innerColor)
            }
            it.list.add(secondColor);

            var tabTitle = "float"
            expression.resolve()?.let { psiMethod ->
                tabTitle = psiMethod.parameterList.parameters[offset + 1].name
            }
            it.list.add(this.wrap(tabTitle))
        }
    }

    //private var buildingSequence=false;
    override fun getTabComponent(project: Project, popupRef: Ref<LightCalloutPopup>, sequence: ColorExpParserSequence, sequenceManager: Ref<ColorExpSequenceManager>, writable: Boolean): JPanelBuilder {
        val colorA = tmpColor.also { sequence.stopColorOn(this, it); innerColor?.apply(it) }.awtColor()
        val colorB = tmpColor.also { secondColor.apply(it) }.awtColor()
        val progress = progress();

        /*  if (!buildingSequence) {
  //            val listView = sequenceManager.get().listView
             *//* buildingSequence=true;
            val builder = innerSequence.getJPanelBuilder(project, popupRef, writable)
            buildingSequence=false;
            return builder;*//*

        }*/
        val hasProgress = progress != null;
        return JPanelBuilder().apply {
            run {
                fun JPanelBuilder.addSetField(parserWrapper: ColorExpressionParserWrapper) {
                    val innerSetColor: StaticSetColorParser = parserWrapper.expressionParser as? StaticSetColorParser ?: return
                    if (innerSetColor.fieldsAmount > 1 && innerSetColor.targetExpression.let { it is UResolvable && it.resolveToUElement() is UField }) {
                        addComponent(innerSetColor.getTabComponent(project, popupRef, sequence, sequenceManager, writable).buildJPanel())
                    }
                }

                val innerColor = innerColor
                if (innerColor != null) {
                    addSetField(innerColor)
                }
                addSetField(secondColor)
            }
            addComponent(GradientComponent(colorA, colorB, drawKnob = hasProgress, editable = hasProgress).apply GradientComponent@{
//                preferredSize = sequence.sliderSize
                size = sequence.sliderSize
//                maximumSize=sequence.sliderSize
                setProgressColorValue(progress ?: 0f)
                sequenceManager.get().registerListener(this@GradientColorParser) {
                    this@GradientComponent.colorA = tmpColor.also { sequence.stopColorOn(this@GradientColorParser, it); innerColor?.apply(it) }.awtColor()
                    this@GradientComponent.colorB = tmpColor.also { secondColor.apply(it) }.awtColor()
                    this@GradientComponent.setProgressColorValue(progress() ?: 0f)
                }
                if (hasProgress) {
                    var updating = false
                    addProgressListener { progress ->
                        if (updating) return@addProgressListener
                        updating = true;
                        writeColorAction(project) {
                            val expr = expression.valueArguments
                            val delta = UastExpressionUtils.replaceFloat(expr[offset+1], progress)
                            CustomUastTreeUtil.ChangeAppliedListener(progress,{
                                UastExpressionUtils.getObject(expression.valueArguments[offset+1])
                            }){
                                updating=false
                                sequenceManager.get().fireUpdate(this@GradientColorParser)
                            }
//                            element = sourcePsi.toUElement()!!
//                            element = CustomUastTreeUtil.restore(psiFile, prevrange, delta)
//                    updateCallback.get()(this@ShiftHsvComponent)
                        }
                    }
                }
            })
        }
    }


}
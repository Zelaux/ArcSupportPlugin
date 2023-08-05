package com.zelaux.arcplugin.colorExpression.drawmethods

import arc.graphics.Color
import com.intellij.openapi.util.Ref
import com.zelaux.arcplugin.MetaData
import com.zelaux.arcplugin.expressions.render.ArcColorExpressionRenderSettings
import com.zelaux.arcplugin.expressions.render.ArcColorExpressionRenderer
import com.zelaux.arcplugin.expressions.render.exp.LerpColorParserRenderer
import com.zelaux.arcplugin.expressions.resolve.ArcColorExpression
import com.zelaux.arcplugin.expressions.resolve.Expression.ExpressionEntryPoint
import com.zelaux.arcplugin.expressions.resolve.methods.*
import com.zelaux.arcplugin.psi.PrimitiveType
import org.jetbrains.uast.UCallExpression

@Suppress("UseJBColor")
object drawmethods : DrawRegister(MetaData.Draw.PATH) {
    override fun load() {
        open class WithAlpha(expr: UCallExpression, tabName: String) :
                LerpExpression(expr, tabName), ExpressionEntryPoint {

            override fun isEntryPoint(): Boolean {
                return true
            }

            override fun apply(target: Color): Boolean {
                secondColor.get().apply(target)
                target.a = 0f
                return super.apply(target)
            }
            override fun createRenderer(): ArcColorExpressionRenderer {
                return object: LerpColorParserRenderer(this) {
                    override fun calculateColorA(param: Ref<ArcColorExpressionRenderSettings>?, tmpColor: Color?): java.awt.Color {
                        val c= super.calculateColorA(param, tmpColor)
                        return java.awt.Color(c.red,c.green,c.blue,0)
                    }
                }
            }
        }
        for (method in arrayOf("mixcol", "color", "tint")) {
            if (method != "tint") method(MetaData.Color.PATH, PrimitiveType.FLOAT.canonicalText) {
                WithAlpha(it, "$method(Color,float)")
            }
            method(*MetaData.Color.PATH * 2 + PrimitiveType.FLOAT.canonicalText) {
                LerpExpression(it, method).also { it.parameterOffset = 1 }
            }
        }

        for (tab in arrayOf("color", "tint")) {
            tab(MetaData.Color.PATH) {
                StaticSetColorExpression(it, UCallExpression::class.java, tab + "(Color)") {
                    (it as UCallExpression).valueArguments[0]
                }
            }
        }
        "color"(PrimitiveType.INT) {
            SetIntExpression(it, "color(int)", true)
        }

        "color"(*PrimitiveType.FLOAT * 3) {
            SetFloatsExpression(it, false, "color")
        }
        "color"(*PrimitiveType.FLOAT * 4) { SetFloatsExpression(it, true, "color") }
//        "color"(*emptyArray<String>()){ }TODO

        "colorl"(PrimitiveType.FLOAT) { GraysExpression(it, "colorl(float)") }
        "colorl"(*PrimitiveType.FLOAT * 2) {
            object : WithAlpha(it, "colorl(float,float)") {
                override fun getStaticSetColorParser(index: Int): ArcColorExpression? {
                    val self=this;
                    if (index == 0) return object: GraysExpression(castElement(), "NONE"){
                        override fun invalidateUElement() {
                            super.invalidateUElement()
                            self.invalidateUElement();
                        }}

                    return super.getStaticSetColorParser(index)
                }

            }
        }
    }
}
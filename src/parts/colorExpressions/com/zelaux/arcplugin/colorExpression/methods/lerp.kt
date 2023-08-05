package com.zelaux.arcplugin.colorExpression.methods

import com.zelaux.arcplugin.MetaData
import com.zelaux.arcplugin.expressions.resolve.ArcColorExpression

import com.zelaux.arcplugin.expressions.resolve.methods.LerpExpression
import com.zelaux.arcplugin.expressions.resolve.methods.SetFloatsExpression
import com.zelaux.arcplugin.psi.PrimitiveType
import org.jetbrains.uast.UCallExpression

object lerp : ColorRegister() {
    override fun load() {
        "lerp"(MetaData.Color.PATH, PrimitiveType.FLOAT.canonicalText) { LerpExpression(it, "lerp(Color,float)") }
        class Lerp(callExpression: UCallExpression) : LerpExpression(callExpression, "lerp(float,float,float,float,float)") {
            init{
                parameterOffset=3;
            }
            override fun getStaticSetColorParser(index: Int): ArcColorExpression {
                if(index==0){
                    return object: SetFloatsExpression(castElement(),"",true){
                        override fun invalidateUElement() {
                            super.invalidateUElement()
                            this@Lerp.invalidateUElement()
                        }
                    }
                }
                return super.getStaticSetColorParser(index)
            }
        }
        "lerp"(*PrimitiveType.FLOAT * 5) { Lerp(it) }
    }
}
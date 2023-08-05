package com.zelaux.arcplugin.colorExpression.drawmethods

import com.zelaux.arcplugin.MetaData
import com.zelaux.arcplugin.colorExpression.ClassQualifier
import com.zelaux.arcplugin.colorExpression.ParserRegisterDSL

abstract class DrawRegister(path:String):ParserRegisterDSL(ClassQualifier(path)) {
    companion object {
        fun register() {
            drawmethods.load()
            linesmethods.load()
        }
    }

}
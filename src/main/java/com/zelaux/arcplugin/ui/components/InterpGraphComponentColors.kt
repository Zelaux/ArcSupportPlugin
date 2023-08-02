package com.zelaux.arcplugin.ui.components

import com.intellij.ui.Gray
import com.intellij.ui.JBColor
import java.awt.Color

class InterpGraphComponentColors {
    val mainLineColor: Color= JBColor(Color(50, 50, 180),Color(180, 50, 50))
    val backgroundColor: Color get() = JBColor.background()//JBColor(Gray._252, Gray._64)
    val axisLineColor: Color = JBColor.black
    val mainGridColor: Color = JBColor(Gray._80, Gray._176)
    val subGridColor: Color = JBColor.gray
    val mainMarkersColor: Color = JBColor.darkGray
    val fontColor: Color = JBColor.black
}

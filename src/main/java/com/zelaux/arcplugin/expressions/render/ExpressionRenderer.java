package com.zelaux.arcplugin.expressions.render;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.ui.colorpicker.LightCalloutPopup;
import com.zelaux.arcplugin.ui.JPanelBuilder;

public interface ExpressionRenderer<SEQUENCE_TYPE,PARAM> {
     JPanelBuilder getTabComponent(
            Project project,
            Ref<LightCalloutPopup> popupRef,
            Ref<PARAM> paramRef
    ) ;
}

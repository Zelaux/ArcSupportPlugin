package com.zelaux.arcplugin.settings;

import arc.files.*;
import com.intellij.ui.components.*;
import com.intellij.util.ui.*;

import javax.swing.*;

public class ArcPluginSettingsComponent{

    private final JPanel myMainPanel;
    //    private final JBTextField myUserNameText = new JBTextField();
    private final JBCheckBox myColorExprSeqAsList = new JBCheckBox("View color expression sequence as list? ");

    private final JBCheckBox myEnabledDebugViewForObjectMap = new JBCheckBox("Special debug view for arc.struct.ObjectMap? ");
    private final JBCheckBox myEnabledDebugViewForObjectMapEntry = new JBCheckBox("Special debug view for arc.struct.ObjectMap.Entry? ");
    private final JBCheckBox myEnabledDebugViewForSeq = new JBCheckBox("Special debug view for arc.struct.Seq? ");
    private final JBCheckBox myEnabledDebugViewForFi = new JBCheckBox("Special debug view for arc.files.Fi? ");
    private final JBCheckBox myEnabledDebugViewForColor = new JBCheckBox("Special debug view for arc.graphics.Color? ");


    public ArcPluginSettingsComponent(){
        myMainPanel = FormBuilder.createFormBuilder()
//        .addLabeledComponent(new JBLabel("Enter user name: "), myUserNameText, 1, false)
        .addComponent(myEnabledDebugViewForObjectMap, 1)
        .addComponent(myEnabledDebugViewForObjectMapEntry, 1)
        .addComponent(myEnabledDebugViewForSeq, 1)
        .addComponent(myEnabledDebugViewForFi, 1)
        .addComponent(myEnabledDebugViewForColor, 1)
        .addSeparator(1)
        .addComponent(myColorExprSeqAsList, 1)
        .addComponentFillVertically(new JPanel(), 0)
        .getPanel();
    }

    public JPanel getPanel(){
        return myMainPanel;
    }

    public JComponent getPreferredFocusedComponent(){
        return myColorExprSeqAsList;
    }


    public boolean getViewColorExprSeqAsList(){
        return myColorExprSeqAsList.isSelected();
    }

    public void setViewColorExprSeqAsList(boolean newStatus){
        myColorExprSeqAsList.setSelected(newStatus);
    }

    public boolean getEnabledDebugViewForObjectMap(){
        return myEnabledDebugViewForObjectMap.isSelected();
    }

    public void setEnabledDebugViewForObjectMap(boolean newStatus){
        myEnabledDebugViewForObjectMap.setSelected(newStatus);
    }

    public boolean getEnabledDebugViewForObjectMapEntry(){
        return myEnabledDebugViewForObjectMapEntry.isSelected();
    }

    public void setEnabledDebugViewForObjectMapEntry(boolean newStatus){
        myEnabledDebugViewForObjectMapEntry.setSelected(newStatus);
    }

    public boolean getEnabledDebugViewForSeq(){
        return myEnabledDebugViewForSeq.isSelected();
    }

    public void setEnabledDebugViewForSeq(boolean newStatus){
        myEnabledDebugViewForSeq.setSelected(newStatus);
    }

    public boolean getEnabledDebugViewForFi(){
        return myEnabledDebugViewForFi.isSelected();
    }

    public void setEnabledDebugViewForFi(boolean newStatus){
        myEnabledDebugViewForFi.setSelected(newStatus);
    }

    public boolean getEnabledDebugViewForColor(){
        return myEnabledDebugViewForColor.isSelected();
    }

    public void setEnabledDebugViewForColor(boolean newStatus){
        myEnabledDebugViewForColor.setSelected(newStatus);
    }
}

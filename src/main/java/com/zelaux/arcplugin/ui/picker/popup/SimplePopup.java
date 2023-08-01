package com.zelaux.arcplugin.ui.picker.popup;

import com.intellij.ide.*;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.project.*;
import com.intellij.openapi.ui.*;
import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.registry.*;
import com.intellij.ui.*;
import com.intellij.ui.awt.*;
import com.intellij.ui.colorpicker.*;
import com.zelaux.arcplugin.ui.*;
import kotlin.jvm.functions.*;
import org.jetbrains.annotations.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public abstract class SimplePopup<CALLBACK>{
    public static final SimplePopup<JPanelBuilder> referencePopup = new SimplePopupImpl();
    public static final SimplePopup<Function1<Ref<LightCalloutPopup>, JPanelBuilder>> lambdaPopup = new LambdaPopupImpl();

    @Nullable
    public static RelativePoint bestLocationForColorPickerPopup(@Nullable Editor editor){
        if(editor == null || editor.isDisposed()){
            return null;
        }
        AWTEvent event = IdeEventQueue.getInstance().getTrueCurrentEvent();
        if(event instanceof MouseEvent){
            Component component = ((MouseEvent)event).getComponent();
            Component clickComponent = SwingUtilities.getDeepestComponentAt(component, ((MouseEvent)event).getX(), ((MouseEvent)event).getY());
            if(clickComponent instanceof EditorGutter){
                return null;
            }
        }
        VisualPosition visualPosition = editor.getCaretModel().getCurrentCaret().getVisualPosition();
        Point pointInEditor = editor.visualPositionToXY(new VisualPosition(visualPosition.line + 1, visualPosition.column));
        return new RelativePoint(editor.getContentComponent(), pointInEditor);
    }

    public static boolean showPopup(@Nullable Project project, @NotNull JPanelBuilder builder, @Nullable Editor editor){
        return showPopup(project, referencePopup, builder, editor);
    }

    public static <T> boolean showPopup(@Nullable Project project, @Nullable SimplePopup<T> simplePopup, T callback, @Nullable Editor editor){
        return showPopup(project, simplePopup, callback, bestLocationForColorPickerPopup(editor));
    }

    public static boolean showPopup(@Nullable Project project, @NotNull JPanelBuilder builder){
        return showPopup(project, referencePopup, builder);
    }

    private static <T> boolean showPopup(@Nullable Project project, @NotNull SimplePopup<T> simplePopup, T builder){
        return showPopup(project, simplePopup, builder, (Editor)null);

    }

    public static <T> boolean showPopup(@Nullable final Project project, @NotNull JPanelBuilder builder, @Nullable RelativePoint location){
        return showPopup(project, referencePopup, builder, location);
    }

    public static <T> boolean showPopup(@Nullable final Project project, @Nullable SimplePopup<T> simplePopup, T callback, @Nullable RelativePoint location){
        if(!isEnoughSpaceToShowPopup() /*|| !Registry.is("ide.new.color.picker")*/){
//            showDialog(currentColor, listener, showAlpha, showAlphaAsPercent);
            return false;
        }
        Ref<LightCalloutPopup> ref = Ref.create();
        JPanelBuilder builder = simplePopup.builder(project, callback, ref);

        if(!builder.hasFocus()){
            builder.withFocus();
        }
        builder.focusWhenDisplay(true)
        .setFocusCycleRoot(true)
        .addKeyAction(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelPopup(ref))
        .addKeyAction(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), applyColor(ref));
        LightCalloutPopup popup = builder.buildLightCalloutPopup();
        ref.set(popup);
        if(location == null){
            location = new RelativePoint(MouseInfo.getPointerInfo().getLocation());
        }
        popup.show(location.getScreenPoint());
        simplePopup.updatePointer(ref);
        return true;
    }

    @NotNull
    static AbstractAction cancelPopup(Ref<LightCalloutPopup> ref){
        return new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e){
                final LightCalloutPopup popup = ref.get();
                if(popup != null){
                    popup.cancel();
                }
            }
        };
    }

    @NotNull
    static AbstractAction applyColor(Ref<LightCalloutPopup> ref){
        return new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent e){
                final LightCalloutPopup popup = ref.get();
                if(popup != null){
                    popup.close();
                }
            }
        };
    }

    protected static boolean isEnoughSpaceToShowPopup(){
        DialogWrapper currentDialog = DialogWrapper.findInstanceFromFocus();
        if(currentDialog != null && (currentDialog.getWindow().getWidth() < 500 || currentDialog.getWindow().getHeight() < 500)){
            return false;
        }
        return true;
    }

    public boolean show(@Nullable final Project project, CALLBACK callback, @Nullable RelativePoint location){
        return showPopup(project, this, callback, location);
    }

    public boolean show(@Nullable final Project project, CALLBACK callback){
        return showPopup(project, this, callback);
    }

    public boolean show(@Nullable final Project project, @Nullable Editor editor, CALLBACK callback){
        return showPopup(project, this, callback, editor);
    }

    public boolean show(CALLBACK callback){
        return showPopup(null, this, callback);
    }

    protected void updatePointer(Ref<LightCalloutPopup> ref){
        LightCalloutPopup popup = ref.get();
        Balloon balloon = popup.getBalloon();
        if(balloon instanceof BalloonImpl){
            RelativePoint showingPoint = ((BalloonImpl)balloon).getShowingPoint();
            ((BalloonImpl)balloon).setHideOnClickOutside(true);
            /*Color c = popup.getPointerColor(showingPoint, ((BalloonImpl)balloon).getComponent());
            if (c != null) {
                c = ColorUtil.withAlpha(c, 1.0); //clear transparency
            }
            ((BalloonImpl)balloon).setPointerColor(c);*/
        }
    }

    public abstract JPanelBuilder builder(Project project, CALLBACK callback, Ref<LightCalloutPopup> ref);

}

class   LambdaPopupImpl extends SimplePopup<Function1<Ref<LightCalloutPopup>, JPanelBuilder>>{

    @Override
    public JPanelBuilder builder(Project project, Function1<Ref<LightCalloutPopup>, JPanelBuilder> function, Ref<LightCalloutPopup> ref){
        return function.invoke(ref);
    }
}

class SimplePopupImpl extends SimplePopup<JPanelBuilder>{
    @Override
    public JPanelBuilder builder(Project project, JPanelBuilder builder, Ref<LightCalloutPopup> ref){
        return builder;
    }
}
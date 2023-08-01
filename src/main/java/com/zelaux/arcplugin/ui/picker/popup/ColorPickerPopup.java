package com.zelaux.arcplugin.ui.picker.popup;

import com.intellij.ide.*;
import com.intellij.ide.util.*;
import com.intellij.openapi.*;
import com.intellij.openapi.application.*;
import com.intellij.openapi.command.*;
import com.intellij.openapi.project.*;
import com.intellij.openapi.ui.popup.*;
import com.intellij.openapi.util.*;
import com.intellij.openapi.util.registry.*;
import com.intellij.openapi.util.text.*;
import com.intellij.ui.*;
import com.intellij.ui.awt.*;
import com.intellij.ui.colorpicker.*;
import com.intellij.ui.picker.ColorPipette;
import com.intellij.ui.picker.*;
import com.intellij.util.*;
import com.zelaux.arcplugin.ui.colorpicker.*;
import org.jetbrains.annotations.*;

import java.awt.*;
import java.util.List;
import java.util.*;

public abstract class ColorPickerPopup<CALLBACK_TYPE> extends SimplePopup<CALLBACK_TYPE>{
    //    private static final String COLOR_CHOOSER_COLORS_KEY = "GrayScaleColorChooser.RecentColors";
    private static final String HSB_PROPERTY = "color.picker.is.hsb";
    public static boolean shouldUpdatePointer = false;
    protected final RecentColorsComponent recentColorsComponent;
    public boolean showAlpha;
    public boolean showAlphaAsPercent;
    public Color currentColor;

    protected ColorPickerPopup(String colorChooserColorsKey){
        this.recentColorsComponent = new RecentColorsComponent(colorChooserColorsKey);
    }

    @Nullable
    private ColorPipette getPipetteIfAvailable(@NotNull ColorPipette pipette, @NotNull Disposable parentDisposable){
        if(pipette.isAvailable()){
            Disposer.register(parentDisposable, pipette);
            return pipette;
        }else{
            Disposer.dispose(pipette);
            return null;
        }
    }

    public ColorPickerPopup<CALLBACK_TYPE> set(boolean alpha, Color currentColor){
        this.showAlpha = alpha;
        this.currentColor = currentColor;
        return this;
    }

    protected abstract void getColorChanged(@NotNull CALLBACK_TYPE listener, Color color, Object source);

    protected abstract void showDialog(@NotNull CALLBACK_TYPE listener);

    protected abstract @NotNull ColorPickerJPanelBuilder colorPickerBuilder(Ref<LightCalloutPopup> ref, CALLBACK_TYPE inputData);

    @Override
    protected void updatePointer(Ref<LightCalloutPopup> ref){
        if(!shouldUpdatePointer) return;
        LightCalloutPopup popup = ref.get();
        Balloon balloon = popup.getBalloon();
        if(balloon instanceof BalloonImpl){
            RelativePoint showingPoint = ((BalloonImpl)balloon).getShowingPoint();
            Color c = popup.getPointerColor(showingPoint, ((BalloonImpl)balloon).getComponent());
            if(c != null){
                c = ColorUtil.withAlpha(c, 1.0); //clear transparency
            }
            ((BalloonImpl)balloon).setPointerColor(c);
        }
    }

    @Override
    public final ColorPickerJPanelBuilder builder(Project project, CALLBACK_TYPE callback_type, Ref<LightCalloutPopup> ref){
        ColorPickerJPanelBuilder builder = colorPickerBuilder(ref, callback_type);

        ColorListener colorListener = new ColorListener(){
            final Object groupId = new Object();
            final Alarm alarm = new Alarm();

            @Override
            public void colorChanged(final Color color, final Object source){
                Runnable apply = () -> CommandProcessor.getInstance().executeCommand(project,
                () -> getColorChanged(callback_type, color, source),
                IdeBundle.message("command.name.apply.color"),
                groupId);
                alarm.cancelAllRequests();
                Runnable request = () -> ApplicationManager.getApplication().invokeLaterOnWriteThread(apply);
                if(source instanceof ColorPipetteButton && ((ColorPipetteButton)source).getCurrentState() == ColorPipetteButton.PipetteState.UPDATING){
                    alarm.addRequest(request, 150);
                }else{
                    request.run();
                }
            }
        };
        builder.addColorListener(colorListener, true);
        return builder;
    }


    protected final class RecentColorsComponent{
        private final String COLOR_CHOOSER_COLORS_KEY;


        private RecentColorsComponent(String color_chooser_colors_key){
            COLOR_CHOOSER_COLORS_KEY = color_chooser_colors_key;
        }

        @SuppressWarnings("UseJBColor")
        public List<Color> getRecentColors(){
            final String value = PropertiesComponent.getInstance().getValue(COLOR_CHOOSER_COLORS_KEY);
            if(value != null){
                final List<String> colors = StringUtil.split(value, ",,,");
                ArrayList<Color> recentColors = new ArrayList<>();
                for(String color : colors){
                    if(color.contains("-")){
                        List<String> components = StringUtil.split(color, "-");
                        if(components.size() == 4){
                            recentColors.add(new Color(Integer.parseInt(components.get(0)),
                            Integer.parseInt(components.get(1)),
                            Integer.parseInt(components.get(2)),
                            Integer.parseInt(components.get(3))));
                        }
                    }else{
                        recentColors.add(new Color(Integer.parseInt(color)));
                    }
                }
                return recentColors;
            }
            return Collections.emptyList();
        }

        protected void saveRecentColors(List<Color> recentColors){
            final List<String> values = new ArrayList<>();
            for(Color recentColor : recentColors){
                if(recentColor == null) break;
                values
                .add(String.format("%d-%d-%d-%d", recentColor.getRed(), recentColor.getGreen(), recentColor.getBlue(), recentColor.getAlpha()));
            }

            PropertiesComponent.getInstance().setValue(COLOR_CHOOSER_COLORS_KEY, values.isEmpty() ? null : StringUtil.join(values, ",,,"), null);
        }

        protected List<Color> appendColor(Color color, List<Color> recentColors, int maxSize){
            ArrayList<Color> colors = new ArrayList<>(recentColors);
            colors.remove(color);
            colors.add(0, color);

            if(colors.size() > maxSize){
                colors = new ArrayList<>(recentColors.subList(0, maxSize));
            }
            return colors;
        }

    }

}


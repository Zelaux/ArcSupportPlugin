package com.zelaux.arcplugin.parsers.colorexpr

import arc.util.Tmp
import com.intellij.codeInsight.daemon.MergeableLineMarkerInfo
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Ref
import com.intellij.openapi.util.TextRange
import com.intellij.ui.colorpicker.LightCalloutPopup
import com.intellij.ui.colorpicker.PICKER_PREFERRED_WIDTH
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.scale.JBUIScale
import com.intellij.ui.tabs.TabInfo
import com.intellij.ui.tabs.TabsListener
import com.intellij.util.ui.ColorIcon
import com.intellij.util.ui.ColorsIcon
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import com.zelaux.arcplugin.actions.ActionUtils
import com.zelaux.arcplugin.actions.CustomEntryPointActionGroups
import com.zelaux.arcplugin.marker.colorexp.ExpressionSequenceParserLineMarkerProvider
import com.zelaux.arcplugin.parsers.ExpressionParserSequence
import com.zelaux.arcplugin.settings.ArcPluginSettingsState
import com.zelaux.arcplugin.ui.JPanelBuilder
import com.zelaux.arcplugin.ui.colorparser.ColorExpressionParserTabs
import com.zelaux.arcplugin.ui.components.ColorComponent
import com.zelaux.arcplugin.ui.picker.popup.SimplePopup
import java.awt.Color
import java.awt.Dimension
import java.util.function.IntFunction
import javax.swing.Icon
import javax.swing.JPanel
import javax.swing.SwingConstants

class ColorExpParserSequence : ExpressionParserSequence<ColorExpSequenceManager> {
    override val iconColor: Color
        get() = resultColor
    val resultColor get() = tmpColor1.set(0xFF).apply { apply(this) }.awtColor()
    val list = ArrayList<ColorExpressionParser>()
    val startIndex get() = Math.max(0,list.indexOfFirst { !it.pointless })
    override val textRange: TextRange get() = list[list.size - 1].element.sourcePsi!!.textRange
    override fun isEmpty(): Boolean=list.isEmpty()

    override val icon: Icon
        get() = JBUIScale.scaleIcon(ColorIcon(12, resultColor))
    val sliderSize
        get() = if (ArcPluginSettingsState.getInstance().viewColorExprSeqAsList)
            Dimension(PICKER_PREFERRED_WIDTH, 150 / 4)
        else {
            Dimension(PICKER_PREFERRED_WIDTH, 150 / 2)
        }

    companion object {
        private val tmpColor1 = arcColor()

    }

    fun stopColorOn(parser: ColorExpressionParser, color: arcColor = Tmp.c1.set(arcColor.black)): arcColor {
        for (i in startIndex until list.size) {
            if (list[i] == parser) return color
            list[i].apply(color)
        }
        throw IllegalArgumentException()
    }

    fun apply(color: arcColor) {
        for (i in startIndex until list.size) {
            list[i].apply(color)
        }
    }


    fun getJPanelBuilder(
        project: Project,
        ref: Ref<LightCalloutPopup>,
        writable: Boolean,
        sequenceManager: Ref<ColorExpSequenceManager> = Ref.create(ColorExpSequenceManager(ArcPluginSettingsState.getInstance().viewColorExprSeqAsList))
    ): JPanelBuilder {
        if (list.size == 1) {
            return list[0].getTabComponent(project, ref, this, sequenceManager, writable)
        }
        if (sequenceManager.get().listView) {
            return listBuilder(project, ref, writable, sequenceManager)
        }
        return tabsBuilder(project, ref, writable, sequenceManager)
    }


    private fun listBuilder(
        project: Project,
        ref: Ref<LightCalloutPopup>,
        writable: Boolean,
        sequenceManager: Ref<ColorExpSequenceManager> = Ref.create(ColorExpSequenceManager(ArcPluginSettingsState.getInstance().viewColorExprSeqAsList))
    ) = JPanelBuilder().apply jpanelBuilder@{
//        JBScrollPane(FormBuilder.createFormBuilder())

//        addSeparator()
        JBScrollPane(
            JPanelBuilder().apply {
                for ((i, parser) in list.withIndex()) {

                    val component = parser.getTabComponent(project, ref, this@ColorExpParserSequence, sequenceManager, writable).buildJPanel()
                    addComponent(
                        FormBuilder.createFormBuilder()
                            .addLabeledComponent(parser.tabTitle + ": ", JPanel()).panel
                    )
                    /* FormBuilder.createFormBuilder()
                         .addLabeledComponent(JBLabel(parser.tabTitle + ": "), component).panel.also { addComponent(it) }*/
                    addComponent(component.apply {
                        minimumSize = preferredSize
                    })
                    if (i < list.lastIndex) {
                        addSeparator()
                    }
                }
//                val size = Dimension(sliderSize.width, sliderSize.height / 2)
//                addComponent(ColorComponent(java.awt.Color(0, 0, 0, 0), size, size))
            }.buildJPanel()
        ).apply {
            maximumSize = maximumSizeForRootComponent()
            val toolbar = ActionUtils.createTabsToolbar(CustomEntryPointActionGroups.getColorExprTabsEntryPoint(), this)
            this@jpanelBuilder.addComponent(toolbar.component)
            sequenceManager.get().staticListeners.add {
                this.repaint()
            }
            this@jpanelBuilder.addComponent(this)
        }

        addColorPreview(sequenceManager)
    }


    private fun tabsBuilder(
        project: Project,
        ref: Ref<LightCalloutPopup>,
        writable: Boolean,
        sequenceManager: Ref<ColorExpSequenceManager> = Ref.create(ColorExpSequenceManager(ArcPluginSettingsState.getInstance().viewColorExprSeqAsList))
    ) = JPanelBuilder().apply {
        val colorExpressionParserTabs = ColorExpressionParserTabs(list[0].element.sourcePsi!!.project, {})
        val tabs = Array(list.size) { TabInfo(null).setObject(it) }
//        val sequenceManager = Ref.create(SequenceManager(false))
        for ((i, info) in tabs.withIndex()) {
            val parser = list[i]
            val component = parser
                .getTabComponent(project, ref, this@ColorExpParserSequence, sequenceManager, writable)
                .buildJPanel();
            info.apply {
                text = parser.tabTitle
                setComponent(component)
                this.preferredFocusableComponent = component
            }
        }
        tabs.forEach { colorExpressionParserTabs.addTab(it) }
        addComponent(colorExpressionParserTabs)
        colorExpressionParserTabs.addListener(object : TabsListener {
            override fun beforeSelectionChanged(oldSelection: TabInfo?, newSelection: TabInfo?) {
                if (newSelection == null) return
                val id = newSelection.`object` as Int
                val parser = list[id]

                val component = parser
                    .getTabComponent(project, ref, this@ColorExpParserSequence, sequenceManager, writable)
                    .buildJPanel();
                newSelection.apply {
                    setComponent(component)
                    this.preferredFocusableComponent = component
                }
            }

            override fun selectionChanged(oldSelection: TabInfo?, newSelection: TabInfo?) {
                super.selectionChanged(oldSelection, newSelection)
            }
        })
        colorExpressionParserTabs.maximumSize = maximumSizeForRootComponent()
        addColorPreview(sequenceManager)
    }

    private fun maximumSizeForRootComponent() = JBUI.size(PICKER_PREFERRED_WIDTH + 10, (PICKER_PREFERRED_WIDTH * 1.5f).toInt())

    private fun JPanelBuilder.addColorPreview(updateCallback: Ref<ColorExpSequenceManager>) {
        val size = Dimension(50, 50)
        FormBuilder.createFormBuilder()


            .addLabeledComponent(JBLabel("Color preview: ").apply {
                verticalAlignment = SwingConstants.TOP;
            }, ColorComponent(resultColor, preferredSize = size, minimumSize = size).also {
                updateCallback.get().staticListeners.add {
                    it.color = resultColor
                }
            }).panel.also { addComponent(it) }
    }

    override fun showPopup(project: Project, editor: Editor, writable: Boolean) {
        SimplePopup.lambdaPopup.show(project, editor) { getJPanelBuilder(project, it, writable) }
//        SimplePopup.showPopup(project, expressionParser.getJPanelBuilder(elt.isWritable()), editor)
    }

    override fun mergeIcons(list: Array<out ExpressionParserSequence<ColorExpSequenceManager>>): Icon {
        return JBUIScale.scaleIcon(ColorsIcon(12, *list.map { it.iconColor }.toTypedArray()))
    }
}

class ColorExpSequenceManager(val listView: Boolean) {
    private val updateListeners = HashMap<ColorExpressionParser, Runnable>()
    public val staticListeners = ArrayList<Runnable>()
    fun registerListener(parser: ColorExpressionParser, listener: Runnable) {
        updateListeners[parser] = listener
    }

    fun fireUpdate(parser: ColorExpressionParser) {
        for (pair in updateListeners) {
            if (pair.key == parser) continue
            pair.value.run()
        }
        staticListeners.forEach(Runnable::run)
    }
}
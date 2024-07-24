package lu.way.jadx.flow

import jadx.api.plugins.JadxPlugin
import jadx.api.plugins.JadxPluginContext
import jadx.api.plugins.JadxPluginInfo
import lu.way.jadx.flow.gui.FlowGUIDelegate
import lu.way.jadx.flow.gui.SimpleSourceSinkDisplay
import lu.way.jadx.flow.options.JadxFlowAllOptions
import lu.way.jadx.flow.taints.AnalysisSession
import lu.way.jadx.flow.taints.FlowdroidTaintLoader
import lu.way.jadx.flow.taints.ReferenceParser
import java.io.File
import java.nio.file.Path
import javax.swing.JFileChooser
import javax.swing.JOptionPane
import kotlin.io.path.name

class FlowInspectPlugin : JadxPlugin {

	private val scriptOptions = JadxFlowAllOptions()

	val loadedInfo = FlowAfterPass()

	val referenceParser = ReferenceParser(this)

	private val flowCodeInput = FlowCodeInput()

	private val treeViewDialog = SimpleSourceSinkDisplay(FlowGUIDelegate(this))

	private var loadOnce = false

	var currentSession: AnalysisSession? = null

	lateinit var pluginContext: JadxPluginContext

	override fun getPluginInfo() = JadxPluginInfo(PLUGIN_ID, "Jadx Flow Inspect", "Dataflow analysis output assistance")

	class MenuActionInspect : Runnable {
		override fun run() {
			TODO("Not yet implemented")
		}
	}

	override fun init(context: JadxPluginContext?) {
		if (loadOnce) {
			return
		}
		pluginContext = context!!
		loadOnce = true
		context.registerOptions(scriptOptions)
		context.addPass(loadedInfo)
		context.addCodeInput(flowCodeInput)
		context.guiContext?.let { guiContext ->
			guiContext.addMenuAction("Flow Inspect") {
				if (currentSession != null) {
					treeViewDialog.modelUpdate()
					treeViewDialog.isVisible = true
				} else {
					JOptionPane.showMessageDialog(
						null,
						"" + loadedInfo.decompiler.classesWithInners.size + ", " + loadedInfo.decompiler.classes.size,
					)
				}
			}
			guiContext.addMenuAction("Load Taint File") {
				var selectedFile: File? = null

				flowCodeInput.apkPath?.let { path ->
					val sniffXml = File(path.toFile().parent, path.name + ".xml")
					if (sniffXml.exists()) {
						if (JOptionPane.showConfirmDialog(
								null,
								"Found associated XML file in same folder, load it?",
								"Flow Inspect Plugin",
								JOptionPane.YES_NO_OPTION,
								JOptionPane.QUESTION_MESSAGE,
							) == JOptionPane.YES_OPTION
						) {
							selectedFile = sniffXml
						}
					}
				}
				if (selectedFile == null) {
					val fileChooser = flowCodeInput.apkPath?.let { JFileChooser(it.toFile()) } ?: JFileChooser()
					fileChooser.fileSelectionMode = JFileChooser.FILES_ONLY
					fileChooser.isMultiSelectionEnabled = false
					if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
						selectedFile = fileChooser.selectedFile
					}
				}
				if (selectedFile != null) {
					startSession(selectedFile!!, flowCodeInput.apkPath)
				}
			}
		}
	}

	private fun startSession(xmlFile: File, apkFile: Path?): Boolean {
		if (!xmlFile.exists()) return false
		val taintResult = FlowdroidTaintLoader().loadXmlFile(xmlFile)

		if (currentSession != null) {
			if (JOptionPane.showConfirmDialog(
					null,
					"An existing taint analysis session exists, overwrite it?",
					"Flow Inspect Plugin",
					JOptionPane.YES_NO_OPTION,
					JOptionPane.QUESTION_MESSAGE,
				) != JOptionPane.YES_OPTION
			) {
				return false
			}
		}

		currentSession = AnalysisSession(this, taintResult, apkFile, xmlFile)

		return true
	}

	companion object {
		const val PLUGIN_ID = "flow-inspect"
	}
}

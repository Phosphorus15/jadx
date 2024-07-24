package lu.way.jadx.flow.gui

import lu.way.jadx.flow.FlowInspectPlugin
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class FlowGUIDelegate(val plugin: FlowInspectPlugin) {

	fun startGraphView() {
	}

	fun updateSourceSinkTreeModel(tree: JTree) {
		tree.model = DefaultTreeModel(DefaultMutableTreeNode())
		plugin.currentSession?.let { session ->
			tree.model = TaintTreeModel(session.taintList)
		}
	}

	fun gotoMethodReference(method: String) {
		plugin.referenceParser.resolveClassByQualifiedMethodName(method)!!.let { clz ->
			plugin.referenceParser.findMethodByQualifiedName(method)!!.let {
				val meth = clz.searchMethodByShortId(it.methodInfo.shortId)
				plugin.pluginContext.guiContext?.open(meth!!.codeNodeRef)

				if (!meth!!.methodNode.isNoCode) {
					meth.methodNode.load()
				}
			}
		}
	}
}

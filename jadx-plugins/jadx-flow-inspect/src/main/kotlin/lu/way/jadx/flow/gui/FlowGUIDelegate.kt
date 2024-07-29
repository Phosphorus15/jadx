package lu.way.jadx.flow.gui

import lu.way.jadx.flow.FlowInspectPlugin
import lu.way.jadx.flow.taints.FlowdroidStatement
import lu.way.jadx.flow.taints.TaintPathSummarize
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel

class FlowGUIDelegate(val plugin: FlowInspectPlugin) {

	fun startGraphView() {
	}

	fun startPathView(statement: TrackedSource) {
		val taintSummary = TaintPathSummarize(statement.taintPath)
		taintSummary.prepareSummary()
		PathInfoVisualizeDialog(this, taintSummary).isVisible = true
	}

	fun updateSourceSinkTreeModel(tree: JTree) {
		tree.model = DefaultTreeModel(DefaultMutableTreeNode())
		plugin.currentSession?.let { session ->
			tree.model = TaintTreeModel(session.taintList)
		}
	}

	fun gotoFieldReference(field: String) {
		plugin.referenceParser.resolveClassByQualifiedMFieldName(field)!!.let { clz ->
			println(clz.javaClass.typeName)
			plugin.referenceParser.findFieldByQualifiedName(field)!!.let {
				val field = clz.fields.filter { cur-> cur.name == it.name && cur.declaringClass == it.parentClass.javaNode }
				if (field.size > 1)
					throw Exception("Found multiple fields of $field")
				else if (field.size == 1) {
					val fieldVal = field[0]
					plugin.pluginContext.guiContext?.open(fieldVal!!.codeNodeRef)
				}
			}
		}
	}

	fun gotoMethodReference(method: String) {
		plugin.referenceParser.resolveClassByQualifiedMethodName(method)!!.let { clz ->
			println(clz.javaClass.typeName)
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

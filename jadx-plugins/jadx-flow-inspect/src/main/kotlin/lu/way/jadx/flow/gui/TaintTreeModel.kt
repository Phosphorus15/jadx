package lu.way.jadx.flow.gui

import lu.way.jadx.flow.taints.FlowdroidStatement
import lu.way.jadx.flow.taints.TaintPath
import lu.way.jadx.flow.taints.TaintResult
import java.awt.Component
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.DefaultTreeModel

data class TrackedSource(val statement: FlowdroidStatement, val taintPath: TaintPath)

class TaintTreeModel(private val taintResult: TaintResult, preferredName: String = "Results") :
	DefaultTreeModel(DefaultMutableTreeNode(preferredName)) {

	val sinksSet: MutableMap<FlowdroidStatement, DefaultMutableTreeNode> = mutableMapOf()

	init {
		taintResult.forEach { pair ->
			val node = sinksSet.computeIfAbsent(pair.sink) {
				DefaultMutableTreeNode(pair.sink)
			}
			node.add(DefaultMutableTreeNode(TrackedSource(pair.source, pair)))
		}

		for (p in sinksSet.values) {
			(root as DefaultMutableTreeNode).add(p)
		}
		reload(root)
	}
}

class TaintTreeRenderer : DefaultTreeCellRenderer() {
	override fun getTreeCellRendererComponent(
		tree: JTree?,
		value: Any?,
		sel: Boolean,
		expanded: Boolean,
		leaf: Boolean,
		row: Int,
		hasFocus: Boolean,
	): Component {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus)

		if (value is TrackedSource) {
			text = value.statement.toString()
		} else if (value is FlowdroidStatement) {
			text = value.toString()
		}

		return this
	}
}

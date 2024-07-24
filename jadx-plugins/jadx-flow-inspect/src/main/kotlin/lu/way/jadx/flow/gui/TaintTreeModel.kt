package lu.way.jadx.flow.gui

import lu.way.jadx.flow.taints.FlowdroidStatement
import lu.way.jadx.flow.taints.TaintResult
import java.awt.Component
import javax.swing.JTree
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeCellRenderer
import javax.swing.tree.DefaultTreeModel

class TaintTreeModel(private val taintResult: TaintResult, preferredName: String = "Results") :
	DefaultTreeModel(DefaultMutableTreeNode(preferredName)) {

	val sinksSet: MutableMap<FlowdroidStatement, DefaultMutableTreeNode> = mutableMapOf()

	init {
		taintResult.forEach { pair ->
			val node = sinksSet.computeIfAbsent(pair.sink) {
				DefaultMutableTreeNode(pair.sink)
			}
			node.add(DefaultMutableTreeNode(pair.source))
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

		return this
	}
}

package lu.way.jadx.flow.gui

import lu.way.jadx.flow.taints.TaintMethodSegment
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JList

class MethodListRenderer : DefaultListCellRenderer() {
	override fun getListCellRendererComponent(
		list: JList<*>?,
		value: Any?,
		index: Int,
		isSelected: Boolean,
		cellHasFocus: Boolean
	): Component {
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
		val method = value as TaintMethodSegment
		text = "${method.id}:${method.method}"
		return this
	}
}

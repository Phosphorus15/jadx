package lu.way.jadx.flow.gui

import lu.way.jadx.flow.taints.PathSegmentType
import lu.way.jadx.flow.taints.TaintMethodSegment
import lu.way.jadx.flow.taints.TaintPathSegment
import java.awt.Color
import java.awt.Component
import javax.swing.DefaultListCellRenderer
import javax.swing.JList

class MethodListRenderer(var highlightMethod: String? = null) : DefaultListCellRenderer() {

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
		if (highlightMethod != null && !isSelected && method.method == highlightMethod) {
			foreground = Color.BLUE.darker()
		}
		return this
	}
}

class MethodStatementListRenderer : DefaultListCellRenderer() {
	override fun getListCellRendererComponent(
		list: JList<*>?,
		value: Any?,
		index: Int,
		isSelected: Boolean,
		cellHasFocus: Boolean
	): Component {
		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)

		val statement = value as TaintPathSegment
		text = statement.statement.statement

		if (statement.segmentType == PathSegmentType.SOURCE)
			foreground = Color.RED
		if (statement.segmentType == PathSegmentType.SINK)
			foreground = Color.BLUE

		return this
	}
}

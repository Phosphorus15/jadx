package lu.way.jadx.flow.taints

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import java.io.ByteArrayInputStream
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

data class FlowdroidStatement(val statement: String, val method: String)

data class FlowdroidField(val value: String, val type: String)

data class FlowdroidAccessPath(
	val value: String,
	val type: String,
	val taintSubfields: Boolean,
	val subfields: List<FlowdroidField>,
)

data class FlowdroidPathNode(val statement: FlowdroidStatement, val accessPath: FlowdroidAccessPath)

data class TaintPath(val source: FlowdroidStatement, val sink: FlowdroidStatement, val path: List<FlowdroidPathNode>)

fun TaintPath.hasPathInfo() = path.isNotEmpty()

/* Quick check if this taint definition contains path info */
fun MutableList<TaintPath>.hasPathInfo() = any { it.hasPathInfo() }

class FlowdroidTaintLoader {

	fun loadString(xml: String): MutableList<TaintPath> {
		val factory = DocumentBuilderFactory.newInstance()
		val builder = factory.newDocumentBuilder()
		val doc: Document = builder.parse(ByteArrayInputStream(xml.toByteArray()))
		return loadFlowdroidXml(doc)
	}

	fun loadXmlFile(xmlFile: File): MutableList<TaintPath> {
		val factory = DocumentBuilderFactory.newInstance()
		val builder = factory.newDocumentBuilder()
		val doc: Document = builder.parse(xmlFile)
		return loadFlowdroidXml(doc)
	}

	private fun loadFlowdroidXml(document: Document): MutableList<TaintPath> {
		document.documentElement.normalize()

		val data: MutableList<TaintPath> = ArrayList()

		val results = document.getElementsByTagName("Result")
		for (i in 0 until results.length) {
			val result = results.item(i) as Element
			val sink = result.getElementsByTagName("Sink").item(0) as Element
			val sinkStatement = sink.getAttribute("Statement")
			val sinkMethod = sink.getAttribute("Method")

			val sources = result.getElementsByTagName("Source")
			for (j in 0 until sources.length) {
				val source = sources.item(j) as Element
				val sourceStatement = source.getAttribute("Statement")
				val sourceMethod = source.getAttribute("Method")
				val taintPaths: List<FlowdroidPathNode> = extractPaths(source)
				data.add(
					TaintPath(
						FlowdroidStatement(sourceStatement, sourceMethod),
						FlowdroidStatement(sinkStatement, sinkMethod),
						taintPaths,
					),
				)
			}
		}
		return data
	}

	private fun extractPaths(sourceElement: Element): List<FlowdroidPathNode> {
		val paths: MutableList<FlowdroidPathNode> = mutableListOf()
		val pathElements: NodeList = sourceElement.getElementsByTagName("PathElement")
		for (i in 0 until pathElements.length) {
			val pathElement: Element = pathElements.item(i) as Element
			val statement: String = pathElement.getAttribute("Statement")
			val method: String = pathElement.getAttribute("Method")

			val accessPath = pathElement.getElementsByTagName("AccessPath")
			if (accessPath.length > 0) {
				val access = accessPath.item(0) as Element
				val accessValue = access.getAttribute("Value")
				val accessType = access.getAttribute("Type")
				val accessTaintSubfields = access.getAttribute("TaintSubFields").toBoolean()
				val fields = pathElement.getElementsByTagName("Field")
				val subfields = mutableListOf<FlowdroidField>()
				for (j in 0 until fields.length) {
					val field: Element = fields.item(j) as Element
					subfields.add(FlowdroidField(field.getAttribute("Value"), field.getAttribute("Type")))
				}
				paths.add(
					FlowdroidPathNode(
						FlowdroidStatement(statement, method),
						FlowdroidAccessPath(accessValue, accessType, accessTaintSubfields, subfields),
					),
				)
			} else {
				// TODO add warning for no access info found
			}
		}
		return paths
	}
}

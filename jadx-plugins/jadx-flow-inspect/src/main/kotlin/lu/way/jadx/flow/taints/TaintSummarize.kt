package lu.way.jadx.flow.taints

import org.graphstream.algorithm.Dijkstra
import org.graphstream.graph.Graph

enum class PathSegmentType {
	PATH,
	SOURCE,
	SINK
}

data class TaintPathSegment(
	val id: Int,
	val statement: FlowdroidStatement,
	val accessPath: FlowdroidAccessPath?,
	val segmentType: PathSegmentType = PathSegmentType.PATH,
	val segmentId: Int? = null,
)

data class TaintMethodSegment(val id: Int, val paths: List<TaintPathSegment>, val method: String)


class TaintPathSummarize(val taintPath: TaintPath) {

	lateinit var methodMaps: Map<String, List<TaintMethodSegment>>

	lateinit var segmentMaps: MutableMap<Int, TaintMethodSegment>

	init {

	}

	fun prepareSummary() {
		var currentMethod = taintPath.source.method
		var segmentIndex = 0
		val labeledPaths =
			mutableListOf(TaintPathSegment(0, taintPath.source, null, PathSegmentType.SOURCE, segmentId = 0))
		taintPath.path.forEachIndexed { index, node ->
			if (node.statement.method != currentMethod) {
				currentMethod = node.statement.method
				segmentIndex++
			}
			labeledPaths.add(
				TaintPathSegment(
					index + 1,
					node.statement,
					node.accessPath,
					PathSegmentType.PATH,
					segmentIndex
				)
			)
		}
		labeledPaths.add(
			TaintPathSegment(
				labeledPaths.size,
				taintPath.source,
				null,
				PathSegmentType.SINK,
				segmentId = if (taintPath.sink.method == currentMethod) segmentIndex else segmentIndex + 1
			)
		)
		segmentMaps = labeledPaths.groupBy { key -> key.segmentId }.mapKeys { key -> key.key!! }.mapValues { pathList ->
			// None of key or value should ever be empty
			TaintMethodSegment(
				pathList.key,
				pathList.value,
				pathList.value.firstOrNull()!!.statement.method
			)
		}.toSortedMap().toMutableMap()

		methodMaps = segmentMaps.asIterable().groupBy { it.value.method }.mapValues { entry ->
			entry.value.map { it.value }.sortedBy { it.id }
		}
	}

}

class GraphSummary(val taintSummarize: TaintPathSummarize) {

	fun updateSelectedMethod(graph: Graph, method: String) {
		graph.nodes().forEach { node ->
			if ((node.getAttribute("ui.class") ?: "") == "selected") {
				node.setAttribute("ui.class", "")
			}
		}
		graph.getNode(method)?.let { node ->
			if (node.getAttribute("ui.class") != "source" && node.getAttribute("ui.class") != "sink") {
				node.setAttribute("ui.class", "selected")
			}
		}
	}

	fun addMethodGraph(graph: Graph) {
		graph.setAutoCreate(true)

		val dijkstra = Dijkstra(null, null, null, null, null, null)
		dijkstra.init(graph)

		var lastKey = ""

		taintSummarize.methodMaps.keys.forEachIndexed { ind, key ->
			graph.addNode(key)
			when {
				ind == 0 -> {
					graph.getNode(key).setAttribute("ui.class", "source")
					dijkstra.setSource(key)
				}

				(key == taintSummarize.segmentMaps.values.last().method) -> {
					graph.getNode(key)
						.setAttribute("ui.class", "sink")
					lastKey = key
					dijkstra.setTarget(key)
				}

				else -> graph.getNode(key).setAttribute("ui.class", "flow")
			}
		}

		var lastMethod: String? = null
		var lastId = 0
		var sequenceCount = 0
		for (node in taintSummarize.segmentMaps.values) {
			if (lastMethod != null) {
				val id = node.id
				val edgeId = "${lastMethod}-${node.method}"

//				if (graph.getEdge("${node.method}-${lastMethod}") != null)
//					edgeId = "${node.method}-${lastMethod}"

				val edge = graph.getEdge(edgeId) ?: graph.addEdge(edgeId, lastMethod, node.method, true)

				if (edge.getAttribute("seq") == null) {
					edge.setAttribute("seq", "$sequenceCount")
				} else {
					edge.setAttribute("seq", "${edge.getAttribute("seq")}, $sequenceCount")
				}

			}
			lastMethod = node.method
			lastId = node.id
			sequenceCount += 1
		}

		dijkstra.compute()

		for (e in dijkstra.getPathEdges(graph.getNode(lastKey)))
			e.setAttribute("ui.style", "fill-color: red;")
	}
}

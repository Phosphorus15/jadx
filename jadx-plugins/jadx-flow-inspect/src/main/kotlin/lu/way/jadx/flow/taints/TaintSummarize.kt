package lu.way.jadx.flow.taints

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

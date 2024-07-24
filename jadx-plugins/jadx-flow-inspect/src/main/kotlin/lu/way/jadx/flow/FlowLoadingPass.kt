package lu.way.jadx.flow

import jadx.api.plugins.pass.impl.OrderedJadxPassInfo
import jadx.api.plugins.pass.types.JadxDecompilePass
import jadx.core.dex.nodes.ClassNode
import jadx.core.dex.nodes.MethodNode
import jadx.core.dex.nodes.RootNode

class FlowLoadingPass : JadxDecompilePass {
	override fun getInfo(): OrderedJadxPassInfo = OrderedJadxPassInfo(
		"JadxFlowPass",
		"Collects decompiled classes info",
	)
		.before("CodeRenameVisitor")

	private val classesMap = mutableMapOf<String, ClassNode>()

	override fun init(root: RootNode?) {
		root?.run {
			classesMap.clear()
		}
	}

	override fun visit(cls: ClassNode?): Boolean {
		cls?.run { classesMap.put(cls.name, cls) }
		return true
	}

	override fun visit(mth: MethodNode?) {
	}
}

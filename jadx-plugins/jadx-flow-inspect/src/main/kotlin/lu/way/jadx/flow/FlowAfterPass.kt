package lu.way.jadx.flow

import jadx.api.JadxDecompiler
import jadx.api.plugins.pass.impl.SimpleJadxPassInfo
import jadx.api.plugins.pass.types.JadxAfterLoadPass

class FlowAfterPass : JadxAfterLoadPass {
	override fun getInfo() = SimpleJadxPassInfo("JadxFlowAfterLoad", "Collects all classes after loading")

	lateinit var decompiler: JadxDecompiler

	override fun init(decompiler: JadxDecompiler?) {
		this.decompiler = decompiler!!
	}
}

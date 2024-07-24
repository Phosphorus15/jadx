package lu.way.jadx.flow

import jadx.api.plugins.input.ICodeLoader
import jadx.api.plugins.input.JadxCodeInput
import jadx.api.plugins.input.data.IClassData
import java.nio.file.Path
import java.util.function.Consumer
import kotlin.io.path.extension

class FlowCodeInput : JadxCodeInput, ICodeLoader {

	var apkPath: Path? = null

	override fun loadFiles(input: MutableList<Path>?): ICodeLoader {
		input!!.forEach {
			if (it.extension == "apk") {
				apkPath = it
			}
		}
		return this
	}

	override fun close() {
	}

	override fun visitClasses(consumer: Consumer<IClassData>?) {
	}

	override fun isEmpty(): Boolean = true
}

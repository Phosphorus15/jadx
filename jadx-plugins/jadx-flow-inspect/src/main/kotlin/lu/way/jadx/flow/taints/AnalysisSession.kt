package lu.way.jadx.flow.taints

import lu.way.jadx.flow.FlowInspectPlugin
import java.io.File
import java.nio.file.Path

typealias TaintResult = MutableList<TaintPath>

class AnalysisSession(val plugin: FlowInspectPlugin, val taintList: TaintResult, val apkPath: Path?, val taintFilePath: File)

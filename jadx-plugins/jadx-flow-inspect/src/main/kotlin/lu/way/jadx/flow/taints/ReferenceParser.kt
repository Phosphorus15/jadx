package lu.way.jadx.flow.taints

import jadx.api.JavaClass
import jadx.core.dex.info.MethodInfo
import jadx.core.dex.instructions.args.ArgType
import jadx.core.dex.nodes.ClassNode
import jadx.core.dex.nodes.MethodNode
import lu.way.jadx.flow.FlowInspectPlugin
import lu.way.jadx.flow.parser.MethodParser

class ReferenceParser(val plugin: FlowInspectPlugin) {

	private val methodParser = MethodParser()

	fun resolveClassByQualifiedMethodName(fullName: String): JavaClass? {
		val methodDef = MethodParser.parseMethodDefinition(fullName)
		return plugin.loadedInfo.decompiler.searchJavaClassByOrigFullName(methodDef.className)
	}

	fun findMethodByQualifiedName(fullName: String): MethodNode? {
		val methodDef = MethodParser.parseMethodDefinition(fullName)
		return findMethod(methodDef.methodName, methodDef.className, methodDef.returnType, methodDef.parameterTypes)
	}

	private fun convertToFlattenType(typeString: String): ArgType {
		when (typeString) {
			"boolean" -> return ArgType.BOOLEAN
			"byte" -> return ArgType.BYTE
			"short" -> return ArgType.SHORT
			"char" -> return ArgType.CHAR
			"int" -> return ArgType.INT
			"long" -> return ArgType.LONG
			"float" -> return ArgType.FLOAT
			"double" -> return ArgType.DOUBLE
			"void" -> return ArgType.VOID
			"java.lang.String" -> return ArgType.STRING
			"java.lang.Object" -> return ArgType.OBJECT
			"java.lang.Class" -> return ArgType.CLASS
			else -> return ArgType.`object`(typeString)
		}
	}

	fun convertToArgType(typeString: String): ArgType {
		return if (typeString.endsWith("]")) {
			ArgType.array(
				convertToFlattenType(typeString.replace("[", "").replace("]", "")),
				typeString.count { it == '[' },
			)
		} else {
			convertToFlattenType(typeString)
		}
	}

	fun findMethod(name: String, clazz: String, returnType: String, parameterTypes: MutableList<String>): MethodNode? {
		findClass(clazz)?.let { clazz ->
			MethodInfo.fromDetails(
				clazz.root(),
				clazz.classInfo,
				name,
				parameterTypes.map(this::convertToArgType),
				convertToArgType(returnType)
			).let {
				return clazz.searchMethod(it)
			}
		}
		return null
	}

	fun findClass(clazz: String): ClassNode? = plugin.loadedInfo.decompiler.searchClassNodeByOrigFullName(clazz)
}

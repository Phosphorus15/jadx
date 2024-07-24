package lu.way.jadx.flow.options

import jadx.api.plugins.options.JadxPluginOptions
import jadx.api.plugins.options.OptionDescription

class JadxFlowAllOptions : JadxPluginOptions {
	private lateinit var values: Map<String, String>
	override fun setOptions(options: MutableMap<String, String>?) {
		values = options ?: emptyMap()
	}

	override fun getOptionsDescriptions(): MutableList<OptionDescription> = mutableListOf()
}

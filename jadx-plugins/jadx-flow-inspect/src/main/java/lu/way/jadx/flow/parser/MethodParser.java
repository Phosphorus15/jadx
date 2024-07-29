package lu.way.jadx.flow.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MethodParser {

	public static class ParsedMethod {
		String className;
		String returnType;
		String methodName;
		List<String> parameterTypes;

		public String getClassName() {
			return className;
		}

		public void setClassName(String className) {
			this.className = className;
		}

		public String getReturnType() {
			return returnType;
		}

		public void setReturnType(String returnType) {
			this.returnType = returnType;
		}

		public String getMethodName() {
			return methodName;
		}

		public void setMethodName(String methodName) {
			this.methodName = methodName;
		}

		public List<String> getParameterTypes() {
			return parameterTypes;
		}

		public void setParameterTypes(List<String> parameterTypes) {
			this.parameterTypes = parameterTypes;
		}

		public ParsedMethod(String className, String returnType, String methodName, List<String> parameterTypes) {
			this.className = className;
			this.returnType = returnType;
			this.methodName = methodName;
			this.parameterTypes = parameterTypes;
		}

		@Override
		public String toString() {
			return "ParsedMethod{" +
					"className='" + className + '\'' +
					", returnType='" + returnType + '\'' +
					", methodName='" + methodName + '\'' +
					", parameterTypes=" + parameterTypes +
					'}';
		}
	}

	public static ParsedMethod parseMethodDefinition(String methodDefinition) {
		// Regular expression pattern to parse the method definition
		String regex = "<([^:]+):\\s+([^\\s]+)\\s+([^\\(]+)\\(([^\\)]*)\\)>";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(methodDefinition);

		if (matcher.matches()) {
			String className = matcher.group(1);
			String returnType = matcher.group(2);
			String methodName = matcher.group(3);
			String parameterString = matcher.group(4);

			List<String> parameterTypes = new ArrayList<>();
			if (!parameterString.isEmpty()) {
				String[] params = parameterString.split(",");
				for (String param : params) {
					parameterTypes.add(param.trim());
				}
			}

			return new ParsedMethod(className, returnType, methodName, parameterTypes);
		} else {
			throw new IllegalArgumentException("Invalid method definition format");
		}
	}

	/**
	 * Return a parsed method with only field definition info
	 *
	 * @param methodDefinition
	 * @return
	 */
	public static ParsedMethod parseField(String methodDefinition) {
		// Regular expression pattern to parse the method definition
		String regex = "<([^:]+):\\s+([^\\s]+)\\s+([^\\(]+)>";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(methodDefinition);

		if (matcher.matches()) {
			String className = matcher.group(1);
			String returnType = matcher.group(2);
			String methodName = matcher.group(3);

			return new ParsedMethod(className, returnType, methodName, null);
		} else {
			throw new IllegalArgumentException("Invalid method definition format");
		}
	}

	public static void main(String[] args) {
		String methodDefinition =
				"<com.startapp.android.publish.c.b: com.startapp.android.publish.c.b a(android.app.Activity,android.content.Intent,com.startapp.android.publish.model.AdPreferences$Placement)>";

		ParsedMethod parsedMethod = parseMethodDefinition(methodDefinition);
		System.out.println(parsedMethod);
	}
}

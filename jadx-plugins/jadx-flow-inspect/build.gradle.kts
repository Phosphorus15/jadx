plugins {
	id("jadx-library")
	id("jadx-kotlin")
}

dependencies {
	api(project(":jadx-core"))

	implementation("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.9.0")
	// https://mvnrepository.com/artifact/com.intellij/forms_rt
	implementation("com.intellij:forms_rt:7.0.3")
	implementation("org.graphstream:gs-core:2.0")
	implementation("org.graphstream:gs-algo:2.0")
	implementation("org.graphstream:gs-ui-swing:2.0")

	testImplementation(project(":jadx-core").dependencyProject.sourceSets.test.get().output)
	testImplementation("org.apache.commons:commons-lang3:3.14.0")

	testRuntimeOnly(project(":jadx-plugins:jadx-smali-input"))


}

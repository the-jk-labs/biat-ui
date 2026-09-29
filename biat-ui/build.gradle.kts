plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.biat.ui"
    compileSdk = 37

    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    // jvmTarget defaults to compileOptions.targetCompatibility (AGP built-in Kotlin).
    buildFeatures {
        compose = true
    }
}

// JVM-signature snapshot of the public API (javap over release classes).
// javap ships with the JDK, so this needs no Kotlin Gradle plugin (which
// AGP 9 forbids) and no runnable Metalava distribution. apiDump refreshes
// the golden file; apiCheck (wired into check) fails with a diff on change.
val apiGoldenFile = layout.projectDirectory.file("api/current.txt")

fun apiOutputDirs(): List<File> =
    listOf("compileReleaseKotlin", "compileReleaseJavaWithJavac")
        .mapNotNull { tasks.findByName(it) }
        .flatMap { it.outputs.files.files }
        .filter { it.isDirectory }

fun apiClassNames(): List<String> {
    val names = mutableSetOf<String>()
    apiOutputDirs().forEach { dir ->
        dir
            .walkTopDown()
            .filter { it.isFile && it.extension == "class" }
            .forEach { file ->
                val name =
                    file
                        .relativeTo(dir)
                        .path
                        .removeSuffix(".class")
                        .replace(File.separatorChar, '.')
                if (name == "BuildConfig" || name == "R" || name.startsWith("R$") || name.contains(".R$")) return@forEach
                names.add(name)
            }
    }
    return names.sorted()
}

fun apiClasspath(): String {
    val compileCp =
        configurations
            .named("releaseCompileClasspath")
            .get()
            .files
            .map { it.absolutePath }
    val outputs = apiOutputDirs().map { it.absolutePath }
    return (outputs + compileCp).joinToString(File.pathSeparator)
}

fun registerApiDumpTask(
    name: String,
    outFile: File,
): TaskProvider<Exec> =
    tasks.register<Exec>(name) {
        group = "verification"
        val compileTasks = listOf("compileReleaseKotlin", "compileReleaseJavaWithJavac").mapNotNull { tasks.findByName(it) }
        dependsOn(compileTasks)
        doFirst {
            outFile.parentFile.mkdirs()
            val names = apiClassNames()
            require(names.isNotEmpty()) { "No release classes found; compile tasks produced no output." }
            commandLine = listOf("javap", "-classpath", apiClasspath()) + names
            standardOutput = outFile.outputStream()
        }
    }

registerApiDumpTask("apiDump", apiGoldenFile.asFile).configure {
    description = "Regenerates biat-ui/api/current.txt from the release classes."
}

val apiDumpForCheck =
    registerApiDumpTask(
        "apiDumpForCheck",
        layout.buildDirectory
            .file("api/current.txt")
            .get()
            .asFile,
    )
apiDumpForCheck.configure {
    description = "Dumps the current API signature for apiCheck (do not call directly)."
}

tasks.register("apiCheck") {
    group = "verification"
    description = "Fails if the public API differs from biat-ui/api/current.txt."
    dependsOn(apiDumpForCheck)
    doLast {
        val golden = apiGoldenFile.asFile
        require(golden.isFile) { "Missing ${golden.path}; run :biat-ui:apiDump to create the baseline." }
        val current =
            layout.buildDirectory
                .file("api/current.txt")
                .get()
                .asFile
                .readLines()
        val expected = golden.readLines()
        if (current != expected) {
            val diff =
                expected
                    .zip(current)
                    .mapIndexedNotNull { index, (a, b) -> if (a == b) null else "line ${index + 1}:\n- $a\n+ $b" }
                    .take(20)
                    .joinToString("\n")
            val extra =
                if (current.size != expected.size) {
                    "\nline counts differ: golden=${expected.size}, current=${current.size}"
                } else {
                    ""
                }
            throw GradleException("Public API changed. Review the diff, then run :biat-ui:apiDump to accept.\n$diff$extra")
        }
    }
}

tasks.named("check") {
    dependsOn("apiCheck")
}

// Dokka HTML docs from KDoc, driven via dokka-cli (fat jar + JSON config).
// The Dokka Gradle plugin emits empty output here because it hooks the
// Kotlin Gradle plugin source sets, which AGP 9 forbids (built-in Kotlin).
val dokkaCli = configurations.create("dokkaCli")
val dokkaPlugins = configurations.create("dokkaPlugins")

fun jsonString(value: String): String = "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

tasks.register<JavaExec>("dokkaHtml") {
    group = "documentation"
    description = "Generates HTML API docs from KDoc into build/dokka/html."
    dependsOn("compileReleaseKotlin")
    classpath = dokkaCli
    mainClass.set("org.jetbrains.dokka.MainKt")
    standardOutput = System.out
    errorOutput = System.err
    doFirst {
        val outDir =
            layout.buildDirectory
                .dir("dokka/html")
                .get()
                .asFile
        val configFile =
            layout.buildDirectory
                .file("dokka/config.json")
                .get()
                .asFile
        outDir.mkdirs()
        configFile.parentFile.mkdirs()
        val classpathJson = apiClasspath().split(File.pathSeparator).joinToString(", ") { jsonString(it) }
        val pluginsJson = dokkaPlugins.files.joinToString(", ") { jsonString(it.absolutePath) }
        configFile.writeText(
            """
            {
              "moduleName": "biat-ui",
              "outputDir": ${jsonString(outDir.absolutePath)},
              "offlineMode": true,
              "reportUndocumented": true,
              "sourceSets": [
                {
                  "sourceSetID": { "scopeId": "biat-ui", "sourceSetName": "main" },
                  "displayName": "android",
                  "analysisPlatform": "jvm",
                  "sourceRoots": [${jsonString(
                layout.projectDirectory
                    .dir("src/main/kotlin")
                    .asFile.absolutePath,
            )}],
                  "classpath": [$classpathJson]
                }
              ],
              "pluginsClasspath": [$pluginsJson]
            }
            """.trimIndent(),
        )
        args = listOf(configFile.absolutePath)
    }
}

dependencies {
    dokkaCli(libs.dokka.cli)
    dokkaPlugins(libs.dokka.base)
    dokkaPlugins(libs.dokka.analysis)
    implementation(platform(libs.compose.bom))
    implementation(libs.compose.ui)
    implementation(libs.compose.ui.graphics)
    implementation(libs.compose.foundation)
    implementation(libs.compose.foundation.layout)
    implementation(libs.compose.runtime)
    implementation(libs.compose.runtime.saveable)
    implementation(libs.core.ktx)
    implementation(libs.coroutines.android)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)
}

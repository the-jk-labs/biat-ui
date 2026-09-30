plugins {
    base
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}

// ktlint runs as a CLI (com.pinterest.ktlint:ktlint-cli) because the
// Gradle lint plugins hook the Kotlin Gradle plugin source sets, and this
// repo uses AGP built-in Kotlin (no org.jetbrains.kotlin.android applied).
// Style rules come from .editorconfig (ktlint_official).
val ktlint = configurations.create("ktlint")
ktlint.attributes {
    attribute(
        Attribute.of("org.gradle.dependency.bundling", String::class.java),
        "shadowed",
    )
}

dependencies {
    ktlint(libs.ktlint.cli)
}

val ktlintPatterns =
    listOf(
        "biat-ui/src/**/*.kt",
        "sample/src/**/*.kt",
        "*.kts",
        "biat-ui/*.kts",
        "sample/*.kts",
    )

tasks.register<JavaExec>("ktlintCheck") {
    group = "verification"
    description = "Runs ktlint over library, sample, and build scripts."
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    args = listOf("check", "--reporter=plain") + ktlintPatterns
    standardOutput = System.out
    errorOutput = System.err
}

tasks.register<JavaExec>("ktlintFormat") {
    group = "formatting"
    description = "Auto-fixes ktlint violations in place."
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    args = listOf("--format") + ktlintPatterns
}

tasks.named("check") {
    dependsOn("ktlintCheck")
}

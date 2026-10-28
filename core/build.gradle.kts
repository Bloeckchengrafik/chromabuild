plugins { `java-library` }
java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }
tasks.test { useJUnitPlatform() }
dependencies {
    // Minecraft 1.21.1 strictly pins Gson 2.10.1. Share that version with NeoForge.
    api("com.google.code.gson:gson:2.10.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

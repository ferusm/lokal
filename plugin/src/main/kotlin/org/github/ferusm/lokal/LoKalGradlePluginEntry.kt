package org.github.ferusm.lokal


class LoKalGradlePluginEntry {
    lateinit var inputFilePath: String
    lateinit var outputPackage: String
    lateinit var outputDirPath: String

    fun validate(defaultOutputDir: String) {
        if (!this::inputFilePath.isInitialized) {
            throw IllegalStateException("${this::inputFilePath.name} property must be initialized")
        }
        if (!this::outputPackage.isInitialized) {
            throw IllegalStateException("${this::outputPackage.name} property must be initialized")
        }
        if (!this::outputDirPath.isInitialized) {
            outputDirPath = defaultOutputDir
        }
    }
}
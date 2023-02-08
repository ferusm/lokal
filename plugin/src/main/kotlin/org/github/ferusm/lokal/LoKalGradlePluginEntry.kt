package org.github.ferusm.lokal

import java.io.File


class LoKalGradlePluginEntry {
    lateinit var input: File
    lateinit var pack: String

    fun validate() {
        if (!this::input.isInitialized) {
            throw IllegalStateException("${this::input.name} property must be initialized")
        }
        if (!this::pack.isInitialized) {
            throw IllegalStateException("${this::pack.name} property must be initialized")
        }
    }
}
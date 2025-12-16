package io.github.ugaikit.gemini4kt

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.BINARY)
expect annotation class JsExport()

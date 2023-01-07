package com.github.fernthedev.lightchat.core.api

/**
 * Just to notify Intellij that usage will not be required at compile-time because it's an API method
 */
@Retention(AnnotationRetention.SOURCE)
@Target(
    AnnotationTarget.ANNOTATION_CLASS,
    AnnotationTarget.CLASS,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY_GETTER,
    AnnotationTarget.PROPERTY_SETTER,
    AnnotationTarget.FIELD,
    AnnotationTarget.VALUE_PARAMETER
)
@APIUsage
@MustBeDocumented
annotation class APIUsage 
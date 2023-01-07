package com.github.fernthedev.lightchat.core.api

/**
 * Used to show that the method is ASYNC
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@APIUsage
@MustBeDocumented
annotation class Async 
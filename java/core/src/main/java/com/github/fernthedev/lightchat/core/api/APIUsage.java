package com.github.fernthedev.lightchat.core.api;

import java.lang.annotation.*;

/**
 * Just to notify Intellij that usage will not be required at compile-time because it's an API method
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER})
@APIUsage
@Documented
public @interface APIUsage { }

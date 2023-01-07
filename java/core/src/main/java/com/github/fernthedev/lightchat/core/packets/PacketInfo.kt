package com.github.fernthedev.lightchat.core.packets

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class PacketInfo(val name: String)
package com.netease.cloudmusic.plugin.util

class Log {

    private static boolean sDebug = false

    def static setDebug(boolean debug) {
        sDebug = debug
    }

    def static isDebug() {
        return sDebug
    }

    def static d(Closure<String> msg) {
        if (sDebug) {
            println(msg.call())
        }
    }

    def static i(String msg) {
        println(msg)
    }
}
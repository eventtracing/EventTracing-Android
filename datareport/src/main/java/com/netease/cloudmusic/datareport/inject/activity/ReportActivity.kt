package com.netease.cloudmusic.datareport.inject.activity

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.view.LayoutInflaterCompat

open class ReportActivity: Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val layoutInflater = LayoutInflater.from(this)
        if (layoutInflater.factory == null) {
            LayoutInflaterCompat.setFactory(layoutInflater, ScrollFactory())
        }
        super.onCreate(savedInstanceState)
    }
}
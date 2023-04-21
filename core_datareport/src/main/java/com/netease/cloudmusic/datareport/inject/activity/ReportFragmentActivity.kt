package com.netease.cloudmusic.datareport.inject.activity

import android.os.Bundle
import android.view.LayoutInflater
import androidx.core.view.LayoutInflaterCompat
import androidx.fragment.app.FragmentActivity

open class ReportFragmentActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val layoutInflater = LayoutInflater.from(this)
        if (layoutInflater.factory == null) {
            LayoutInflaterCompat.setFactory(layoutInflater, ScrollFactory())
        }
        super.onCreate(savedInstanceState)
    }
}
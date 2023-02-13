package com.netease.cloudmusic.datareport.inject.activity

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.LayoutInflaterCompat
import androidx.core.view.LayoutInflaterFactory

open class ReportAppCompatActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val layoutInflater = LayoutInflater.from(this)
        if (layoutInflater.factory == null) {
            LayoutInflaterCompat.setFactory(layoutInflater, AppCompatFactory(delegate, ScrollFactory()))
        }
        super.onCreate(savedInstanceState)
    }

}

class AppCompatFactory(private val appCompatDelegate: AppCompatDelegate, private val scrollFactory: ScrollFactory): LayoutInflaterFactory{

    override fun onCreateView(
        parent: View?,
        name: String?,
        context: Context,
        attrs: AttributeSet
    ): View? {
        var view = scrollFactory.onCreateView(parent, name, context, attrs)
        if (view == null) {
            view = appCompatDelegate.createView(parent, name, context, attrs)
        }
        return view
    }

}
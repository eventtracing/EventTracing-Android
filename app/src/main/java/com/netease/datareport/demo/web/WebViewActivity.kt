package com.netease.datareport.demo.web

import android.os.Bundle
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import com.netease.cloudmusic.datareport.eventtracing.EventTracing
import com.netease.cloudmusic.datareport.eventtracing.NodeBuilder
import com.netease.cloudmusic.datareport.operator.DataReport
import com.netease.datareport.demo.R
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception

class WebViewActivity : AppCompatActivity() {
    //assets/html/test.js
    private val jsStr = "javascript:var bridge = {\n" +
            "  default: this,\n" +
            "  call: function (module, method, args, cb) {\n" +
            "    if (typeof args == \"function\") {\n" +
            "      cb = args;\n" +
            "      args = {};\n" +
            "    }\n" +
            "    args = args === undefined ? null : args;\n" +
            "    window.__et_message_call_seq++;\n" +
            "    if (typeof cb == \"function\") {\n" +
            "      var cb_name = \"__etcb_\" + window.__et_message_call_seq;\n" +
            "      window[cb_name] = cb;\n" +
            "    }\n" +
            "\n" +
            "    if(window.MonitorBridge && window.MonitorBridge.emit) {\n" +
            "      window.MonitorBridge.emit(window.__et_message_call_seq + \"\", module, method, JSON.stringify(args));\n" +
            "    }\n" +
            "  },\n" +
            "  registe: function (module, method, fun) {\n" +
            "    var q = window.__et_bridge_f;\n" +
            "    if (typeof fun != \"function\") {\n" +
            "      return;\n" +
            "    }\n" +
            "\n" +
            "    if (method === undefined || method == '' || method == null) {\n" +
            "      return;\n" +
            "    }\n" +
            "\n" +
            "    if (module === undefined || module == '' || module == null) {\n" +
            "      q[method] = fun;\n" +
            "      return;\n" +
            "    }\n" +
            "\n" +
            "    var methods = q[module];\n" +
            "    methods = methods==undefined ? {} : methods;\n" +
            "    methods[method] = fun;\n" +
            "    q[module] = methods;\n" +
            "  },\n" +
            "  isBridgeAvaiable: function (module, method, cb) {\n" +
            "    this.call('__et_jsb_internal_bridge', 'avaiable', {'module': module, 'method': method}, function(error, result, context){\n" +
            "      cb(result['avaiable'], {'module': module, 'method': method});\n" +
            "    });\n" +
            "  }\n" +
            "};\n"+
            "!(function () {\n" +
            "  if (window.__et_bridge_initialized) return;\n" +
            "\n" +
            "  var ob = {\n" +
            "    __et_bridge_initialized: true,\n" +
            "    __et_bridge_f: {},\n" +
            "    __et_message_call_seq: 0,\n" +
            "    __et_bridge: bridge,\n" +
            "    __et_call_cb_from_native: function (seq, error, result, context) {\n" +
            "      var cb_name = \"__etcb_\" + seq;\n" +
            "      var cb = window[cb_name];\n" +
            "      if (typeof cb != 'function') {\n" +
            "        return;\n" +
            "      }\n" +
            "\n" +
            "      setTimeout(() => {\n" +
            "        cb(error, result, context);\n" +
            "      }, 0);\n" +
            "    },\n" +
            "    __et_call_f_from_native: function (module, method, args) {\n" +
            "        var methods = window.__et_bridge_f[module];\n" +
            "        var f = methods[method];\n" +
            "        if (typeof f != 'function') {\n" +
            "          return;\n" +
            "        }\n" +
            "\n" +
            "        setTimeout(() => {\n" +
            "          f(args, {'module': module, 'method': method});\n" +
            "        }, 0);\n" +
            "    },\n" +
            "    __et_has_js_method: function (module, method) {\n" +
            "      var methods = window.__et_bridge_f[module];\n" +
            "      var f = methods[method];\n" +
            "      return f && (typeof f == 'function');\n" +
            "    }\n" +
            "  };\n" +
            "  for (var attr in ob) {\n" +
            "    window[attr] = ob[attr];\n" +
            "  }\n" +
            "})();"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)
        NodeBuilder.setPageId(this, "page_h5")

        findViewById<WebView>(R.id.webview)?.apply {
            initWebView(this)
            NodeBuilder.setElementId(this, "webview")
        }
    }

    private fun initWebView(webView: WebView) {
        val settings = webView.settings
        settings.javaScriptEnabled = true
        webView.addJavascriptInterface(DataReportJsBridge(webView), "MonitorBridge")

        webView.webChromeClient = object : WebChromeClient() {
            override fun onJsAlert(
                view: WebView?,
                url: String?,
                message: String?,
                result: JsResult?
            ): Boolean {
                return super.onJsAlert(view, url, message, result)
            }
            override fun onReceivedTitle(view: WebView?, title: String?) {
                super.onReceivedTitle(view, title)
                view?.loadUrl(jsStr)
            }
        }
        webView.loadUrl("http://10.221.74.28:8000/") //加载h5的地址
    }
}

/**
 * jsbridge 接受类
 */
class DataReportJsBridge (private val webView: WebView) {
    @JavascriptInterface
    fun emit(seq: String, module: String, method: String, args: String) {
        val message = DataReportMessage(seq, module, method, args, null, null)
        dealWithJsb(message)
    }

    fun dealWithJsb(message: DataReportMessage) {
        when (message.module) {
            "eventTracing" -> {
                when (message.method) {
                    "refers" -> { refers(message) }
                    "report" -> { report(message) }
                    "reportBatch" -> { reportBatch(message) }
                    "rebuildVTree" -> { rebuildVTree() }
                }
            }
            else -> {
            }
        }
    }

    private fun rebuildVTree() {
        EventTracing.reBuildVTree(webView)
    }

    private fun reportBatch(message: DataReportMessage) {
        try {
            val jsonArray = JSONObject(message.args).getJSONArray("logs")
            val size = jsonArray.length()
            if (size > 0) {
                for (index in 0 until size) {
                    reportItem(jsonArray.getJSONObject(index))
                }
            }
            message.result = JSONObject().apply { put("success", true) }.toString()
        } catch (e: JSONException) {
            message.error = e.message
        }
        callbackWithAction(message)
    }

    private fun report(message: DataReportMessage) {
        try {
            reportItem(JSONObject(message.args))
            message.result = JSONObject().apply { put("success", true) }.toString()
        } catch (e: JSONException) {
            message.error = e.message
        }
        callbackWithAction(message)
    }

    private fun reportItem(data: JSONObject) {
        val event = data.getString("event")
        val useForRefer = data.optBoolean("useForRefer", false)
        val params = data.optJSONObject("params")
        val pList = data.optJSONArray("_plist")
        val eList = data.optJSONArray("_elist")
        EventTracing.onWebReport(webView, event, useForRefer, pList, eList, params, "s_position")
    }

    private fun refers(message: DataReportMessage) {
        try {
            val params = JSONObject(message.args)
            val refers = JSONObject()
            val keys = params.optString("key", "")
            if (keys == "all") {
                listOf<String>("sessid", "sidrefer", "eventrefer", "multirefers", "hsrefer")
            } else {
                keys.split(",")
            }.forEach {
                when (it) {
                    "sessid" -> {
                        refers.put("sessid", DataReport.getInstance().sessionId)
                    }
                    "sidrefer" -> {
                        refers.put("sidrefer", DataReport.getInstance().sideRefer)
                    }
                    "eventrefer" -> {
                        refers.put("eventrefer", DataReport.getInstance().lastRefer)
                        DataReport.getInstance().lastUndefineRefer?.let { undefineRefer ->
                            refers.put("undefinedEventRefer", undefineRefer)
                        }
                    }
                    "multirefers" -> {
                        refers.put("multirefers", DataReport.getInstance().multiRefer)
                    }
                    "hsrefer" -> {
                        refers.put("hsrefer", DataReport.getInstance().hsRefer)
                    }
                    else -> {
                    }
                }
            }
            message.result = JSONObject().apply { put("refers", refers) }.toString()
            callbackWithAction(message)
        } catch (e: Exception) {
            message.error = e.message
        }
        callbackWithAction(message)
    }

    fun callbackWithAction(message: DataReportMessage) {
        webView.post{
            webView.loadUrl("javascript:window.__et_call_cb_from_native && window.__et_call_cb_from_native(\"${message.seq}\",${message.error},${message.result},{\"module\":\"${message.module}\",\"method\":\"${message.method}\"})")
        }
    }
}

data class DataReportMessage(val seq: String, val module: String, val method: String, val args: String, var result: String? = null, var error: String? = null)
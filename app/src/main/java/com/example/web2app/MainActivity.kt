package com.example.web2app

import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Network
import android.net.Uri
import android.net.http.SslError
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.Button
import android.widget.ProgressBar
import androidx.core.content.ContextCompat
import com.example.web2app.MainActivity.Companion.BASE_URL

class MainActivity : AppCompatActivity(), ConnectivityManager.OnNetworkActiveListener {

    companion object {
        var BASE_URL = "https://www.zkyte.com.ng/"
    }

    private lateinit var webView: WebView
    lateinit var progressBar: ProgressBar
    lateinit var error: View
    private lateinit var reloadBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportActionBar?.hide()

        progressBar = findViewById(R.id.progressBar)
        webView = findViewById(R.id.website_webview)
        error = findViewById(R.id.networkError)
        reloadBtn = findViewById(R.id.reloadBtn)

        webView.settings.domStorageEnabled = true
        webView.settings.javaScriptEnabled = true
        webView.settings.setAppCacheEnabled(true)
        webView.settings.databaseEnabled = true
        webView.settings.saveFormData = true

        webView.webViewClient = MyWebViewClient(this)
        webView.webChromeClient = MyChromeClient()

        webView.loadUrl(BASE_URL)

        reloadBtn.setOnClickListener {
            webView.reload()
        }

        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        connectivityManager.registerDefaultNetworkCallback(object :
            ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
//                Log.i(this.toString(), "The default network is now: " + network)
                runOnUiThread {
                    this@MainActivity.isConnected(true)
                }
            }

            override fun onLost(network: Network) {
//                Log.i(
//                    this.toString(),
//                    "The application no longer has a default network. The last default network was " + network
//                )
                runOnUiThread {
                    this@MainActivity.isConnected(false)
                }
            }
        })
    }

    private fun isConnected(connected: Boolean) {
        if (!connected) {
            progressBar.visibility = View.GONE
            error.visibility = View.VISIBLE
            webView.visibility = View.GONE
        } else {
//            webView.reload()
//            progressBar.visibility = View.VISIBLE
//            webView.loadUrl(BASE_URL)

            error.visibility = View.GONE
            webView.visibility = View.VISIBLE
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    override fun onNetworkActive() {}

}

class MyChromeClient : WebChromeClient()

class MyWebViewClient(private var activity: MainActivity) : WebViewClient() {


    override fun shouldOverrideUrlLoading(view: WebView?, url: String): Boolean {
//        Log.i("Loading", "--$url")

        if (url.indexOf(BASE_URL) > -1) {
            view!!.visibility = View.GONE
            activity.progressBar.visibility = View.VISIBLE
            view.loadUrl(url)
        } else {
            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data = Uri.parse(url)
            ContextCompat.startActivity(view!!.context, openURL, null)
        }

        return true
    }


    override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)

//        Log.i("Loading", "started loading: $url")
        view!!.visibility = View.GONE
        activity.progressBar.visibility = View.VISIBLE
        activity.error.visibility = View.GONE
    }

    override fun onPageFinished(view: WebView?, url: String?) {
        super.onPageFinished(view, url)

//        Log.i("Loading", "finished loading: $url")
        activity.progressBar.visibility = View.GONE
        view!!.visibility = View.VISIBLE
        activity.error.visibility = View.GONE
    }

    override fun onReceivedError(
        view: WebView?,
        request: WebResourceRequest?,
        error: WebResourceError?
    ) {
        super.onReceivedError(view, request, error)

//        handleError(error?.errorCode!!, view!!)
//        view!!.clearHistory()
        if (error!!.errorCode != -1) {
            view?.loadUrl("file:///android_asset/404.html")
        }
    }


    override fun onReceivedHttpError(
        view: WebView?,
        request: WebResourceRequest?,
        errorResponse: WebResourceResponse?
    ) {
        super.onReceivedHttpError(view, request, errorResponse)

        handleError(errorResponse!!.statusCode, view!!)
//        Log.i("Error", "HTTP Error: ${errorResponse.reasonPhrase}")
    }

    override fun onReceivedSslError(
        view: WebView?,
        handler: SslErrorHandler?,
        error: SslError?
    ) {
        super.onReceivedSslError(view, handler, error)

        handleError(error!!.primaryError, view!!)

//        Log.i("Error", "SSL Error: ${error.primaryError}")
    }

    private fun handleError(errorCode: Int, view: WebView) {
        val a: ArrayList<Int> = arrayListOf(
            ERROR_AUTHENTICATION,
            ERROR_TIMEOUT,
            ERROR_TOO_MANY_REQUESTS,
            ERROR_UNKNOWN,
            ERROR_BAD_URL,
            ERROR_CONNECT,
            ERROR_FAILED_SSL_HANDSHAKE,
            ERROR_HOST_LOOKUP,
            ERROR_PROXY_AUTHENTICATION,
            ERROR_REDIRECT_LOOP,
            ERROR_UNSUPPORTED_AUTH_SCHEME,
            ERROR_UNSUPPORTED_SCHEME,
            ERROR_FILE,
            ERROR_FILE_NOT_FOUND,
            ERROR_IO
        )

        if (errorCode in a) {
            Log.i("Error Code", "Error: $errorCode")
            activity.progressBar.visibility = View.GONE
            view.visibility = View.GONE
            activity.error.visibility = View.VISIBLE
        }
    }
}


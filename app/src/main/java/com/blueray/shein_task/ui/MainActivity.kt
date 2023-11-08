package com.blueray.shein_task.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.blueray.shein_task.databinding.ActivityMainBinding
import com.blueray.shein_task.viewModels.MainViewModel
import kotlinx.coroutines.launch
import org.apache.commons.lang3.StringEscapeUtils

class MainActivity : AppCompatActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var unescapedHtml : String

    private val viewModel : MainViewModel by viewModels()

    private val pattern = "p-\\d+-cat-"
    private var lastUrl = ""

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // set up webView
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.domStorageEnabled = true
        binding.webView.settings.allowFileAccess = true
        binding.webView.settings.allowContentAccess = true
        binding.webView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

        binding.goToCartBtn.setOnClickListener {
            binding.webView.loadUrl("https://m.shein.com/ar-en/cart")
        }


        // subscribe to stateflow
        buttonState()
        priceState()
        lastPriceState()


        binding.webView.webChromeClient = MyCustomChromeClient()

        val regex = Regex(pattern)

        // Create a WebViewClient to override the onLoadResource method
        binding.webView.webViewClient = object : WebViewClient() {

            override fun onLoadResource(view: WebView?, url: String?) {
                if(binding.webView.url?.contains("/cart") == true){
                    viewModel.buttonState.value = "Checkout"
                }else{
                    viewModel.buttonState.value = "Show Cart"
                }
                if(regex.containsMatchIn(url!!)){
                    if(viewModel.priceState.value == "" || lastUrl != binding.webView.url)
                        binding.webView.evaluateJavascript("document.documentElement.outerHTML") { html ->
                        unescapedHtml = StringEscapeUtils.unescapeJava(html)
                            viewModel.extractPriceFromHtml(unescapedHtml)
                            lastUrl = binding.webView.url!!
                    }
                }
            }
        }

    }

    // custom chrome client
    private inner class MyCustomChromeClient : WebChromeClient()

    private fun buttonState(){
        lifecycleScope.launch {
            viewModel.buttonState.collect{
                binding.goToCartBtn.text = it
            }

        }
    }
    private fun priceState(){
        lifecycleScope.launch {
            viewModel.priceState.collect{
                if(it == ""){
                    binding.priceView.text = it
                    binding.pricePH.visibility = View.INVISIBLE
                }else{
                    binding.priceView.text = it
                    binding.pricePH.visibility = View.VISIBLE
                }
            }

        }
    }
    private fun lastPriceState(){
        lifecycleScope.launch {
            viewModel.lastUrl.collect{
                binding.webView.loadUrl(it)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.lastUrl.value = binding.webView.url!!
    }


}
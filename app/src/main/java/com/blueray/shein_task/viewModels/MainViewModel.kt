package com.blueray.shein_task.viewModels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow

class MainViewModel(

) : ViewModel() {

    val buttonState = MutableStateFlow("show cart")

    val priceState = MutableStateFlow("")

    val lastUrl = MutableStateFlow("https://m.shein.com/")

    fun extractPriceFromHtml(htmlContent: String){
        priceState.value = extractPrice(htmlContent) ?: ""
    }

    private fun extractPrice(htmlContent: String): String? {
        val regex = Regex("""aria-label="([A-Z]+[\d.]+)"""")
        val match = regex.find(htmlContent)

        val priceValue = match?.groupValues?.getOrNull(1)

        return priceValue
    }





}
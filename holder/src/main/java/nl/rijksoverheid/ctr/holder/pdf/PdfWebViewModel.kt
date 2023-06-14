package nl.rijksoverheid.ctr.holder.pdf

import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

abstract class PdfWebViewModel: ViewModel() {
    abstract fun storePdf(fileOutputStream: FileOutputStream, contents: String)
}

class PdfWebViewModelImpl: PdfWebViewModel() {
    override fun storePdf(fileOutputStream: FileOutputStream, contents: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val base64Content = contents.replace("data:application/pdf;base64,", "")
            val base64DecodedContent = Base64.decode(base64Content, Base64.DEFAULT)
            fileOutputStream.use {
                it.write(base64DecodedContent)
                it.flush()
            }
        }
    }
}

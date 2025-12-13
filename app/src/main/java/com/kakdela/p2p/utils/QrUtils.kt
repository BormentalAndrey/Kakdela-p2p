package com.kakdela.p2p.utils

import android.graphics.Bitmap
import net.glxn.qrgen.android.QRCode

object QrUtils {
    fun generateQrCode(data: String): Bitmap {
        return QRCode.from(data).bitmap()
    }
}

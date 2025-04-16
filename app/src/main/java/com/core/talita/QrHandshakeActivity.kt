package com.core.talita

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.common.BitMatrix
import com.core.talita.databinding.ActivityQrHandshakeBinding

class QrHandshakeActivity : AppCompatActivity() {

    companion object {
        private const val QR_SCAN_REQUEST = 1001
        private const val TAG = "QrHandshakeActivity"
    }

    private lateinit var binding: ActivityQrHandshakeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrHandshakeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnGenerateQr.setOnClickListener {
            try {
                generateQrCode()
            } catch (e: WriterException) {
                e.printStackTrace()
            }
        }

        binding.btnScanQr.setOnClickListener {
            launchQrScanner()
        }
    }

    @Throws(WriterException::class)
    private fun generateQrCode() {
        // Generate cryptographic keys (simplified example)
        val publicKey = "SAMPLE_PUBLIC_KEY_123".toByteArray()

        // QR Generation
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(
            Base64.encodeToString(publicKey, Base64.DEFAULT),
            BarcodeFormat.QR_CODE,
            400,
            400
        )

        // Convert to Bitmap
        val bitmap = Bitmap.createBitmap(400, 400, Bitmap.Config.RGB_565).apply {
            for (x in 0 until 400) {
                for (y in 0 until 400) {
                    setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }

        binding.ivQrCode.setImageBitmap(bitmap)
    }

    private fun launchQrScanner() {
        try {
            val intent = Intent("com.google.zxing.client.android.SCAN").apply {
                putExtra("SCAN_MODE", "QR_CODE_MODE")
            }
            startActivityForResult(intent, QR_SCAN_REQUEST)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "Please install a QR scanner app", Toast.LENGTH_LONG).show()
        }
    }

    private fun startActivityForResult(intent: Intent, qrScanRequest: Int) {
        TODO("Not yet implemented")
    }

    private fun putExtra(s: String, s1: String) {
        TODO("Not yet implemented")
    }

    class Intent(s: String) {

    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: android.content.Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == QR_SCAN_REQUEST && resultCode == RESULT_OK) {
            val scannedContent = data?.getStringExtra("SCAN_RESULT")
            scannedContent?.let { processScannedQr(it) }
        }
    }

    private fun processScannedQr(content: String) {
        try {
            val decodedData = Base64.decode(content, Base64.DEFAULT)
            Log.d(TAG, "Received contact: ${decodedData.toString(Charsets.UTF_8)}")
            Toast.makeText(this, "Contact added!", Toast.LENGTH_SHORT).show()
            // TODO: Store contact securely
        } catch (e: Exception) {
            Log.e(TAG, "QR processing failed", e)
            Toast.makeText(this, "Invalid QR code", Toast.LENGTH_SHORT).show()
        }
    }
}

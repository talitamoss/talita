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
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.KeyFactory
import java.security.spec.X509EncodedKeySpec
import java.security.MessageDigest

class QrHandshakeActivity : AppCompatActivity() {

    companion object {
        private const val QR_SCAN_REQUEST = 1001
        private const val TAG = "QrHandshakeActivity"
    }

    private lateinit var binding: ActivityQrHandshakeBinding
    private lateinit var keyPair: KeyPair

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
        if (!::keyPair.isInitialized) {
            generateKeyPair()
        }

        val publicKeyBytes = keyPair.public.encoded
        val publicKeyBase64 = Base64.encodeToString(publicKeyBytes, Base64.DEFAULT)

        // QR Generation
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(
            publicKeyBase64,
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

    private fun generateKeyPair() {
        val keyGen = KeyPairGenerator.getInstance("RSA")
        keyGen.initialize(2048)
        keyPair = keyGen.generateKeyPair()
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
            val decodedBytes = Base64.decode(content, Base64.DEFAULT)
            val keySpec = X509EncodedKeySpec(decodedBytes)
            val keyFactory = KeyFactory.getInstance("RSA")
            val publicKey = keyFactory.generatePublic(keySpec)

            // Optional: generate a fingerprint to identify the contact
            val fingerprint = MessageDigest.getInstance("SHA-256")
                .digest(decodedBytes)
                .joinToString("") { "%02x".format(it) }

            Log.d(TAG, "Trusted key added. Fingerprint: $fingerprint")
            Toast.makeText(this, "Contact added!", Toast.LENGTH_SHORT).show()

            // TODO: Store publicKey somewhere secure (like EncryptedSharedPreferences)

        } catch (e: Exception) {
            Log.e(TAG, "QR processing failed", e)
            Toast.makeText(this, "Invalid QR code", Toast.LENGTH_SHORT).show()
        }
    }
}

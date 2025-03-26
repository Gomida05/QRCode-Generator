package com.das.myqrcode.ui.home

import android.graphics.Bitmap
import android.media.MediaScannerConnection
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.das.myqrcode.databinding.FragmentHomeBinding
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import java.io.File
import java.io.FileOutputStream
import kotlin.Any


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private var bitmap: Bitmap? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        return binding.root
    }



    override fun onStart() {
        super.onStart()


        binding.clearText.setOnClickListener{
            binding.inputText.text.clear()
        }

        binding.generateQrCode.setOnClickListener{
            val pretexting= binding.inputText
            if (pretexting.text.toString().isNotBlank()) {
                val qrCodeBitmap = generateQRCode(pretexting.text.toString())
                binding.imageView2.setImageBitmap(qrCodeBitmap)
                bitmap = qrCodeBitmap
                hideOrShowButtons(View.VISIBLE)
            }else{
                Toast.makeText(requireContext(), "Please enter something", Toast.LENGTH_SHORT).show()
                hideOrShowButtons(View.GONE)
            }
        }

        binding.inputText.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                if (binding.inputText.text.toString().isNotBlank()) {
                    val qrCodeBitmap= generateQRCode(binding.inputText.text.toString())
                    binding.imageView2.setImageBitmap(qrCodeBitmap)
                    bitmap = qrCodeBitmap
                    hideOrShowButtons(View.VISIBLE)
                    true
                }else{
                    Toast.makeText(requireContext(), "Please enter something", Toast.LENGTH_SHORT).show()
                    hideOrShowButtons(View.GONE)
                    false
                }
            }else{
                false
            }
        }
        binding.deleteBtn.setOnClickListener{
            binding.imageView2.setImageBitmap(null)
            bitmap= null
            hideOrShowButtons(View.GONE)
        }
        binding.saveBtn.setOnClickListener{
            saveQRCodeToExternalStorage("QRCode_${System.currentTimeMillis()}")
            binding.imageView2.setImageBitmap(null)
        }

    }



    private fun generateQRCode(content: String): Bitmap {
        val writer = MultiFormatWriter()

        val hints = hashMapOf<EncodeHintType, Any>(
            EncodeHintType.MARGIN to 1,
            EncodeHintType.ERROR_CORRECTION to com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.L,
        )


        val bitMatrix: BitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512, hints)


        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(
                    x,
                    y,
                    if (bitMatrix.get(
                            x,
                            y
                        )
                    ) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                )
            }
        }
        return bitmap
    }



    private fun saveQRCodeToExternalStorage(fileName: String): Boolean {
        return try {
            val directory = File("/storage/emulated/0/DCIM/QRCodeImages")
            if (!directory.exists()) {
                directory.mkdirs()
            }
            val file = File(directory, "$fileName.png")
            FileOutputStream(file).use { outputStream ->
                bitmap?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            MediaScannerConnection.scanFile(
                requireContext(),
                arrayOf("${directory}/${fileName}.png"),
                null,
                null
            )
            Toast.makeText(requireContext(), "Saved to Gallery!", Toast.LENGTH_SHORT).show()
            hideOrShowButtons(View.GONE)
            binding.inputText.text.clear()

            true
        } catch (e: Exception) {
            Log.e("QRCode", "Failed to save QR code: ${e.message}")
            false
        }
    }


    private fun hideOrShowButtons(visibility: Int){
        binding.deleteBtn.visibility= visibility
        binding.saveBtn.visibility= visibility
    }







    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
package com.ichiisoft.android_ftp

import android.Manifest.permission
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.storage.StorageManager
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import java.io.BufferedOutputStream
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.nio.file.Files

// free test ftp server
// https://sftpcloud.io/tools/free-ftp-server

class MainActivity : AppCompatActivity() {
    private lateinit var tvLog: TextView

    private val hostname = "eu-central-1.sftpcloud.io"
    private val user = "9bf38345c8cd4891873fddced0af094a"
    private val password = "FmTAZ8anQJJFFvdydewXMSTnc2qWBbj5"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(
            this, arrayOf(permission.READ_MEDIA_IMAGES),
            PackageManager.PERMISSION_GRANTED
        )

        tvLog = findViewById(R.id.tvLog)

        findViewById<Button>(R.id.btnUpload).setOnClickListener {
            val runable = Runnable {
                uploadFile()
            }
            val thread = Thread(runable)
            thread.start()
        }

        findViewById<Button>(R.id.btnGet).setOnClickListener {
            val runable = Runnable {
                getFile()
            }
            val thread = Thread(runable)
            thread.start()
        }
    }

    private fun uploadFile() {
        val storageManager = getSystemService(STORAGE_SERVICE) as StorageManager
        val storageVolume = storageManager.storageVolumes[0] // 0 for internal Storage
        val fileImage = File(storageVolume.directory!!.path + "/Download/download.jpeg")
        val ftpClient = FTPClient()
        try {
            val inputStream = Files.newInputStream(fileImage.toPath())
            ftpClient.connect(hostname)
            ftpClient.login(user, password)
            ftpClient.changeWorkingDirectory("usb1_1/Uploads/")
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE)
            ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE)
            ftpClient.enterLocalPassiveMode()
            ftpClient.sendCommand("OPTS UTF8 ON")
            val booleanStatus = ftpClient.storeFile("image_remoteFile.jpeg", inputStream)
            runOnUiThread {
                tvLog.text = if (booleanStatus) { "Upload file success!" } else { "Upload file faild!" }
            }
            inputStream.close()
            ftpClient.logout()
            ftpClient.disconnect()
        } catch (e: IOException) {
            throw java.lang.RuntimeException(e)
        }
    }

    private fun getFile() {
        try {
            val storageManager = getSystemService(STORAGE_SERVICE) as StorageManager
            val storageVolume = storageManager.storageVolumes[0]
            val fileImage = File(storageVolume.directory?.path + "/Download/${System.currentTimeMillis()}.jpeg")
            downloadAndSaveFile("image_remoteFile.jpeg", fileImage)
        } catch (e: IOException) {
            throw java.lang.RuntimeException(e)
        }
    }

    @Throws(IOException::class)
    private fun downloadAndSaveFile(filename: String?, localFile: File) {
        val LOG_TAG = "FTPconnection_download"
        val ftpClient = FTPClient()
        try {
            val outputStream: OutputStream =
                BufferedOutputStream(Files.newOutputStream(localFile.toPath()))
            ftpClient.connect(hostname)
            ftpClient.login(user, password)
            ftpClient.changeWorkingDirectory("usb1_1/Uploads/")
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE)
            ftpClient.setFileTransferMode(FTP.BINARY_FILE_TYPE)
            ftpClient.enterLocalPassiveMode()
            ftpClient.sendCommand("OPTS UTF8 ON")
            Log.d(LOG_TAG, "Downloading")
            val booleanStatus = ftpClient.retrieveFile(filename, outputStream)
            runOnUiThread {
                tvLog.text = if (booleanStatus) { "Get file success!" } else { "Get file faild!" }
            }
            outputStream.close()
            ftpClient.logout()
            ftpClient.disconnect()
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }
}
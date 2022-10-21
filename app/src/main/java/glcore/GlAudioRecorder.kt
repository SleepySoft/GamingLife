package glcore

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.Environment
import android.support.annotation.RequiresApi
import android.support.v4.app.ActivityCompat
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream


// https://blog.csdn.net/wozuihaole/article/details/104063277

object GlAudioRecorder {
    // 1.设置录音相关参数,音频采集源、采样率、声道、数据格式
    // 2.计算最小录音缓存区大小
    // 3.创建audioRecord对象
    // 4.开始录音
    // 5.创建文件用于保存PCM文件
    // 6.录音完毕，关闭录音及释放相关资源
    // 7.将pcm文件转换为WAV文件

    private const val AudioSource = MediaRecorder.AudioSource.MIC
    private const val SampleRate = 16000
    private const val Channel = AudioFormat.CHANNEL_IN_MONO
    private const val EncodingType = AudioFormat.ENCODING_PCM_16BIT

    @RequiresApi(Build.VERSION_CODES.R)
    private val PCMPath: String = "${Environment.getStorageDirectory().path}/temp/RawAudio.pcm"

    @RequiresApi(Build.VERSION_CODES.R)
    private val WAVPath: String = "${Environment.getStorageDirectory().path}/temp/WavAudio.wav"

    private var bufferSizeInByte: Int = 0
    private var audioRecorder: AudioRecord? = null
    private var isRecording = false

    private fun init() {
        bufferSizeInByte = AudioRecord.getMinBufferSize(SampleRate, Channel, EncodingType)
        createRecorder()
    }

    private fun createRecorder() : Boolean {
        return if (ActivityCompat.checkSelfPermission(
                GlApplication.applicationContext(),
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            false
        }
        else {
            audioRecorder = AudioRecord(
                AudioSource, SampleRate, Channel,
                EncodingType, bufferSizeInByte
            )
            true
        }
    }

    fun startRecord(): Boolean {
        return if (isRecording) {
            false
        } else {
            audioRecorder ?: createRecorder()
            audioRecorder?.startRecording()

            isRecording = true
            AudioRecordToFile().start()
            true
        }
    }

    fun stopRecord() {
        audioRecorder?.stop()
        audioRecorder?.release()
        isRecording = false
        audioRecorder = null
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun writeDateTOFile() {

        val audioData = ByteArray(bufferSizeInByte)
        val file = File(PCMPath)
        if (file.parentFile?.exists() != true) {
            file.parentFile?.mkdirs()
        }
        if (file.exists()) {
            file.delete()
        }
        file.createNewFile()
        val out = BufferedOutputStream(FileOutputStream(file))
        var length = 0
        while (isRecording && audioRecorder != null) {
            length = audioRecorder!!.read(audioData, 0, bufferSizeInByte)
            if (AudioRecord.ERROR_INVALID_OPERATION != length) {
                out.write(audioData, 0, length)
                out.flush()
            }
        }
        out.close()
    }

    //将pcm格式的文件转换为WAV格式的
    private fun copyWaveFile() {

        val fileIn = FileInputStream(PCMPath)
        val fileOut = FileOutputStream(WAVPath)
        val data = ByteArray(bufferSizeInByte)
        val totalAudioLen = fileIn.channel.size()
        val totalDataLen = totalAudioLen + 36
        writeWaveFileHeader(fileOut, totalAudioLen, totalDataLen)
        var count = fileIn.read(data, 0, bufferSizeInByte)
        while (count != -1) {
            fileOut.write(data, 0, count)
            fileOut.flush()
            count = fileIn.read(data, 0, bufferSizeInByte)
        }
        fileIn.close()
        fileOut.close()
    }

    //添加WAV格式的文件头
    private fun writeWaveFileHeader(
        out: FileOutputStream, totalAudioLen: Long,
        totalDataLen: Long) {

        val channels = 1
        val byteRate = 16 * SampleRate * channels / 8
        val header = ByteArray(44)
        header[0] = 'R'.code.toByte()
        header[1] = 'I'.code.toByte()
        header[2] = 'F'.code.toByte()
        header[3] = 'F'.code.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte()
        header[7] = (totalDataLen shr 24 and 0xff).toByte()
        header[8] = 'W'.code.toByte()
        header[9] = 'A'.code.toByte()
        header[10] = 'V'.code.toByte()
        header[11] = 'E'.code.toByte()
        header[12] = 'f'.code.toByte() // 'fmt ' chunk
        header[13] = 'm'.code.toByte()
        header[14] = 't'.code.toByte()
        header[15] = ' '.code.toByte()
        header[16] = 16 // 4 bytes: size of 'fmt ' chunk
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // format = 1
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (SampleRate and 0xff).toByte()
        header[25] = (SampleRate shr 8 and 0xff).toByte()
        header[26] = (SampleRate shr 16 and 0xff).toByte()
        header[27] = (SampleRate shr 24 and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        header[32] = (2 * 16 / 8).toByte() // block align
        header[33] = 0
        header[34] = 16 // bits per sample
        header[35] = 0
        header[36] = 'd'.code.toByte()
        header[37] = 'a'.code.toByte()
        header[38] = 't'.code.toByte()
        header[39] = 'a'.code.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = (totalAudioLen shr 8 and 0xff).toByte()
        header[42] = (totalAudioLen shr 16 and 0xff).toByte()
        header[43] = (totalAudioLen shr 24 and 0xff).toByte()
        out.write(header, 0, 44)
    }

    private class AudioRecordToFile : Thread() {

        @RequiresApi(Build.VERSION_CODES.R)
        override fun run() {
            super.run()
            writeDateTOFile()
            copyWaveFile()
        }
    }
}
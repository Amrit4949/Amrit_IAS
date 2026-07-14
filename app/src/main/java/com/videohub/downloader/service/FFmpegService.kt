package com.videohub.downloader.service

import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import java.io.File

object FFmpegService {

    /**
     * Run manual conversion (e.g. video to audio extraction on Android)
     */
    fun convertToMp3(inputFile: File, outputFile: File): Boolean {
        val cmd = "-y -i \"${inputFile.absolutePath}\" -vn -acodec libmp3lame -ab 192k \"${outputFile.absolutePath}\""
        val session = FFmpegKit.execute(cmd)
        return ReturnCode.isSuccess(session.returnCode)
    }

    /**
     * Verify FFmpeg library version
     */
    fun getVersion(): String {
        val session = FFmpegKit.execute("-version")
        return if (ReturnCode.isSuccess(session.returnCode)) {
            session.output.split("\n").firstOrNull() ?: "ffmpeg-kit"
        } else {
            "unknown"
        }
    }
}

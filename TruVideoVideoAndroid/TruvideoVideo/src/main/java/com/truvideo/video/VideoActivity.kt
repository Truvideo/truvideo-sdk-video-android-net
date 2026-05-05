package com.truvideo.video


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.lifecycleScope
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile
import com.truvideo.sdk.video.model.TruvideoSdkVideoFileDescriptor
import com.truvideo.sdk.video.ui.activities.edit.TruvideoSdkVideoEditContract
import com.truvideo.sdk.video.ui.activities.edit.TruvideoSdkVideoEditParams
import kotlinx.coroutines.launch

class VideoActivity : ComponentActivity() {
    private lateinit var editVideoLauncher: ActivityResultLauncher<TruvideoSdkVideoEditParams>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video)
        val inputPath = intent.getStringExtra("inputPath")
        val outputPath = intent.getStringExtra("outputPath")
        val input_Path = videoFile(inputPath!!);
        val output_Path = videoFileDescriptor(outputPath!!);

        lifecycleScope.launch {
            try {
                editVideoLauncher =
                    registerForActivityResult(TruvideoSdkVideoEditContract(), { resultPath ->
                        // edited video its on 'resultPath'
                       // DotnetTruvideoVideo.mainCallback?.onSuccess(resultPath)

                        if (!resultPath.isNullOrEmpty()) {
                            // ✅ Success
                            DotnetTruvideoVideo.mainCallback?.onSuccess(resultPath)
                        } else {
                            // ❌ Failure case
                            DotnetTruvideoVideo.mainCallback?.onSuccess("Video edit failed")
                        }
                        finish()


                    })
                editVideo(input_Path, output_Path)
            } catch (e: Exception) {
                DotnetTruvideoVideo.mainCallback?.onSuccess(e.message ?: "Unknown error")
            }
        }
    }

    fun editVideo(input: TruvideoSdkVideoFile, output: TruvideoSdkVideoFileDescriptor) {
        try {
            editVideoLauncher.launch(
                TruvideoSdkVideoEditParams(
                    input = input,
                    output = output,
                    useMedia3 = true
                )
            )
        }catch (e: Exception) {
            DotnetTruvideoVideo.mainCallback?.onSuccess(e.message ?: "Unknown error")
        }
    }



    fun videoFile(inputPath: String): TruvideoSdkVideoFile {
        return TruvideoSdkVideoFile.custom(inputPath)
    }

    fun videoFileDescriptor(outputPath: String): TruvideoSdkVideoFileDescriptor {
        return TruvideoSdkVideoFileDescriptor.custom(outputPath)
    }

    fun listVideoFile(list: List<String>): List<TruvideoSdkVideoFile> {
        val listVideo = ArrayList<TruvideoSdkVideoFile>()
        list.forEach {
            listVideo.add(videoFile(it))
        }
        return listVideo
    }

}

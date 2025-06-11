package com.truvideo.video

import android.content.Context
import android.content.Intent
import androidx.startup.AppInitializer
import com.google.gson.Gson
import com.truvideo.sdk.video.TruvideoSdkVideo
import com.truvideo.sdk.video.TruvideoSdkVideoInitializer
import com.truvideo.sdk.video.model.TruvideoSdkVideoFile
import com.truvideo.sdk.video.model.TruvideoSdkVideoFileDescriptor
import com.truvideo.sdk.video.model.TruvideoSdkVideoFrameRate
import com.truvideo.sdk.video.model.TruvideoSdkVideoInformation
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequest
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestStatus
import com.truvideo.sdk.video.model.TruvideoSdkVideoRequestType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File

class DotnetTruvideoVideo {
    companion object {
        var mainCallback: VideoCallback? = null

        @JvmStatic
        fun getResultPath(context: Context, name: String, callback: VideoCallback) {
            // get result path with dynamic name
            //var outputPath = File("${context.filesDir}/${name}").path
            var outputPath = File("${name}").path
            callback.onSuccess(outputPath)
        }

        @JvmStatic
        fun initAppVideoInitializer(context: Context, callback: VideoCallback) {
            GlobalScope.launch(Dispatchers.Main) {
                AppInitializer.getInstance(context.applicationContext)
                    .initializeComponent(TruvideoSdkVideoInitializer::class.java)
                callback.onSuccess("Video Initializer")
            }
        }

        @JvmStatic
        fun editVideo(
            context: Context,
            inputPath: String,
            outputPath: String,
            callback: VideoCallback
        ) {
            CoroutineScope(Dispatchers.Main).launch {
                AppInitializer.getInstance(context.applicationContext)
                    .initializeComponent(TruvideoSdkVideoInitializer::class.java)
                mainCallback = callback
                val intent = Intent(context.applicationContext, VideoActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.applicationContext.startActivity(
                    intent.putExtra("inputPath", inputPath).putExtra("outputPath", outputPath)
                )
            }
        }


        @JvmStatic
        fun getVideoInfo(context: Context, inputPath: String, callback: VideoCallback) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    var input_Path = videoFile(inputPath)
                    val info: TruvideoSdkVideoInformation = TruvideoSdkVideo.getInfo(input_Path)
                    // Handle video information
                    val duration: Long = info.durationMillis
                    val width: Long = info.size
                    val gson = Gson()
                    val jsonResult = gson.toJson(info)
                    callback.onSuccess(jsonResult)
                } catch (exception: Exception) {
                    exception.printStackTrace()
                    // Handle error
                }
            }
        }

        @JvmStatic
        fun generateThumbnail(
            context: Context,
            inputPath: String,
            outputPath: String,
            position: Long,
            width: Int,
            height: Int,
            callback: VideoCallback
        ) {
            var input_Path = videoFile(inputPath)
            var output_Path = videoFileDescriptor(outputPath)
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val resultPath: String = TruvideoSdkVideo.createThumbnail(
                        input = input_Path,
                        output = output_Path,
                        position = position,
                        width = width, // or null
                        height = height // or null
                    )
                    callback.onSuccess(resultPath)
                    // Handle result
                    // the thumbnail image is stored in resultPath
                } catch (exception: Exception) {
                    // Handle error
                    exception.printStackTrace()
                }
            }

        }

        @JvmStatic
        fun clearNoise(
            context: Context,
            inputPath: String,
            outputPath: String,
            callback: VideoCallback
        ) {
            var input_Path = videoFile(inputPath)
            var output_Path = videoFileDescriptor(outputPath)
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val outputPath = TruvideoSdkVideo.clearNoise(input_Path, output_Path)
                    callback.onSuccess(outputPath)
                    // The cleaned video is stored in outputPath
                } catch (exception: Exception) {
                    // Handle error
                    exception.printStackTrace()
                }
            }
        }

        @JvmStatic
        fun mergeVideos(
            context: Context,
            arrayList: List<String>,
            outputPath: String,
            width: Int,
            height: Int,
            framesRateString: String,
            callback: VideoCallback
        ) {
            val inputpath = listVideoFile(arrayList)
            val outputpath = videoFileDescriptor(outputPath)
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val framesRate = if (framesRateString.equals("defaultFrameRate", true)) {
                        TruvideoSdkVideoFrameRate.defaultFrameRate
                    } else if (framesRateString.equals("twentyFourFps", true)) {
                        TruvideoSdkVideoFrameRate.twentyFourFps
                    } else if (framesRateString.equals("twentyFiveFps", true)) {
                        TruvideoSdkVideoFrameRate.twentyFiveFps
                    } else if (framesRateString.equals("thirtyFps", true)) {
                        TruvideoSdkVideoFrameRate.thirtyFps
                    } else if (framesRateString.equals("fiftyFps", true)) {
                        TruvideoSdkVideoFrameRate.fiftyFps
                    } else {
                        TruvideoSdkVideoFrameRate.sixtyFps
                    }
                    val builder = TruvideoSdkVideo.MergeBuilder(inputpath, outputpath)
                    // Set custom video resolution
                    builder.width = width
                    builder.height = height
                    builder.framesRate = framesRate
                    val request: TruvideoSdkVideoRequest = builder.build()
                   // val id = request.id
                    val resultPath: String = request.process()
                    callback.onSuccess(""+resultPath)
                    // Handle result
                    // the merged video its on 'resultPath'
                } catch (exception: Exception) {
                    //Handle error
                    exception.printStackTrace()
                }
            }
        }

        @JvmStatic
        fun streamAllRequests(context: Context, callback: VideoCallback) {
            val allRequest = TruvideoSdkVideo.streamAllRequests()
            val gson = Gson()
            val jsonResult = gson.toJson(allRequest)
            callback.onSuccess(jsonResult)
        }

        @JvmStatic
        fun getAllRequests(statusValue: String, callback: VideoCallback) {
            CoroutineScope(Dispatchers.Main).launch {
                val status = if (statusValue.equals("IDLE", true)) {
                    TruvideoSdkVideoRequestStatus.IDLE
                } else if (statusValue.equals("ERROR", true)) {
                    TruvideoSdkVideoRequestStatus.ERROR
                } else if (statusValue.equals("PROCESSING", true)) {
                    TruvideoSdkVideoRequestStatus.PROCESSING
                } else if (statusValue.equals("COMPLETED", true)) {
                    TruvideoSdkVideoRequestStatus.COMPLETED
                } else if (statusValue.equals("CANCELED", true)) {
                    TruvideoSdkVideoRequestStatus.CANCELED
                } else {
                    TruvideoSdkVideoRequestStatus.CANCELED
                }
                val allRequest = TruvideoSdkVideo.getAllRequests(status)
                //val gson = Gson()
                //val jsonResult = gson.toJson(allRequest)
                callback.onSuccess(""+allRequest)
            }
        }

        @JvmStatic
        fun status(id: String, callback: VideoCallback) {
            CoroutineScope(Dispatchers.Main).launch {
                val request = TruvideoSdkVideo.getRequestById(id)
                val status = request!!.status
                callback.onSuccess("" + status)
            }
        }

        @JvmStatic
        fun process(id: String, callback: VideoCallback) {
            CoroutineScope(Dispatchers.Main).launch {
                val request = TruvideoSdkVideo.getRequestById(id)
                val process = request!!.process()
                callback.onSuccess(process)
            }
        }

        @JvmStatic
        fun progress(id: String, callback: VideoCallback) {
            CoroutineScope(Dispatchers.Main).launch {
                val request = TruvideoSdkVideo.getRequestById(id)
                val progress = request!!.progress
                callback.onSuccess("" + progress)
            }
        }

        @JvmStatic
        fun cancel(id: String, callback: VideoCallback) {
            CoroutineScope(Dispatchers.Main).launch {
                val request = TruvideoSdkVideo.getRequestById(id)
                request!!.cancel()
                callback.onSuccess("Request Cancel")
            }
        }

        @JvmStatic
        fun delete(id: String, callback: VideoCallback) {
            CoroutineScope(Dispatchers.Main).launch {
                val request = TruvideoSdkVideo.getRequestById(id)
                request!!.delete()
                callback.onSuccess("Request delete")
            }
        }

        @JvmStatic
        fun type(id: String, callback: VideoCallback) {
            CoroutineScope(Dispatchers.Main).launch {
                val request = TruvideoSdkVideo.getRequestById(id)
                val type = request!!.type
                callback.onSuccess("" + type)
            }
        }

        @JvmStatic
        fun data(id: String, callback: VideoCallback) {
            CoroutineScope(Dispatchers.Main).launch {
                val request = TruvideoSdkVideo.getRequestById(id)
                val type = request!!.type
                val data = if (type == TruvideoSdkVideoRequestType.MERGE) {
                    request.mergeData
                } else if (type == TruvideoSdkVideoRequestType.ENCODE) {
                    request.encodeData
                } else {
                    request.concatData
                }
                callback.onSuccess("" + data)
            }
        }

        @JvmStatic
        fun encodeVideo(
            context: Context,
            inputPath: String,
            outputPath: String,
            width: Int,
            height: Int,
            framesRateString: String,
            callback: VideoCallback
        ) {
            var input_Path = videoFile(inputPath)
            var output_Path = videoFileDescriptor(outputPath)
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val framesRate = if (framesRateString.equals("defaultFrameRate", true)) {
                        TruvideoSdkVideoFrameRate.defaultFrameRate
                    } else if (framesRateString.equals("twentyFourFps", true)) {
                        TruvideoSdkVideoFrameRate.twentyFourFps
                    } else if (framesRateString.equals("twentyFiveFps", true)) {
                        TruvideoSdkVideoFrameRate.twentyFiveFps
                    } else if (framesRateString.equals("thirtyFps", true)) {
                        TruvideoSdkVideoFrameRate.thirtyFps
                    } else if (framesRateString.equals("fiftyFps", true)) {
                        TruvideoSdkVideoFrameRate.fiftyFps
                    } else {
                        TruvideoSdkVideoFrameRate.sixtyFps
                    }
                    val builder = TruvideoSdkVideo.EncodeBuilder(input_Path, output_Path)
                    // Set custom video resolution
                    builder.width = width
                    builder.height = height
                    builder.framesRate = framesRate
                    val request: TruvideoSdkVideoRequest = builder.build()
                   // val id = request.id
                    val resultPath: String = request.process()
                    callback.onSuccess(""+resultPath)
                    // Handle result
                    // the merged video its on 'resultPath'
                } catch (exception: Exception) {
                    //Handle error
                    exception.printStackTrace()
                }
            }
        }

        @JvmStatic
        fun concatVideos(
            context: Context,
            arrayList: List<String>,
            outputPath: String,
            callback: VideoCallback
        ) {
            var input_Path = listVideoFile(arrayList)
            var output_Path = videoFileDescriptor(outputPath)
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val builder = TruvideoSdkVideo.ConcatBuilder(input_Path, output_Path)
                    val request: TruvideoSdkVideoRequest = builder.build()
                    val id = request.id
                    val outputPath = request.process()
                    callback.onSuccess(outputPath)
                    // Handle result
                    // the concated video its on 'outputPath'
                } catch (exception: Exception) {
                    // Handle error
                    exception.printStackTrace()
                }
            }
        }

        @JvmStatic
        fun compareVideos(context: Context, arrayList: List<String>, callback: VideoCallback) {
            //Compare videos and return true or false if they are ready to concat
            var input_Path = listVideoFile(arrayList)
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val result = TruvideoSdkVideo.compare(input_Path)
                    // Handle result
                    if (result) {
                        // videos are ready to concat
                        callback.onSuccess("" + result)
                    } else {
                        // videos are needed to merge
                        callback.onSuccess("" + result)
                    }
                } catch (exception: Exception) {
                    // Handle error
                    exception.printStackTrace()
                }
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
}
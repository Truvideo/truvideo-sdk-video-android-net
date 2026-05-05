package com.truvideo.video

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Build
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
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.format.DateTimeFormatter

class DotnetTruvideoVideo {
    companion object {
        var mainCallback: VideoCallback? = null

        @SuppressLint("SuspiciousIndentation")
        @JvmStatic
        fun getResultPath(context: Context, name: String, callback: VideoCallback) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val outputPath = File(name).path
                    callback.onSuccess(outputPath)

                } catch (exception: Exception) {
                    exception.printStackTrace()
                    callback.onFailure(exception.message ?: "Unknown error")

                }
            }
        }

        @JvmStatic
        fun version(callback: VideoCallback) {
            val version = TruvideoSdkVideo.version
            callback.onSuccess("" + version)
        }

        /* @JvmStatic
         fun initAppVideoInitializer(context: Context, callback: VideoCallback) {
             CoroutineScope(Dispatchers.IO).launch {
                 try {
                     AppInitializer.getInstance(context.applicationContext)
                         .initializeComponent(TruvideoSdkVideoInitializer::class.java)
                         callback.onSuccess("Video Initializer")

                 } catch (exception: Exception) {
                     exception.printStackTrace()
                     callback.onFailure(exception.message ?: "Unknown error")
                 }
             }
         }*/

        @JvmStatic
        fun initAppVideoInitializer(context: Context, callback: VideoCallback) {
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    AppInitializer.getInstance(context.applicationContext)
                        .initializeComponent(TruvideoSdkVideoInitializer::class.java)
                    callback.onSuccess("Video Initializer")
                }
            } catch (exception: Exception) {
                exception.printStackTrace()
                callback.onFailure(exception.message ?: "Unknown error")
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
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    val input = videoFile(inputPath)
                    val output = videoFileDescriptor(outputPath)
                    val resultPath = TruvideoSdkVideo.createThumbnail(
                        input = input,
                        output = output,
                        position = position,
                        width = width,
                        height = height,
                        useMedia3 = true
                    )
                    callback.onSuccess(resultPath)

                } catch (exception: Exception) {
                    exception.printStackTrace()
                    callback.onFailure(exception.message ?: "Unknown error")

                }
            }
        }


        @JvmStatic
        fun editVideo(
            context: Context,
            inputPath: String,
            outputPath: String,
            callback: VideoCallback
        ) {
            if(inputPath.endsWith(".png") || inputPath.endsWith(".jpg") || inputPath.endsWith(".jpeg")){
                callback.onFailure("video path must be video not image")
                return
            }
            CoroutineScope(Dispatchers.IO).launch {
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
            if(inputPath== null){
                callback.onFailure("input path is not valid")
                return
            }
            if(inputPath.endsWith(".png") || inputPath.endsWith(".jpg") || inputPath.endsWith(".jpeg")){
                callback.onFailure("video path must be video not image")
            }

            try{
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val inputPath1 = videoFile(inputPath)
                        val info: TruvideoSdkVideoInformation = TruvideoSdkVideo.getInfo(inputPath1,true)
                        // Handle video information
                        val duration: Long = info.durationMillis
                        val width: Long = info.size
                        val gson = Gson()
                        val jsonResult = gson.toJson(info)
                        callback.onSuccess(jsonResult)
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                        callback.onFailure("Get video info failed: ${exception.message}")
                        // Handle error
                    }
                }
            } catch (e: Exception) {
                callback.onFailure("Get video info failed: ${e.message}")
            }
        }

        /* @JvmStatic
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
             CoroutineScope(Dispatchers.IO).launch {
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

         }*/

        @JvmStatic
        fun clearNoise(
            context: Context,
            inputPath: String,
            outputPath: String,
            callback: VideoCallback
        ) {
            if (inputPath.isEmpty() || outputPath.isEmpty()) {
                callback.onFailure("input path or result path not valid")
                return
            }
            if(outputPath.endsWith(".png") || outputPath.endsWith(".jpg") || outputPath.endsWith(".jpeg")){
                callback.onFailure("video path must be video not image")
                return
            }
            try {
                val inputPath1 = videoFile(inputPath)
                val outputPath1 = videoFileDescriptor(outputPath)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val result = TruvideoSdkVideo.clearNoise(inputPath1, outputPath1,true)
                        callback.onSuccess(result)
                        // The cleaned video is stored in outputPath
                    } catch (exception: Exception) {
                        // Handle error
                        callback.onFailure("Clear noise failed: ${exception.message}")
                        exception.printStackTrace()
                    }
                }
            }catch (exception:Exception){
                // Handle error
                callback.onFailure("Clear noise failed: ${exception.message}")
                exception.printStackTrace()
            }
        }

       /* @JvmStatic
        fun streamAllRequests(context: Context, callback: VideoCallback) {
            try{
                val allRequest = TruvideoSdkVideo.streamAllRequests()
                val gson = Gson()
                val jsonResult = gson.toJson(allRequest)
                callback.onSuccess(jsonResult)
            } catch (e: Exception) {
                callback.onFailure("Stream all request failed: ${e.message}")
            }
        }
*/
        @JvmStatic
        fun getAllRequests(statusValue: String, callback: VideoCallback) {
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    val status = if (statusValue.equals("IDLE", true)) {
                        TruvideoSdkVideoRequestStatus.IDLE
                    } else if (statusValue.equals("ERROR", true)) {
                        TruvideoSdkVideoRequestStatus.ERROR
                    } else if (statusValue.equals("PROCESSING", true)) {
                        TruvideoSdkVideoRequestStatus.PROCESSING
                    } else if (statusValue.equals("COMPLETED", true)) {
                        TruvideoSdkVideoRequestStatus.COMPLETE
                    } else if (statusValue.equals("CANCELED", true)) {
                        TruvideoSdkVideoRequestStatus.CANCELLED
                    } else {
                        TruvideoSdkVideoRequestStatus.CANCELLED
                    }
                    val allRequest = TruvideoSdkVideo.getAllRequests(status)
                    //val gson = Gson()
                    //val jsonResult = gson.toJson(allRequest)
                    callback.onSuccess("" + allRequest)
                }
            } catch (e: Exception) {
                callback.onFailure("Get all request failed: ${e.message}")
            }
        }

        @JvmStatic
        fun status(id: String, callback: VideoCallback) {
            try{
                CoroutineScope(Dispatchers.IO).launch {
                    val request = TruvideoSdkVideo.getRequestById(id)
                    val status = request!!.status
                    callback.onSuccess("" + status)
                }
            } catch (e: Exception) {
                callback.onFailure("Status failed: ${e.message}")
            }
        }

//        @JvmStatic
//        fun process(id: String, callback: VideoCallback) {
//            try {
//                CoroutineScope(Dispatchers.IO).launch {
//                    val request = TruvideoSdkVideo.getRequestById(id)
//                    val process = request!!.process(true)
//                    callback.onSuccess(process)
//                }
//            } catch (e: Exception) {
//                callback.onFailure("Process failed: ${e.message}")
//            }
//        }


        @JvmStatic
        fun process(id: String, callback: VideoCallback) {
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                try {
                    val request = TruvideoSdkVideo.getRequestById(id)
                    if (request == null) {
                        callback.onFailure("Request not found for id: $id")
                        return@launch
                    }

                    val resultPath = request.process(true)  // ✅ Execute processing
                    callback.onSuccess(resultPath)

                } catch (e: Exception) {
                    e.printStackTrace()
                    callback.onFailure("Process failed: ${e.message ?: "Unknown error"}")
                }
            }
        }

        @JvmStatic
        fun progress(id: String, callback: VideoCallback) {
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    val request = TruvideoSdkVideo.getRequestById(id)
                    val progress = request!!.progress
                    callback.onSuccess("" + progress)
                }
            } catch (e: Exception) {
                callback.onFailure("Progress failed: ${e.message}")
            }
        }

       @JvmStatic
       fun cancel(id: String, callback: VideoCallback) {
           CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
               try {
                   val request = TruvideoSdkVideo.getRequestById(id)
       
                   if (request == null) {
                       callback.onFailure("Request not found")
                       return@launch
                   }
       
                   request.cancel(true)
                   callback.onSuccess("Request Cancel")
       
               } catch (e: Exception) {
                   callback.onFailure("Cancel failed: ${e.message ?: "Unknown error"}")
               }
           }
       }

        @JvmStatic
        fun delete(id: String, callback: VideoCallback) {
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                try{
                    val request = TruvideoSdkVideo.getRequestById(id)
                    request!!.delete()
                    callback.onSuccess("Request delete")

            } catch (e: Exception) {
                callback.onFailure("Delete failed: ${e.message}")
            }
                }
        }

        @JvmStatic
        fun type(id: String, callback: VideoCallback) {
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    val request = TruvideoSdkVideo.getRequestById(id)
                    val type = request!!.type
                    callback.onSuccess("" + type)
                }
            } catch (e: Exception) {
                callback.onFailure("Type failed: ${e.message}")
            }
        }

        @JvmStatic
        fun data(id: String, callback: VideoCallback) {
            try {
                CoroutineScope(Dispatchers.IO).launch {
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
            } catch (e: Exception) {
                callback.onFailure("Data failed: ${e.message}")
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
            val inputPath = listVideoFile(arrayList)
            val outputPath1 = videoFileDescriptor(outputPath)
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                try {
                    val framesRates = if (framesRateString.equals("defaultFrameRate", true)) {
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
                    try {
                        val builder = TruvideoSdkVideo.MergeBuilder(inputPath, outputPath1)
                        // Set custom video resolution
                        builder.width = width
                        builder.height = height
                        builder.framesRate = framesRates
                        val request: TruvideoSdkVideoRequest = builder.build()

                        callback.onSuccess(returnRequest(request))

                        //val resultPath: String = request.process()
                        //callback.onSuccess(""+resultPath)
                        // Handle result
                        // the merged video its on 'resultPath'
                    } catch (exception: Exception) {
                        callback.onFailure(exception.message ?: "Merge failed")   // ✅ MUST
                    }
                } catch (exception: Exception) {
                    //Handle error
                    //exception.printStackTrace()
                    callback.onFailure(exception.message ?: "Merge failed")
                }
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
            val inputPath1 = videoFile(inputPath)
            val outputPath1 = videoFileDescriptor(outputPath)
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
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
                    try {
                        val builder = TruvideoSdkVideo.EncodeBuilder(inputPath1, outputPath1)
                        // Set custom video resolution
                        builder.width = width
                        builder.height = height
                        builder.framesRate = framesRate
                        val request: TruvideoSdkVideoRequest = builder.build()
                        callback.onSuccess(returnRequest(request))

                        // val resultPath: String = request.process()
                        //callback.onSuccess(""+resultPath)
                        // Handle result
                        // the merged video its on 'resultPath'
                    } catch (exception: Exception) {
                        // exception.printStackTrace()
                        callback.onFailure(exception.message ?: "Encoding failed")
                    }

                } catch (exception: Exception) {
                    //Handle error
                    //exception.printStackTrace()
                    callback.onFailure(exception.message ?: "Encoding failed")
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
            val inputPath = listVideoFile(arrayList)
            val outputPath1 = videoFileDescriptor(outputPath)
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                try {
                    val builder = TruvideoSdkVideo.ConcatBuilder(inputPath, outputPath1)
                    val request: TruvideoSdkVideoRequest = builder.build()
                    callback.onSuccess(returnRequest(request))

                    // val resultPath = request.process()
                    //callback.onSuccess(resultPath)
                    // Handle result
                    // the concated video its on 'resultPath'
                } catch (exception: Exception) {
                    // Handle error
                    //exception.printStackTrace()
                    callback.onFailure(exception.message ?: "Concat failed")
                }
            }
        }

        @JvmStatic
        fun compareVideos(context: Context, arrayList: List<String>, callback: VideoCallback) {
            if(arrayList== null ){
                callback.onFailure("input path or result path not valid")
                return
            }
            try {
                arrayList.forEach {
                    if(it.endsWith(".png") || it.endsWith(".jpg") || it.endsWith(".jpeg")){
                        callback.onFailure("input path must be video not image")
                        return
                    }
                }
                //Compare videos and return true or false if they are ready to concat
                val inputPath = listVideoFile(arrayList)
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val result = TruvideoSdkVideo.compare(inputPath,true)
                        // Handle result
                        if (result) {
                            callback.onSuccess("" +result)
                        } else {
                            callback.onSuccess("" + result)
                        }
                    } catch (e: Exception) {
                        // Handle error
                        e.printStackTrace()
                        callback.onFailure("Compare video failed: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                callback.onFailure("Compare video failed: ${e.message}")
            }
        }


        @JvmStatic
        fun reEncodeVideo(
            context: Context,
            inputPath: String,
            outputPath: String,
            callback: VideoCallback
        ) {
            if (inputPath.isEmpty() || outputPath.isEmpty()) {
                callback.onFailure("Input or output path is invalid")
                return
            }

            if (outputPath.endsWith(".png") ||
                outputPath.endsWith(".jpg") ||
                outputPath.endsWith(".jpeg")
            ) {
                callback.onFailure("Output must be a video file")
                return
            }

            val handler = CoroutineExceptionHandler { _, exception ->
                exception.printStackTrace()
                callback.onFailure(exception.message ?: "Unknown coroutine error")
            }

            CoroutineScope(Dispatchers.IO + handler).launch {
                try {
                    val input = videoFile(inputPath)
                    val output = videoFileDescriptor(outputPath)
                    val result = TruvideoSdkVideo.ReEncode(
                        input,
                        output
                    )
                    callback.onSuccess(result)

                } catch (e: Exception) {
                    e.printStackTrace()
                    callback.onFailure("Re-encode failed: ${e.message}")
                }
            }
        }

        @JvmStatic
        fun streamAllRequests(
            context: Context,
            callback: VideoCallback
        ) {
            try {

                val liveData = TruvideoSdkVideo.streamAllRequests()

                liveData.observeForever { requests ->

                    try {
                        if (requests == null) {
                            callback.onFailure("No requests available")
                            return@observeForever
                        }

                        val json = returnRequests(requests)

                        callback.onSuccess(json)

                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback.onFailure(e.message ?: "Stream conversion failed")
                    }

                }

            } catch (e: Exception) {
                e.printStackTrace()
                callback.onFailure("Stream all requests failed: ${e.message}")
            }
        }

        @JvmStatic
        fun streamRequestById(
            id: String,
            callback: VideoCallback
        ) {
            if (id.isEmpty()) {
                callback.onFailure("Request id is empty")
                return
            }

            try {
                val liveData = TruvideoSdkVideo.streamRequestById(id)
                liveData.observeForever { request ->
                    try {
                        if (request == null) {
                            callback.onFailure("Request not found")
                            return@observeForever
                        }

                        val json = returnRequest(request)

                        callback.onSuccess(json)

                    } catch (e: Exception) {
                        e.printStackTrace()
                        callback.onFailure(e.message ?: "Stream request failed")
                    }

                }

            } catch (e: Exception) {
                e.printStackTrace()
                callback.onFailure("Stream request by id failed: ${e.message}")
            }
        }

        @JvmStatic
        fun getRequestById(
            id: String,
            callback: VideoCallback
        ) {
            if (id.isEmpty()) {
                callback.onFailure("Request id is empty")
                return
            }

            val handler = CoroutineExceptionHandler { _, exception ->
                exception.printStackTrace()
                callback.onFailure(exception.message ?: "Unknown coroutine error")
            }

            CoroutineScope(Dispatchers.IO + handler).launch {
                try {
                    val request = TruvideoSdkVideo.getRequestById(id)
                    if (request == null) {
                        callback.onFailure("Request not found")
                        return@launch
                    }
                    val json = returnRequest(request)
                    callback.onSuccess(json)

                } catch (e: Exception) {
                    e.printStackTrace()
                    callback.onFailure("Get request by id failed: ${e.message}")
                }
            }
        }

        @JvmStatic
        fun cleanupFFmpegSessions(callback: VideoCallback) {
            try {
                TruvideoSdkVideo.cleanupFFmpegSessionsIfNeeded()
                callback.onSuccess("FFmpeg sessions cleaned")
            } catch (e: Exception) {
                e.printStackTrace()
                callback.onFailure("Cleanup failed: ${e.message}")
            }
        }

        @JvmStatic
        fun setFFmpegSessionLimit(limit: Int, callback: VideoCallback) {
            if (limit <= 0) {
                callback.onFailure("Limit must be greater than 0")
                return
            }
            try {
                TruvideoSdkVideo.setFFmpegMaxSessionLimit(limit)
                callback.onSuccess("FFmpeg session limit set to $limit")
            } catch (e: Exception) {
                e.printStackTrace()
                callback.onFailure("Set session limit failed: ${e.message}")
            }
        }

        private fun returnRequest(request : TruvideoSdkVideoRequest) : String{
            return JSONObject().apply{
                put("id",request.id)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    put("createdAt", DateTimeFormatter.ISO_INSTANT.format(request.createdAt.toInstant()))
                    put("updatedAt", DateTimeFormatter.ISO_INSTANT.format(request.updatedAt.toInstant()))
                }else{
                    put("createdAt", request.createdAt)
                    put("updatedAt", request.updatedAt)
                }
                put("status", when(request.status){
                    TruvideoSdkVideoRequestStatus.IDLE -> "idle"
                    TruvideoSdkVideoRequestStatus.PROCESSING -> "processing"
                    TruvideoSdkVideoRequestStatus.ERROR -> "error"
                    TruvideoSdkVideoRequestStatus.COMPLETE -> "complete"
                    TruvideoSdkVideoRequestStatus.CANCELLED -> "cancelled"
                })
                put("type", request.type.name.lowercase())

            }.toString()
        }

        fun returnRequests(requests: List<TruvideoSdkVideoRequest>): String {
            val jsonArray = JSONArray()

            for (request in requests) {
                val jsonString = returnRequest(request)
                try {
                    val jsonObject = JSONObject(jsonString)
                    jsonArray.put(jsonObject)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            return jsonArray.toString()
        }



        private fun videoFile(inputPath: String): TruvideoSdkVideoFile {
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
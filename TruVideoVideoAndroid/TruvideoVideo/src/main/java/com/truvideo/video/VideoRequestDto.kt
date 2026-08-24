package com.truvideo.video

data class VideoRequestDto(
    val id: String?,
    val errorMessage: String?,
    val createdAtMillis: Long?,
    val updatedAtMillis: Long?,
    val progress: Float?,
    val status: String?,
    val type: String?,
    val encodeData: VideoEncodeDataDto?,
    val concatData: VideoConcatDataDto?,
    val mergeData: VideoMergeDataDto?
)

data class VideoEncodeDataDto(
    val inputPath: String?,
    val outputPath: String?,
    val resultPath: String?,
)

data class VideoConcatDataDto(
    val inputPaths: List<String>?,
    val outputPath: String?,
    val resultPath: String?
)

data class VideoMergeDataDto(
    val inputPaths: List<String>?,
    val outputPath: String?,
    val resultPath: String?
)


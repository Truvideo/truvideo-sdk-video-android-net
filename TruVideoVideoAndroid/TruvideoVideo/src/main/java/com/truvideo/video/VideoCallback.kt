package com.truvideo.video

interface VideoCallback {
    fun onSuccess(result: String?)
    fun onFailure(error: String?)
}
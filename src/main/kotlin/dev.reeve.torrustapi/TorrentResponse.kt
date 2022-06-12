package dev.reeve.torrustapi

import com.google.gson.annotations.SerializedName

data class TorrentResponse(@SerializedName("torrent_id")var torrent: Long)
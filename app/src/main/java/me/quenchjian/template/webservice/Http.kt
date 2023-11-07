package me.quenchjian.template.webservice

import androidx.annotation.WorkerThread
import okio.IOException
import java.io.File

@WorkerThread
interface Http {

  @Throws(IOException::class)
  suspend fun sendRequest(request: Request): String

  @Throws(IOException::class)
  suspend fun download(url: String, dest: File, onProgress: (DownloadProgress) -> Unit = {})

  enum class Method { GET, HEAD, POST, PUT, DELETE, OPTIONS, PATCH }

  enum class ContentType(val contentType: String) {
    TEXT("text/plain"),
    FORM("application/x-www-form-urlencoded"),
    JSON("application/json"),
    MULTIPART("multipart/form-data")
  }

  class Request {
    var url = ""
      private set
    var method = Method.GET
      private set
    var contentType = ContentType.TEXT
      private set
    val queryParams = mutableMapOf<String, String>()
    val headers = mutableMapOf<String, String>()
    val params = mutableMapOf<String, String>()
    val files = mutableMapOf<String, File>()

    fun setUrl(url: String) = apply { this.url = url }
    fun setMethod(method: Method) = apply { this.method = method }
    fun setContentType(contentType: ContentType) = apply { this.contentType = contentType }
    fun addQueryParam(key: String, value: String) = apply { queryParams[key] = value }
    fun addHeader(key: String, value: String) = apply { headers[key] = value }
    fun addParam(key: String, value: String) = apply { params[key] = value }
    fun addFile(key: String, file: File) = apply { files[key] = file }
  }

  class Exception(
    val statusCode: Int,
    val msg: String,
    val body: String,
  ) : IOException("$statusCode $msg")

  class DownloadProgress(private val file: File, val contentLength: Long) {
    val downloaded: Long get() = file.length()
    val percent: Double get() = downloaded * 100.0 / contentLength
  }
}
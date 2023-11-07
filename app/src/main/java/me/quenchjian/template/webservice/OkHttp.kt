package me.quenchjian.template.webservice

import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.IOException
import okio.buffer
import okio.sink
import okio.source
import org.json.JSONObject
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

open class OkHttp(private val client: OkHttpClient) : Http {

  private val executor by lazy { Executors.newScheduledThreadPool(3) }

  override suspend fun sendRequest(request: Http.Request): String {
    client.newCall(makeRequest(request)).execute().use { resp ->
      val body = resp.body ?: throw IOException("body is null") // should never happen
      if (!resp.isSuccessful) {
        throw Http.Exception(resp.code, resp.message, body.string())
      }
      return body.string()
    }
  }

  override suspend fun download(
    url: String,
    dest: File,
    onProgress: (Http.DownloadProgress) -> Unit
  ) {
    require(url.isNotEmpty()) { "Download url is empty." }
    require(dest.isFile) { "Destination is a directory: ${dest.path}." }
    client.newCall(Request.Builder().url(url).build()).execute().use { resp ->
      val body = resp.body ?: throw IOException("body is null") // should never happen
      if (!resp.isSuccessful) {
        throw Http.Exception(resp.code, resp.message, body.string())
      }
      val future = scheduleProgress(Http.DownloadProgress(dest, body.contentLength()), onProgress)
      try {
        body.byteStream().source().buffer().use { input ->
          dest.sink().buffer().use { output -> output.writeAll(input) }
        }
      } finally {
        future.cancel(true)
      }
    }
  }

  private fun makeRequest(request: Http.Request): Request {
    return Request.Builder()
      .url(makeUrl(request.url, request.queryParams))
      .headers(makeHeaders(request.headers))
      .method(request.method.name, makeRequestBody(request))
      .build()
  }

  private fun makeUrl(url: String, queryParams: Map<String, String>): HttpUrl {
    val builder = url.toHttpUrl().newBuilder()
    queryParams.forEach { (k, v) -> builder.addQueryParameter(k, v) }
    return builder.build()
  }

  private fun makeHeaders(headers: Map<String, String>): Headers {
    val builder = Headers.Builder()
    headers.forEach { (k, v) -> builder.add(k, v) }
    return builder.build()
  }

  private fun makeRequestBody(request: Http.Request): RequestBody? {
    return when (request.method) {
      Http.Method.GET, Http.Method.HEAD -> null
      else -> when (val type = request.contentType) {
        Http.ContentType.TEXT -> "".toRequestBody(type.contentType.toMediaType())
        Http.ContentType.FORM -> makeFormBody(request.params)
        Http.ContentType.JSON -> makeJsonBody(request.params)
        Http.ContentType.MULTIPART -> makeMultipartBody(request.params, request.files)
      }
    }
  }

  private fun makeFormBody(params: Map<String, String>): RequestBody {
    val builder = FormBody.Builder()
    params.forEach { (k, v) -> builder.add(k, v) }
    return builder.build()
  }

  private fun makeJsonBody(params: Map<String, String>): RequestBody {
    val mediaType = Http.ContentType.JSON.contentType.toMediaType()
    return JSONObject(params).toString().toRequestBody(mediaType)
  }

  private fun makeMultipartBody(
    params: Map<String, String>,
    files: Map<String, File>
  ): RequestBody {
    val builder = MultipartBody.Builder()
    params.forEach { (k, v) -> builder.addFormDataPart(k, v) }
    files.forEach { (k, v) ->
      builder.addFormDataPart(
        k,
        v.name,
        v.asRequestBody(MultipartBody.FORM)
      )
    }
    return builder.build()
  }

  private fun scheduleProgress(progress: Http.DownloadProgress, listener: (Http.DownloadProgress) -> Unit): ScheduledFuture<*> {
    val updater = { listener.invoke(progress) }
    // 16 milliseconds ~= 60 FPS
    return executor.scheduleAtFixedRate(updater, 0, 16, TimeUnit.MILLISECONDS)
  }
}
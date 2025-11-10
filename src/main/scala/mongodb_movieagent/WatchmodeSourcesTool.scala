package mongodb_movieagent

import dev.langchain4j.agentic.Agent
import dev.langchain4j.service.V

import java.net.URI
import java.net.http.{HttpClient, HttpRequest, HttpResponse}

class WatchmodeSourcesTool(private val apiKey: String) {
  final private val http = HttpClient.newHttpClient

  @Agent(value = "Get streaming availability for a Watchmode ID", outputKey = "streamingSources") 
  def getSources(@V("watchmodeId") id: String, @V("region") region: String): String = try {
    val url = String.format("https://api.watchmode.com/v1/title/%s/sources/?apiKey=%s&regions=%s", id, apiKey, region)
    val req = HttpRequest.newBuilder.uri(URI.create(url)).GET.build
    val resp = http.send(req, HttpResponse.BodyHandlers.ofString)
    resp.body
  } catch {
    case e: Exception =>
      "Error retrieving sources: " + e.getMessage
  }
}
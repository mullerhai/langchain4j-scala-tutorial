package mongodb_movieagent

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import dev.langchain4j.agentic.Agent
import dev.langchain4j.service.V

import java.net.{URI, URLEncoder}
import java.net.http.{HttpClient, HttpRequest, HttpResponse}
import java.nio.charset.StandardCharsets

class WatchmodeSearchTool(private val apiKey: String) {
  final private val http = HttpClient.newHttpClient

  @Agent(value = "Find Watchmode ID for a given movie title", outputKey = "watchmodeId") 
  def getWatchmodeId(@V("title") title: String): String = try {
    val url = String.format("https://api.watchmode.com/v1/search/?apiKey=%s&search_field=name&search_value=%s&types=movie", apiKey, URLEncoder.encode(title, StandardCharsets.UTF_8))
    val req = HttpRequest.newBuilder.uri(URI.create(url)).GET.build
    val resp = http.send(req, HttpResponse.BodyHandlers.ofString)
    val root = new ObjectMapper().readTree(resp.body)
    val results = root.get("title_results")
    if (results != null && !results.isEmpty) {
      val id = results.get(0).get("id").asInt
      System.out.println("Watchmode ID: " + id)
      return String.valueOf(id)
    }
    "No Watchmode ID found for " + title
  } catch {
    case e: Exception =>
      "Error retrieving ID: " + e.getMessage
  }
}
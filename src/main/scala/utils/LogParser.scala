package utils

import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import utils.CustomLogging

import java.util.regex.{Matcher, Pattern}
import scala.jdk.CollectionConverters.*
object LogParser {
  private val objectMapper = new ObjectMapper

  def truncateString(input: String): String = {
    val maxChars = CustomLogging.getCharLimit
    if (input == null || input.length <= maxChars) return input
    val firstHalf = maxChars / 2
    val secondHalf = maxChars / 2
    input.substring(0, firstHalf) + "\n[... truncated ...]\n" + input.substring(input.length - secondHalf)
  }

  def logUserMessage(userMessage: String): Unit = {
    System.out.println("USER: " + truncateString(userMessage))
    System.out.println() // 2 newlines for clear separation
    System.out.println()
  }

  def logAssistantResponse(response: String): Unit = {
    System.out.println("MODEL: " + truncateString(response))
    System.out.println() // 2 newlines for clear separation
    System.out.println()
  }

  def logAvailableTools(tools: String): Unit = {
    System.out.println("\tAvailable tools: " + tools)
    System.out.println() // 2 newlines for clear separation
    System.out.println()
  }

  def logToolCallRequest(toolId: String, toolName: String, arguments: String): Unit = {
    System.out.println("MODEL REQUESTS TOOL CALL: " + toolName + " (id: " + toolId + ")")
    System.out.println("  Args: " + truncateString(arguments))
    System.out.println() // 2 newlines for clear separation
    System.out.println()
  }

  def logToolCallResult(toolId: String, toolName: String, result: String): Unit = {
    System.out.println("TOOL RESULT: " + toolName + " (id: " + toolId + ")")
    System.out.println("  Result: " + truncateString(result))
    System.out.println() // 2 newlines for clear separation
    System.out.println()
  }

  def parseHttpRequest(logMessage: String): Unit = {
    if (!logMessage.contains("HTTP request:") || !logMessage.contains("- body:")) return
    try {
      val jsonBody = extractJsonFromLog(logMessage)
      if (jsonBody == null) return
      val root = objectMapper.readTree(jsonBody)
      val messages = root.get("messages")
      val tools = root.get("tools")
      if (messages == null || !messages.isArray) return
      // Find the LAST message in the conversation (what's new)
      val lastMessage = messages.get(messages.size - 1)
      if (lastMessage == null) return
      val role = lastMessage.get("role").asText
      if ("user" == role) {
        // New user question
        val content = lastMessage.get("content").asText
        if (content != null && !content.isEmpty) logUserMessage(content)
        // Show available tools AFTER user message when tools are present
        if (tools != null && tools.isArray && tools.size > 0) {
          val toolNames = new StringBuilder
       
          for (tool <- tools.elements().asScala) {
            if (toolNames.length > 0) toolNames.append(", ")
            toolNames.append(tool.get("function").get("name").asText)
          }
          logAvailableTools(toolNames.toString)
        }
      }
      else if ("tool" == role) {
        // New tool result
        val toolCallId = lastMessage.get("tool_call_id").asText
        val content = lastMessage.get("content").asText
        val toolName = extractToolNameFromHistory(messages, toolCallId)
        logToolCallResult(toolCallId, toolName, content)
      }
      else if ("assistant" == role) {
        // Check if this is a final response (not a tool call)
        val toolCalls = lastMessage.get("tool_calls")
        if (toolCalls == null || !toolCalls.isArray || toolCalls.size == 0) {
          val content = lastMessage.get("content").asText
          if (content != null && !content.isEmpty) logAssistantResponse(content)
        }
      }
    } catch {
      case e: Exception =>


      // Ignore parsing errors
    }
  }

  private def extractToolNameFromHistory(messages: JsonNode, toolCallId: String): String = {

    for (message <- messages.elements().asScala) {
      val role = message.get("role").asText
      if ("assistant" == role) {
        val toolCalls = message.get("tool_calls")
        if (toolCalls != null && toolCalls.isArray) {
//          import scala.collection.JavaConversions.*
          for (toolCall <- toolCalls.elements().asScala) {
            val id = toolCall.get("id").asText
            if (toolCallId == id) return toolCall.get("function").get("name").asText
          }
        }
      }
    }
    "unknown"
  }

  def parseHttpResponse(logMessage: String): Unit = {
    if (!logMessage.contains("HTTP response:") || !logMessage.contains("- body:")) return
    try {
      val jsonBody = extractJsonFromLog(logMessage)
      if (jsonBody == null) return
      val root = objectMapper.readTree(jsonBody)
      val choices = root.get("choices")
      if (choices == null || !choices.isArray || choices.size == 0) return
      val message = choices.get(0).get("message")
      val content = message.get("content").asText
      // Check for tool calls first
      val toolCalls = message.get("tool_calls")
      if (toolCalls != null && toolCalls.isArray && toolCalls.size > 0) {
        // New tool call requests
     
        for (toolCall <- toolCalls.elements().asScala) {
          val toolId = toolCall.get("id").asText
          val toolName = toolCall.get("function").get("name").asText
          val arguments = toolCall.get("function").get("arguments").asText
          logToolCallRequest(toolId, toolName, arguments)
        }
      }
      else if (content != null && !content.isEmpty) {
        // New assistant response (no tool calls)
        logAssistantResponse(content)
      }
    } catch {
      case e: Exception =>


      // Ignore parsing errors
    }
  }

  private def extractJsonFromLog(logMessage: String): String = {
    // Find the JSON body after "- body:"
    val pattern = Pattern.compile("- body:\\s*(.*?)(?=\\n\\n|$)", Pattern.DOTALL)
    val matcher = pattern.matcher(logMessage)
    if (matcher.find) return matcher.group(1).trim
    null
  }
}
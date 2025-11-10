package utils

import dev.langchain4j.agentic.scope.AgenticScope

import java.util
import java.util.regex.{Matcher, Pattern}
import scala.jdk.CollectionConverters.*
import scala.util.control.Breaks.{break, breakable}

object AgenticScopePrinter {
  def printPretty(agenticScope: AgenticScope, maxChars: Int): String = {
    if (agenticScope == null) return "null"
    val sb = new StringBuilder
    sb.append("{\n")
    sb.append("  \"memoryId\": \"").append(agenticScope.memoryId).append("\",\n")
    sb.append("  \"state\": {\n")
    val state = agenticScope.state
    if (state == null || state.isEmpty) sb.append("    // empty\n")
    else {
      var count = 0
//      import scala.collection.JavaConversions.*
      for (entry <- state.entrySet().asScala) {
        val key = entry.getKey
        val value = entry.getValue
        if (count > 0) sb.append(",\n")
        sb.append("    \"").append(key).append("\": ")
        if (value == null) sb.append("null")
        else {
          val valueStr = value.toString
          if (valueStr.length <= maxChars) {
            val escaped = valueStr.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")
            sb.append("\"").append(escaped).append("\"")
          }
          else {
            val truncated = valueStr.substring(0, maxChars)
            val escaped = truncated.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t")
            sb.append("\"").append(escaped).append(" [truncated...]\"")
          }
        }
        count += 1
      }
      sb.append("\n")
    }
    sb.append("  }\n")
    sb.append("}")
    sb.toString
  }

  def printConversation(conversation: String, maxChars: Int): String = {
    if (conversation == null || conversation.isEmpty) return "(empty conversation)"
    val parts = conversation.split("(?m)(?=^User:|^\\w+\\s+agent:)") // <-- fixed
    val sb = new StringBuilder
    val agentPattern = Pattern.compile("^(\\w+)\\s+agent:(.*)$", Pattern.DOTALL)
    for (part <- parts) {
      breakable{
        if (part.trim.isEmpty) break
      }
      //continue //todo: continue is not supported
      val agentMatcher = agentPattern.matcher(part.trim)
      if (agentMatcher.matches) {
        val agentType = agentMatcher.group(1)
        val content = agentMatcher.group(2).trim
        sb.append(agentType).append(" agent:")
        if (!content.isEmpty) if (content.length > maxChars) sb.append(" ").append(content, 0, maxChars).append(" [truncated...]")
        else sb.append(" ").append(content)
      }
      else if (part.startsWith("User:")) {
        val content = part.substring(5).trim
        sb.append("User:")
        if (!content.isEmpty) if (content.length > maxChars) sb.append(" ").append(content, 0, maxChars).append(" [truncated...]")
        else sb.append(" ").append(content)
      }
      else if (part.length > maxChars) sb.append(part, 0, maxChars).append(" [truncated...]")
      else sb.append(part)
      sb.append("\n\n")
    }
    sb.toString.trim
  }
}
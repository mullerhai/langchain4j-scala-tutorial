package tutorial

import dev.langchain4j.code.judge0.Judge0JavaScriptExecutionTool
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI
import dev.langchain4j.service.AiServices

import java.time.Duration.ofSeconds

object _11_ServiceWithDynamicToolsExample {
  trait Assistant {
    def chat(message: String): String
  }

  def main(args: Array[String]): Unit = {
    val judge0Tool = new Judge0JavaScriptExecutionTool(ApiKeys.RAPID_API_KEY)
    val chatModel = OpenAiChatModel.builder.apiKey(ApiKeys.OPENAI_API_KEY).modelName(GPT_4_O_MINI).temperature(0.0).timeout(ofSeconds(60)).build
    val assistant = AiServices.builder(classOf[_11_ServiceWithDynamicToolsExample.Assistant]).chatModel(chatModel).chatMemory(MessageWindowChatMemory.withMaxMessages(20)).tools(judge0Tool).build
    interact(assistant, "What is the square root of 49506838032859?")
    interact(assistant, "Capitalize every third letter: abcabc")
    interact(assistant, "What is the number of hours between 17:00 on 21 Feb 1988 and 04:00 on 12 Apr 2014?")
  }

  private def interact(assistant: _11_ServiceWithDynamicToolsExample.Assistant, userMessage: String): Unit = {
    System.out.println("[User]: " + userMessage)
    val answer = assistant.chat(userMessage)
    System.out.println("[Assistant]: " + answer)
    System.out.println()
    System.out.println()
  }
}
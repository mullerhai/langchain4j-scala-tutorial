package jlama

import dev.langchain4j.data.message.{SystemMessage, UserMessage}
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.model.jlama.JlamaChatModel

object JlamaChatModelExamples {
  object Simple_Prompt {
    def main(args: Array[String]): Unit = {
      val model = JlamaChatModel.builder.modelName("tjake/Llama-3.2-1B-Instruct-JQ4").temperature(0.3f).build
      val chatResponse = model.chat(SystemMessage.from("You are helpful chatbot who is a java expert."), UserMessage.from("Write a java program to print hello world."))
      System.out.println("\n" + chatResponse.aiMessage.text + "\n")
    }
  }
}
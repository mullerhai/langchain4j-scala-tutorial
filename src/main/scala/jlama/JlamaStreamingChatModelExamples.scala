package jlama

import dev.langchain4j.data.message.{ChatMessage, SystemMessage, UserMessage}
import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.chat.response.{ChatResponse, StreamingChatResponseHandler}
import dev.langchain4j.model.jlama.JlamaStreamingChatModel

import java.util
import java.util.concurrent.CompletableFuture

object JlamaStreamingChatModelExamples {
  object Simple_Streaming_Prompt {
    def main(args: Array[String]): Unit = {
      val futureResponse = new CompletableFuture[ChatResponse]
      val model = JlamaStreamingChatModel.builder.modelName("tjake/Llama-3.2-1B-Instruct-JQ4").temperature(0.3f).build
      val messages = util.List.of(SystemMessage.from("You are a helpful chatbot that answers questions in under 30 words."), UserMessage.from("What is the best part of France and why?"))
      model.chat(messages, new StreamingChatResponseHandler() {
        override def onPartialResponse(partialResponse: String): Unit = {
          System.out.print(partialResponse)
        }

        override def onCompleteResponse(completeResponse: ChatResponse): Unit = {
          futureResponse.complete(completeResponse)
        }

        override def onError(error: Throwable): Unit = {
          futureResponse.completeExceptionally(error)
        }
      })
      futureResponse.join
    }
  }
}
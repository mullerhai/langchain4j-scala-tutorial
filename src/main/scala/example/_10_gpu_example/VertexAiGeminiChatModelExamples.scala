package example._10_gpu_example

import dev.langchain4j.model.chat.response.{ChatResponse, StreamingChatResponseHandler}
import dev.langchain4j.model.chat.{ChatModel, StreamingChatModel}
import dev.langchain4j.model.vertexai.gemini.{VertexAiGeminiChatModel, VertexAiGeminiStreamingChatModel}

import java.util.concurrent.CompletableFuture

object VertexAiGeminiChatModelExamples {
  /**
   * 1. Enable Vertex AI in Google Cloud Console
   * 2. Set your own project and location below
   */
  private val PROJECT = "langchain4j"
  private val LOCATION = "us-central1"
  private val MODEL_NAME = "gemini-pro"

  object Simple {
    def main(args: Array[String]): Unit = {
      val model = VertexAiGeminiChatModel.builder.project(PROJECT).location(LOCATION).modelName(MODEL_NAME).build
      val response = model.chat("Tell me a joke")
      System.out.println(response)
    }
  }

  object Streaming {
    def main(args: Array[String]): Unit = {
      val model = VertexAiGeminiStreamingChatModel.builder.project(PROJECT).location(LOCATION).modelName(MODEL_NAME).build
      val futureChatResponse = new CompletableFuture[ChatResponse]
      model.chat("Tell me a long joke", new StreamingChatResponseHandler() {
        override def onPartialResponse(partialResponse: String): Unit = {
          System.out.print(partialResponse)
        }

        override def onCompleteResponse(completeResponse: ChatResponse): Unit = {
          futureChatResponse.complete(completeResponse)
        }

        override def onError(error: Throwable): Unit = {
          futureChatResponse.completeExceptionally(error)
        }
      })
      futureChatResponse.join
    }
  }

  class Streaming {}
}
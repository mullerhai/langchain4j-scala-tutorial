package tutorial

import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilderFactory
import dev.langchain4j.model.chat.response.{ChatResponse, StreamingChatResponseHandler}
import dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI
import dev.langchain4j.model.openai.{OpenAiStreamingChatModel, OpenAiTokenCountEstimator}
import utils.ApiKeys

import java.util.concurrent.CompletableFuture

object _04_Streaming {
  def main(args: Array[String]): Unit = {
    val model = OpenAiStreamingChatModel.builder
      .baseUrl(ApiKeys.BASE_URL)
      .apiKey(ApiKeys.OPENAI_API_KEY)
      .modelName(ApiKeys.MODEL_NAME) //GPT_4_O_MINI)
      .httpClientBuilder(new SpringRestClientBuilderFactory().create())
      .build
    val prompt = "Write a short funny poem about developers and null-pointers, 10 lines maximum"
    System.out.println("Nr of chars: " + prompt.length)
    System.out.println("Nr of tokens: " + new OpenAiTokenCountEstimator(GPT_4_O_MINI).estimateTokenCountInText(prompt))
    val futureChatResponse = new CompletableFuture[ChatResponse]
    model.chat(prompt, new StreamingChatResponseHandler() {
      override def onPartialResponse(partialResponse: String): Unit = {
        System.out.print(partialResponse)
      }

      override def onCompleteResponse(completeResponse: ChatResponse): Unit = {
        System.out.println("\n\nDone streaming")
        futureChatResponse.complete(completeResponse)
      }

      override def onError(error: Throwable): Unit = {
        futureChatResponse.completeExceptionally(error)
      }
    })
    futureChatResponse.join
  }
}
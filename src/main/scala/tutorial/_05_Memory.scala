package tutorial

import dev.langchain4j.data.message.UserMessage.userMessage
import dev.langchain4j.data.message.{AiMessage, SystemMessage, UserMessage}
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilderFactory
import dev.langchain4j.memory.ChatMemory
import dev.langchain4j.memory.chat.TokenWindowChatMemory
import dev.langchain4j.model.chat.response.{ChatResponse, StreamingChatResponseHandler}
import dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI
import dev.langchain4j.model.openai.{OpenAiStreamingChatModel, OpenAiTokenCountEstimator}

import java.util.concurrent.{CompletableFuture, ExecutionException}

object _05_Memory {
  @throws[ExecutionException]
  @throws[InterruptedException]
  def main(args: Array[String]): Unit = {
    val model = OpenAiStreamingChatModel.builder
      .baseUrl(ApiKeys.BASE_URL)
      .apiKey(ApiKeys.OPENAI_API_KEY)
      .modelName(ApiKeys.MODEL_NAME) //GPT_4_O_MINI)
      .httpClientBuilder(new SpringRestClientBuilderFactory().create())
      .build
    val chatMemory = TokenWindowChatMemory.withMaxTokens(1000, new OpenAiTokenCountEstimator(GPT_4_O_MINI))
    val systemMessage = SystemMessage.from("You are a senior developer explaining to another senior developer, " + "the project you are working on is an e-commerce platform with Java back-end, " + "Oracle database, and Spring Data JPA")
    chatMemory.add(systemMessage)
    val userMessage1 = userMessage("How do I optimize database queries for a large-scale e-commerce platform? " + "Answer short in three to five lines maximum.")
    chatMemory.add(userMessage1)
    System.out.println("[User]: " + userMessage1.singleText)
    System.out.print("[LLM]: ")
    val aiMessage1 = streamChat(model, chatMemory)
    chatMemory.add(aiMessage1)
    val userMessage2 = userMessage("Give a concrete example implementation of the first point? " + "Be short, 10 lines of code maximum.")
    chatMemory.add(userMessage2)
    System.out.println("\n\n[User]: " + userMessage2.singleText)
    System.out.print("[LLM]: ")
    val aiMessage2 = streamChat(model, chatMemory)
    chatMemory.add(aiMessage2)
  }

  @throws[ExecutionException]
  @throws[InterruptedException]
  private def streamChat(model: OpenAiStreamingChatModel, chatMemory: ChatMemory) = {
    val futureAiMessage = new CompletableFuture[AiMessage]
    val handler = new StreamingChatResponseHandler() {
      override def onPartialResponse(partialResponse: String): Unit = {
        System.out.print(partialResponse)
      }

      override def onCompleteResponse(completeResponse: ChatResponse): Unit = {
        futureAiMessage.complete(completeResponse.aiMessage)
      }

      override def onError(throwable: Throwable): Unit = {
      }
    }
    model.chat(chatMemory.messages, handler)
    futureAiMessage.get
  }
}
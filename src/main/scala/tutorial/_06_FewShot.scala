package tutorial

import dev.langchain4j.data.message.{AiMessage, ChatMessage, UserMessage}
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilderFactory
import dev.langchain4j.model.chat.response.{ChatResponse, StreamingChatResponseHandler}
import dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI
import dev.langchain4j.model.openai.OpenAiStreamingChatModel

import java.time.Duration.ofSeconds
import java.util
import java.util.concurrent.CompletableFuture

object _06_FewShot {
  def main(args: Array[String]): Unit = {
    val model = OpenAiStreamingChatModel.builder
      .baseUrl(ApiKeys.BASE_URL)
      .apiKey(ApiKeys.OPENAI_API_KEY)
      .modelName(ApiKeys.MODEL_NAME) //GPT_4_O_MINI)
      .timeout(ofSeconds(100))
      .httpClientBuilder(new SpringRestClientBuilderFactory().create())
      .build
    val fewShotHistory = new util.ArrayList[ChatMessage]
    // Adding positive feedback example to history
    fewShotHistory.add(UserMessage.from("I love the new update! The interface is very user-friendly and the new features are amazing!"))
    fewShotHistory.add(AiMessage.from("Action: forward input to positive feedback storage\nReply: Thank you very much for this great feedback! We have transmitted your message to our product development team who will surely be very happy to hear this. We hope you continue enjoying using our product."))
    // Adding negative feedback example to history
    fewShotHistory.add(UserMessage.from("I am facing frequent crashes after the new update on my Android device."))
    fewShotHistory.add(AiMessage.from("Action: open new ticket - crash after update Android\nReply: We are so sorry to hear about the issues you are facing. We have reported the problem to our development team and will make sure this issue is addressed as fast as possible. We will send you an email when the fix is done, and we are always at your service for any further assistance you may need."))
    // Adding another positive feedback example to history
    fewShotHistory.add(UserMessage.from("Your app has made my daily tasks so much easier! Kudos to the team!"))
    fewShotHistory.add(AiMessage.from("Action: forward input to positive feedback storage\nReply: Thank you so much for your kind words! We are thrilled to hear that our app is making your daily tasks easier. Your feedback has been shared with our team. We hope you continue to enjoy using our app!"))
    // Adding another negative feedback example to history
    fewShotHistory.add(UserMessage.from("The new feature is not working as expected. It’s causing data loss."))
    fewShotHistory.add(AiMessage.from("Action: open new ticket - data loss by new feature\nReply:We apologize for the inconvenience caused. Your feedback is crucial to us, and we have reported this issue to our technical team. They are working on it on priority. We will keep you updated on the progress and notify you once the issue is resolved. Thank you for your patience and support."))
    // Adding real user's message
    val customerComplaint = UserMessage.from("How can your app be so slow? Please do something about it!")
    fewShotHistory.add(customerComplaint)
    System.out.println("[User]: " + customerComplaint.singleText)
    System.out.print("[LLM]: ")
    val futureChatResponse = new CompletableFuture[ChatResponse]
    model.chat(fewShotHistory, new StreamingChatResponseHandler() {
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
    // Extract reply and send to customer
    // Perform necessary action in back-end
  }
}
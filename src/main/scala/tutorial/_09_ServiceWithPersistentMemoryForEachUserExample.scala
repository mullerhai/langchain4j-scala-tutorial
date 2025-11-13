package tutorial

import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.ChatMessageDeserializer.messagesFromJson
import dev.langchain4j.data.message.ChatMessageSerializer.messagesToJson
import dev.langchain4j.http.client.spring.restclient.SpringRestClientBuilderFactory
import dev.langchain4j.memory.chat.{ChatMemoryProvider, MessageWindowChatMemory}
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI
import dev.langchain4j.service.{AiServices, MemoryId, UserMessage}
import dev.langchain4j.store.memory.chat.ChatMemoryStore
import org.mapdb.Serializer.{INTEGER, STRING}
import org.mapdb.{DB, DBMaker}
import utils.ApiKeys

import java.util

object _09_ServiceWithPersistentMemoryForEachUserExample {
  trait Assistant {
    def chat(@MemoryId memoryId: Int, @UserMessage userMessage: String): String
  }

  def main(args: Array[String]): Unit = {
    val store = new _09_ServiceWithPersistentMemoryForEachUserExample.PersistentChatMemoryStore
    val chatMemoryProvider:ChatMemoryProvider = (memoryId: AnyRef) => MessageWindowChatMemory.builder.id(memoryId).maxMessages(10).chatMemoryStore(store).build
    val model = OpenAiChatModel.builder
      .baseUrl(ApiKeys.BASE_URL)
      .apiKey(ApiKeys.OPENAI_API_KEY)
      .modelName(ApiKeys.MODEL_NAME) //GPT_4_O_MINI)
//      .timeout(ofSeconds(60))
      .httpClientBuilder(new SpringRestClientBuilderFactory().create())
      .build

    val assistant = AiServices.builder(classOf[_09_ServiceWithPersistentMemoryForEachUserExample.Assistant]).chatModel(model).chatMemoryProvider(chatMemoryProvider).build
    System.out.println(assistant.chat(1, "Hello, my name is Klaus"))
    System.out.println(assistant.chat(2, "Hi, my name is Francine"))
    // Now, comment out the two lines above, uncomment the two lines below, and run again.
    // System.out.println(assistant.chat(1, "What is my name?"));
    // System.out.println(assistant.chat(2, "What is my name?"));
  }

  // You can create your own implementation of ChatMemoryStore and store chat memory whenever you'd like
  class PersistentChatMemoryStore extends ChatMemoryStore {
    final private val db = DBMaker.fileDB("multi-user-chat-memory.db").transactionEnable.make
    final private val map = db.hashMap("messages", INTEGER, STRING).createOrOpen

    override def getMessages(memoryId: AnyRef): util.List[ChatMessage] = {
      val json = map.get(memoryId.asInstanceOf[Int])
      messagesFromJson(json)
    }

    override def updateMessages(memoryId: AnyRef, messages: util.List[ChatMessage]): Unit = {
      val json = messagesToJson(messages)
      map.put(memoryId.asInstanceOf[Int], json)
      db.commit()
    }

    override def deleteMessages(memoryId: AnyRef): Unit = {
      map.remove(memoryId.asInstanceOf[Int])
      db.commit()
    }
  }
}
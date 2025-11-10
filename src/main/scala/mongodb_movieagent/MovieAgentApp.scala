package mongodb_movieagent

import com.mongodb.client.*
import dev.langchain4j.agentic.AgenticServices
import dev.langchain4j.agentic.supervisor.SupervisorResponseStrategy
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.voyageai.VoyageAiEmbeddingModel
import dev.langchain4j.store.embedding.mongodb.{IndexMapping, MongoDbEmbeddingStore}
import org.bson.Document

import java.util

object MovieAgentApp {
  val databaseName = "movie_search"
  val collectionName = "movies"
  val indexName = "vector_index"

  @throws[InterruptedException]
  def main(args: Array[String]): Unit = {
    val embeddingApiKey = System.getenv("VOYAGE_AI_KEY")
    val mongodbUri = System.getenv("MONGODB_URI")
    val watchmodeKey = System.getenv("WATCHMODE_KEY")
    val openAiKey = System.getenv("OPENAI_KEY")
    val mongoClient = MongoClients.create(mongodbUri)
    val embeddingModel = VoyageAiEmbeddingModel.builder.apiKey(embeddingApiKey).modelName("voyage-3").build
    val indexMapping = IndexMapping.builder.dimension(embeddingModel.dimension).metadataFieldNames(new util.HashSet[String]).build
    val embeddingStore = MongoDbEmbeddingStore.builder.databaseName(databaseName)
      .collectionName(collectionName)
      .createIndex(checkIndexExists(mongoClient))
      .indexName(indexName)
      .indexMapping(indexMapping)
      .fromClient(mongoClient).build
    if (checkDataExists(mongoClient)) loadDataFromCSV(embeddingStore, embeddingModel)
    val planningModel = OpenAiChatModel.builder.apiKey(openAiKey).modelName("gpt-4o-mini").build
    val mongoSearch = new MongoDbMovieSearchTool(embeddingModel, embeddingStore)
    val watchmodeSearch = new WatchmodeSearchTool(watchmodeKey)
    val watchmodeSources = new WatchmodeSourcesTool(watchmodeKey)
    // Supervisor
    val supervisor = AgenticServices.supervisorBuilder(classOf[MovieSupervisor]).subAgents(mongoSearch, watchmodeSearch, watchmodeSources).supervisorContext(
      """
                    You are a movie assistant.
                    1. If the user gives a plot/description, call MongoDbMovieSearchTool to find the movie.
                    2. Use WatchmodeSearchTool with the title to get a Watchmode ID.
                    3. Use WatchmodeSourcesTool with that ID and the user's region to get streaming info.
                    4. Return a clean human-readable response.
                    """).chatModel(planningModel).responseStrategy(SupervisorResponseStrategy.SUMMARY).build
    // Example query
    val query = "Find me a sci-fi movie about rebels fighting an empire in space and tell me where to stream it in GB."
    val result = supervisor.invoke(query)
    System.out.println("Agent Response:\n" + result)
  }

  @throws[InterruptedException]
  def loadDataFromCSV(embeddingStore: MongoDbEmbeddingStore, embeddingModel: VoyageAiEmbeddingModel): Unit = {
    System.out.println("Loading data...")
    val embeddingService = new MovieEmbeddingService(embeddingStore, embeddingModel)
    embeddingService.ingestMoviesFromCsv()
    System.out.println("Movie data loaded successfully!")
    System.out.println("Waiting 5 seconds for indexing to complete...")
    Thread.sleep(5000)
  }

  def checkDataExists(mongoClient: MongoClient): Boolean = {
    val collection = mongoClient.getDatabase(databaseName).getCollection(collectionName)
    // check if the collection has any docs
    collection.find.first == null
  }

  def checkIndexExists(mongoClient: MongoClient): Boolean = {
    val collection = mongoClient.getDatabase(databaseName).getCollection(collectionName)
    try {
      val indexes = collection.listIndexes.iterator
      try while (indexes.hasNext) {
        val index = indexes.next
        if (indexName == index.getString(indexName)) return false
      }
      finally if (indexes != null) indexes.close()
    }
    true
  }
}
package mongodb_movieagent

import com.opencsv.bean.CsvToBeanBuilder
import dev.langchain4j.data.document.Metadata
import dev.langchain4j.data.embedding.Embedding
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.store.embedding.EmbeddingStore

import java.io.{InputStream, InputStreamReader}
import java.util.*
import scala.collection.mutable
import scala.jdk.CollectionConverters.*
import scala.util.control.Breaks.{break, breakable}

object MovieEmbeddingService {
  private def getMetadata(movie: Movie) = {
    val metadataMap = new mutable.HashMap[String, AnyRef]
    metadataMap.put("title", movie.title)
    metadataMap.put("year", movie.year)
    metadataMap.put("genre", movie.genre)
    metadataMap.put("director", movie.director)
    metadataMap.put("imdbRating", movie.imdbRating)
    metadataMap.put("star1", movie.star1)
    metadataMap.put("star2", movie.star2)
    new Metadata(metadataMap.asJava)
  }
}

class MovieEmbeddingService(private val embeddingStore: EmbeddingStore[TextSegment], private val embeddingModel: EmbeddingModel) {
  def ingestMoviesFromCsv(): Unit = {
    try {
      val inputStream = getClass.getClassLoader.getResourceAsStream("imdb_top_1000.csv")
      try {
        if (inputStream == null) throw new RuntimeException("imdb_top_1000.csv not found")
        val movies = new CsvToBeanBuilder(new InputStreamReader(inputStream)).withType(classOf[Movie]).build.parse
        System.out.println("Processing " + movies.size + " movies...")
//        import scala.collection.JavaConversions._
        for (movie <- movies.asScala) {
          breakable{
            if (movie.title == null || movie.overview == null) break //continue //todo: continue is not supported

          }
       
          val metadata = MovieEmbeddingService.getMetadata(movie)
          val segment = TextSegment.from(movie.overview, metadata)
          val embedding = embeddingModel.embed(segment).content
          embeddingStore.add(embedding, segment)
          System.out.println("Stored: " + movie.title)
        }
      } catch {
        case e: Exception =>
          System.err.println("Error processing CSV: " + e.getMessage)
          e.printStackTrace()
      } finally if (inputStream != null) inputStream.close()
    }
  }
}
package easyrag.shared

import dev.langchain4j.internal.Utils.getOrDefault
import org.slf4j.{Logger, LoggerFactory}

import java.net.{URISyntaxException, URL}
import java.nio.file.{FileSystems, Path, PathMatcher, Paths}
import java.util.Scanner
import scala.util.control.Breaks.break
class Utils{}
object Utils {
  val OPENAI_API_KEY: String = getOrDefault(System.getenv("OPENAI_API_KEY"), "demo")

  def startConversationWith(assistant: Assistant): Unit = {
    val log = LoggerFactory.getLogger(classOf[Assistant])
    try {
      val scanner = new Scanner(System.in)
      try while (true) {
        log.info("==================================================")
        log.info("User: ")
        val userQuery = scanner.nextLine
        log.info("==================================================")
        if ("exit".equalsIgnoreCase(userQuery)) break //todo: break is not supported
        val agentAnswer = assistant.answer(userQuery)
        log.info("==================================================")
        log.info("Assistant: " + agentAnswer)
      }
      finally if (scanner != null) scanner.close()
    }
  }

  def glob(glob: String): PathMatcher = FileSystems.getDefault.getPathMatcher("glob:" + glob)

  def toPath(relativePath: String): Path = try {
    val fileUrl = classOf[Utils].getClassLoader.getResource(relativePath)
    Paths.get(fileUrl.toURI)
  } catch {
    case e: URISyntaxException =>
      throw new RuntimeException(e)
  }
}
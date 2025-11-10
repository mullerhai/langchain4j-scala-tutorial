package tutorial

import dev.langchain4j.internal.Utils.getOrDefault

object ApiKeys {
  val OPENAI_API_KEY: String = getOrDefault(System.getenv("OPENAI_API_KEY"), "demo")
  val RAPID_API_KEY: String = System.getenv("RAPID_API_KEY")
}
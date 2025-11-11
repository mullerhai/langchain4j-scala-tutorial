package tutorial

import dev.langchain4j.internal.Utils.getOrDefault

object ApiKeys {
  val OPENAI_API_KEY: String = getOrDefault(System.getenv("OPENAI_API_KEY"), "61ada4b717442aac314d1caa8c8428")
  val RAPID_API_KEY: String = System.getenv("RAPID_API_KEY")
  val MODEL_NAME: String = getOrDefault(System.getenv("MODEL_NAME"), "qwen3-max")
  val BASE_URL:String =  getOrDefault(System.getenv("BASE_URL"), "https://dashscope.aliyuncs.com/compatible-mode/v1")//getOrDefault(System.getenv("BASE_URL"), "https://api.openai.com/v1")
}
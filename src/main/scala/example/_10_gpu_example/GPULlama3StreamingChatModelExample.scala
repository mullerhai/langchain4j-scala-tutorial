package example._10_gpu_example

import dev.langchain4j.data.message.{SystemMessage, UserMessage}
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.chat.response.{ChatResponse, StreamingChatResponseHandler}
import dev.langchain4j.model.gpullama3.GPULlama3StreamingChatModel

import java.nio.file.Path
import java.util.concurrent.CompletableFuture

object GPULlama3StreamingChatModelExample {
  def main(args: Array[String]): Unit = {
    // Read path to your *local* model files.
    val localLLMsPath = System.getenv("LOCAL_LLMS_PATH")
    // Check if the environment variable is set
    if (localLLMsPath == null || localLLMsPath.isEmpty) {
      System.err.println("Error: LOCAL_LLMS_PATH environment variable is not set.")
      System.err.println("Please set this environment variable to the directory containing your local model files.")
      System.exit(1)
    }
    // Change this model file name to choose any of your *local* model files.
    // Supports Mistral, Llama3, Phi-3, Qwen2.5 and Qwen3 in gguf format.
    val modelFile = "beehive-llama-3.2-1b-instruct-fp16.gguf"
    val modelPath = Path.of(localLLMsPath, modelFile)
    var prompt: String = null
    if (args.length > 0) {
      prompt = args(0)
      System.out.println("User Prompt: " + prompt)
    }
    else {
      prompt = "What is the capital of France?"
      System.out.println("Example Prompt: " + prompt)
    }
    // @formatter:off
    val request  = ChatRequest.builder
      .messages(UserMessage.from(prompt), SystemMessage.from("reply with extensive sarcasm")).build
    val model  = GPULlama3StreamingChatModel.builder.onGPU(true).build()// if false, runs on CPU though a lightweight implementation of llama3.java.modelPath(modelPath).build
    // @formatter:on
    val futureResponse = new CompletableFuture[ChatResponse]
    model.chat(request, new StreamingChatResponseHandler() {
      override def onPartialResponse(partialResponse: String): Unit = {
        System.out.print(partialResponse)
      }

      override def onCompleteResponse(completeResponse: ChatResponse): Unit = {
        futureResponse.complete(completeResponse)
        model.printLastMetrics()
      }

      override def onError(error: Throwable): Unit = {
        futureResponse.completeExceptionally(error)
      }
    })
    futureResponse.join
  }
}
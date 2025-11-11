package example._10_gpu_example

import dev.langchain4j.data.message.{SystemMessage, UserMessage}
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.model.gpullama3.GPULlama3ChatModel

import java.nio.file.Path

object GPULlama3ChatModelExample {
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
    System.out.println("Path: " + modelPath)
    // @formatter:off
    val request  = ChatRequest.builder.messages(UserMessage.from(prompt), SystemMessage.from("reply with extensive sarcasm")).build
    //Path modelPath = Paths.get("beehive-llama-3.2-1b-instruct-fp16.gguf");
    val model  = GPULlama3ChatModel.builder.modelPath(modelPath).onGPU(true).build()//if false, runs on CPU though a lightweight implementation of llama3.java.build
    // @formatter:on
    val response = model.chat(request)
    System.out.println("\n" + response.aiMessage.text)
    //Optionally print metrics
    model.printLastMetrics()
  }
}
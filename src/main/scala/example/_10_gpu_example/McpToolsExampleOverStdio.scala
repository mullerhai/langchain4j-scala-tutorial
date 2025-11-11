package example._10_gpu_example

import dev.langchain4j.mcp.McpToolProvider
import dev.langchain4j.mcp.client.transport.McpTransport
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport
import dev.langchain4j.mcp.client.{DefaultMcpClient, McpClient}
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.service.AiServices
import dev.langchain4j.service.tool.ToolProvider

import java.io.File
import java.util

object McpToolsExampleOverStdio {
  // We will let the AI read the contents of this file
  val FILE_TO_BE_READ = "src/main/resources/file.txt"

  /**
   * This example uses the `server-filesystem` MCP server to showcase how
   * to allow an LLM to interact with the local filesystem.
   * <p>
   * Running this example requires npm to be installed on your machine,
   * because it spawns the `server-filesystem` as a subprocess via npm:
   * `npm exec @modelcontextprotocol/server-filesystem@0.6.2`.
   * <p>
   * Of course, feel free to swap out the server with any other MCP server.
   * <p>
   * The communication with the server is done directly via stdin/stdout.
   * <p>
   * IMPORTANT: when executing this, make sure that the working directory is
   * equal to the root directory of the project
   * (`langchain4j-examples/mcp-example`), otherwise the program won't be able to find
   * the proper file to read. If you're working from another directory,
   * adjust the path inside the StdioMcpTransport.Builder() usage in the main method.
   */
  @throws[Exception]
  def main(args: Array[String]): Unit = {
    val model = OpenAiChatModel.builder.apiKey(System.getenv("OPENAI_API_KEY")).modelName("gpt-4o-mini").build
    val transport = new StdioMcpTransport.Builder().command(util.List.of("/usr/bin/npm", "exec", "@modelcontextprotocol/server-filesystem@0.6.2", // allowed directory for the server to interact with
      new File("src/main/resources").getAbsolutePath)).logEvents(true).build
    val mcpClient = new DefaultMcpClient.Builder().transport(transport).build
    val toolProvider = McpToolProvider.builder.mcpClients(util.List.of(mcpClient)).build
    val bot = AiServices.builder(classOf[Bot]).chatModel(model).toolProvider(toolProvider).build
    try {
      val file = new File(FILE_TO_BE_READ)
      val response = bot.chat("Read the contents of the file " + file.getAbsolutePath)
      System.out.println("RESPONSE: " + response)
    } finally mcpClient.close()
  }
}
package example._10_gpu_example

import dev.langchain4j.mcp.McpToolProvider
import dev.langchain4j.mcp.client.transport.McpTransport
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport
import dev.langchain4j.mcp.client.{DefaultMcpClient, McpClient}
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.service.AiServices
import dev.langchain4j.service.tool.ToolProvider

import java.time.Duration
import java.util

object McpToolsExampleOverHttp {
  /**
   * This example uses the `server-everything` MCP server that showcases some aspects of the MCP protocol.
   * In particular, we use its 'add' tool that adds two numbers.
   * <p>
   * Before running this example, you need to start the `everything` server in SSE mode on localhost:3001.
   * Check out https://github.com/modelcontextprotocol/servers/tree/main/src/everything
   * and run `npm install` and `node dist/sse.js`.
   * <p>
   * Of course, feel free to swap out the server with any other MCP server.
   * <p>
   * Run the example and check the logs to verify that the model used the tool.
   */
  @throws[Exception]
  def main(args: Array[String]): Unit = {
    val model = OpenAiChatModel.builder.apiKey(System.getenv("OPENAI_API_KEY")).modelName("gpt-4o-mini").logRequests(true).logResponses(true).build
    val transport = new HttpMcpTransport.Builder().sseUrl("http://localhost:3001/sse").timeout(Duration.ofSeconds(60)).logRequests(true).logResponses(true).build
    val mcpClient = new DefaultMcpClient.Builder().transport(transport).build
    val toolProvider = McpToolProvider.builder.mcpClients(util.List.of(mcpClient)).build
    val bot = AiServices.builder(classOf[Bot]).chatModel(model).toolProvider(toolProvider).build
    try {
      val response = bot.chat("What is 5+12? Use the provided tool to answer " + "and always assume that the tool is correct.")
      System.out.println(response)
    } finally mcpClient.close()
  }
}
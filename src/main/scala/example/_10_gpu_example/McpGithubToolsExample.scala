package example._10_gpu_example

import dev.langchain4j.mcp.McpToolProvider
import dev.langchain4j.mcp.client.transport.McpTransport
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport
import dev.langchain4j.mcp.client.{DefaultMcpClient, McpClient}
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.service.AiServices
import dev.langchain4j.service.tool.ToolProvider

import java.util

object McpGithubToolsExample {
  /**
   * This example uses the GitHub MCP server to showcase how
   * to use an LLM to summarize the last commits of a public GitHub repo.
   * Being a public repository (the LangChain4j repository is used as an example), you don't need any
   * authentication to access the data.
   * <p>
   * Running this example requires Docker to be installed on your machine,
   * because it spawns the GitHub MCP Server as a subprocess via Docker:
   * `docker run -i mcp/git`.
   * <p>
   * You first need to build the Docker image of the GitHub MCP Server that is available at `mcp/git`.
   * See https://github.com/modelcontextprotocol/servers/tree/main/src/git to build the image.
   * <p>
   * The communication with the GitHub MCP server is done directly via stdin/stdout.
   */
  @throws[Exception]
  def main(args: Array[String]): Unit = {
    val model = OpenAiChatModel.builder.apiKey(System.getenv("OPENAI_API_KEY")).modelName("gpt-4o-mini").logRequests(true).logResponses(true).build
    val transport = new StdioMcpTransport.Builder().command(util.List.of("/usr/local/bin/docker", "run", "-e", "GITHUB_PERSONAL_ACCESS_TOKEN", "-i", "mcp/git")).logEvents(true).build
    val mcpClient = new DefaultMcpClient.Builder().transport(transport).build
    val toolProvider = McpToolProvider.builder.mcpClients(util.List.of(mcpClient)).build
    val bot = AiServices.builder(classOf[Bot]).chatModel(model).toolProvider(toolProvider).build
    try {
      val response = bot.chat("Summarize the last 3 commits of the LangChain4j GitHub repository")
      System.out.println("RESPONSE: " + response)
    } finally mcpClient.close()
  }
}
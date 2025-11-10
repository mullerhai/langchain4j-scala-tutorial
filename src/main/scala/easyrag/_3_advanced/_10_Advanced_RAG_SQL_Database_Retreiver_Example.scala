//package easyrag._3_advanced
//
//import dev.langchain4j.experimental.rag.content.retriever.sql.SqlDatabaseContentRetriever
//import dev.langchain4j.memory.chat.MessageWindowChatMemory
//import dev.langchain4j.model.chat.ChatModel
//import dev.langchain4j.model.openai.OpenAiChatModel
//import dev.langchain4j.model.openai.OpenAiChatModelName.GPT_4_O_MINI
//import dev.langchain4j.rag.content.retriever.ContentRetriever
//import dev.langchain4j.service.AiServices
//
//
//import java.io.IOException
//import java.nio.file.Files
//import java.sql.{Connection, SQLException, Statement}
//import javax.sql.DataSource
//import easyrag.shared.{Assistant, Utils}
//object _10_Advanced_RAG_SQL_Database_Retreiver_Example {
//  /**
//   * Please refer to {@link Naive_RAG_Example} for a basic context.
//   * <p>
//   * Advanced RAG in LangChain4j is described here: https://github.com/langchain4j/langchain4j/pull/538
//   * <p>
//   * This example demonstrates how to use SQL database content retriever.
//   * <p>
//   * WARNING! Although fun and exciting, {@link SqlDatabaseContentRetriever} is dangerous to use!
//   * Do not ever use it in production! The database user must have very limited READ-ONLY permissions!
//   * Although the generated SQL is somewhat validated (to ensure that the SQL is a SELECT statement),
//   * there is no guarantee that it is harmless. Use it at your own risk!
//   * <p>
//   * In this example we will use an in-memory H2 database with 3 tables: customers, products and orders.
//   * See "resources/sql" directory for more details.
//   * <p>
//   * This example requires "langchain4j-experimental-sql" dependency.
//   */
//  def main(args: Array[String]): Unit = {
//    val assistant = createAssistant
//    // You can ask questions such as "How many customers do we have?" and "What is our top seller?".
//    Utils.startConversationWith(assistant)
//  }
//
//  private def createAssistant = {
//    val dataSource = createDataSource
//    val chatModel = OpenAiChatModel.builder.apiKey(Utils.OPENAI_API_KEY).modelName(GPT_4_O_MINI).build
//    val contentRetriever = SqlDatabaseContentRetriever.builder.dataSource(dataSource).chatModel(chatModel).build
//    AiServices.builder(classOf[Assistant]).chatModel(chatModel).contentRetriever(contentRetriever).chatMemory(MessageWindowChatMemory.withMaxMessages(10)).build
//  }
//
//  private def createDataSource = {
//    import org.h2.jdbcx.JdbcDataSource
//    val dataSource = new JdbcDataSource
//    dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
//    dataSource.setUser("sa")
//    dataSource.setPassword("sa")
//    val createTablesScript = read("sql/create_tables.sql")
//    execute(createTablesScript, dataSource)
//    val prefillTablesScript = read("sql/prefill_tables.sql")
//    execute(prefillTablesScript, dataSource)
//    dataSource
//  }
//
//  private def read(path: String) = try new String(Files.readAllBytes(Utils.toPath(path)))
//  catch {
//    case e: IOException =>
//      throw new RuntimeException(e)
//  }
//
//  private def execute(sql: String, dataSource: DataSource): Unit = {
//    try {
//      val connection = dataSource.getConnection
//      val statement = connection.createStatement
//      try for (sqlStatement <- sql.split(";")) {
//        statement.execute(sqlStatement.trim)
//      }
//      catch {
//        case e: SQLException =>
//          throw new RuntimeException(e)
//      } finally {
//        if (connection != null) connection.close()
//        if (statement != null) statement.close()
//      }
//    }
//  }
//}
package jlama

import dev.langchain4j.agent.tool.*
import dev.langchain4j.data.message.UserMessage.userMessage
import dev.langchain4j.data.message.{AiMessage, ChatMessage, ToolExecutionResultMessage, UserMessage}
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.jlama.JlamaChatModel
import dev.langchain4j.service.tool.{DefaultToolExecutor, ToolExecutor}
import dev.langchain4j.service.{AiServices, SystemMessage}

import java.util.*
import java.util.stream.Collectors.toList
import scala.collection.mutable

object JlamaAiFunctionCallingExample {
  object Payment_Data_From_AiServices {
    val mistralAiModel: ChatModel = JlamaChatModel.builder
      .modelName("tjake/Mistral-7B-Instruct-v0.3-JQ4")
      .temperature(0.0f).build() //Force same output every run.build

    trait Assistant {
      @SystemMessage(
        Array("You are a payment transaction support agent.",
        "You MUST use the payment transaction tool to search the payment transaction data.", 
        "If there is a date, convert it in a human readable format."))
      def chat(userMessage: String): String
    }

//    @main
    def main(args: Array[String]): Unit = {
      // STEP 1: User specify tools and query
      // User define all the necessary tools to be used in the chat
      // This example uses the Payment_Transaction_Tool who define two functions as our two tools
      val paymentTool = Payment_Transaction_Tool.build
      // User define the query to be used in the chat
      val userMessage = "What is the status and the payment date of transaction T1005?"
      // STEP 2: User asks the agent and AiServices call to the functions
      val agent = AiServices.builder(classOf[Payment_Data_From_AiServices.Assistant])
        .chatModel(mistralAiModel)
        .tools(paymentTool)
        .chatMemory(MessageWindowChatMemory.withMaxMessages(10))
        .build
      // STEP 3: User gets the final response from the agent
      val answer = agent.chat(userMessage)
      System.out.println(answer) //According to the payment transaction tool, the payment status of transaction T1005 is Pending and the payment date is 2021-10-08.
    }
  }

  object Payment_Transaction_Tool {
    def build = new JlamaAiFunctionCallingExample.Payment_Transaction_Tool
    // Tool to be executed by mistral model to get payment status
    @Tool(Array("Get payment status of a transaction")) // function description 
    def retrievePaymentStatus(@P("Transaction id to search payment data")  transactionId: String): String =  { return getPaymentDataField(transactionId, "payment_status")}
  // Tool to be executed by mistral model to get payment date
    @Tool(Array("Get payment date of a transaction")) // function description 
    def retrievePaymentDate(@P("Transaction id to search payment data")  transactionId: String): String =  { return getPaymentDataField(transactionId, "payment_date")}


    private def getPaymentData = {
      val data = new mutable.HashMap[String,Seq[String]]
       data.put("transaction_id", Seq("T1001", "T1002", "T1003", "T1004", "T1005"))
       data.put("customer_id", Seq("C001", "C002", "C003", "C002", "C001"))
       data.put("payment_amount", Seq("125.50", "89.99", "120.00", "54.30", "210.20"))
       data.put("payment_date", Seq("2021-10-05", "2021-10-06", "2021-10-07", "2021-10-05", "2021-10-08"))
       data.put("payment_status", Seq("Paid", "Unpaid", "Paid", "Paid", "Pending"))
       data
    }
    private def getPaymentDataField(transactionId: String, data: String) = {
      val transactionIds = getPaymentData.get("transaction_id")
      val paymentData = getPaymentData.get(data)
      val index = transactionIds.toList.indexOf(transactionId)
      if (index != -1) then paymentData.get(index) 
      else "Transaction ID not found"
    }

}

class Payment_Transaction_Tool {}
}
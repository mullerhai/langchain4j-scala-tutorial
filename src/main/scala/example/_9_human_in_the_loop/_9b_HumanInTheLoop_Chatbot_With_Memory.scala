package example._9_human_in_the_loop

import dev.langchain4j.agentic.scope.AgenticScope
import dev.langchain4j.agentic.{AgenticServices, UntypedAgent}
import dev.langchain4j.agentic.workflow.HumanInTheLoop
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.service.AiServices

import java.util
import java.util.Scanner
import utils.ChatModelProvider
import utils.{CustomLogging, LogLevels}

object _9b_HumanInTheLoop_Chatbot_With_Memorys {
  /**
   * This example demonstrates a back-and-forth loop with human-in-the-loop interaction,
   * until an end-goal is reached (exit condition), after which the rest of the workflow
   * can continue.
   * The loop continues until the human confirms availability, which is verified by an AiService.
   * When no slot is found, the loop ends after 5 iterations.
   */
  private val CHAT_MODEL = ChatModelProvider.createChatModel
  try CustomLogging.setLevel(LogLevels.PRETTY, 300) // control how much you see from the model calls

  def main(args: Array[String]): Unit = {
    // 1. Define sub-agent
    val proposer = AgenticServices
      .agentBuilder(classOf[MeetingProposer])
      .chatModel(CHAT_MODEL
      ).chatMemoryProvider((memoryId: AnyRef) => MessageWindowChatMemory.withMaxMessages(15)).build() // so the agent remembers what he proposed already.outputKey("proposal").build
    // 2. Add an AiService to judge if a decision has been reached (this can be a tiny local model because the assignment is so simple)
    val decisionService = AiServices.create(classOf[DecisionsReachedService], CHAT_MODEL)
    // 2. Define Human-in-the-loop agent
    val humanInTheLoop = AgenticServices
      .humanInTheLoopBuilder.description("agent that asks input from the user")
      .outputKey("candidateAnswer") // matches one of the proposer's input variable names
     .inputKey("proposal")// must match the output of the proposer agent
     .requestWriter((request) => {
       println(request)
       println("> ")
  }).responseReader(() => new Scanner(System.in).nextLine)
      .async(true).build() // no need to block the entire program while waiting for user input.build
  // 3. construct the loop
  // Here we only want the exit condition to be checked once per loop, not after every agent invocation,
  // so we bundle both agents in a sequence and give it as one agent to the loop
  val agentSequence: UntypedAgent = AgenticServices.sequenceBuilder
    .subAgents(proposer, humanInTheLoop)
    .output((agenticScope: AgenticScope) => util.Map.of("proposal", agenticScope.readState("proposal"), "candidateAnswer", agenticScope.readState("candidateAnswer"))).outputKey("proposalAndAnswer").build
  val schedulingLoop: UntypedAgent = AgenticServices.loopBuilder.subAgents(agentSequence).exitCondition((scope: AgenticScope) => {
    System.out.println("--- checking exit condition ---")
    val response = scope.readState("candidateAnswer").asInstanceOf[String]
    val proposal = scope.readState("proposal").asInstanceOf[String]
    response != null && decisionService.isDecisionReached(proposal, response)
  }).outputKey("proposalAndAnswer").maxIterations(5).build
  // 4. Run the scheduling loop
  val input: util.Map[String, AnyRef] = util.Map.of("meetingTopic", "on-site visit", "candidateAnswer", "hi", // this variable needs to be present in the AgenticScope in advance because the MeetingProposer takes it as input
    "memoryId", "user-1234") // if we don't put a memoryId, the proposer agent will not remember what he proposed already
  val lastProposalAndAnswer: AnyRef = schedulingLoop.invoke(input)
  System.out.println("=== Result: last proposalAndAnswer ===")
  System.out.println(lastProposalAndAnswer)
}

}
package example._9_human_in_the_loop

import dev.langchain4j.agentic.{AgenticServices, UntypedAgent}
import dev.langchain4j.agentic.workflow.HumanInTheLoop
import dev.langchain4j.model.chat.ChatModel
import domain.CvReview

import java.util
import java.util.Scanner
import utils.ChatModelProvider
import utils.{CustomLogging, LogLevels}

object _9a_HumanInTheLoop_Simple_Validators {
  try CustomLogging.setLevel(LogLevels.PRETTY, 300)
  private val CHAT_MODEL = ChatModelProvider.createChatModel

  def main(args: Array[String]): Unit = {
    // 3. Create involved agents
    val decisionProposer = AgenticServices.agentBuilder(classOf[HiringDecisionProposer]).chatModel(CHAT_MODEL).outputKey("modelDecision").build
    // 2. Define human in the loop for validation
    val humanValidator = AgenticServices.humanInTheLoopBuilder.description("validates the model's proposed hiring decision").inputKey("modelDecision")
      .outputKey("finalDecision")// checked by human.requestWriter((request) => {
      .requestWriter(request => {
    System.out.println("AI hiring assistant suggests: " + request)
    System.out.println("Please confirm the final decision.")
    System.out.println("Options: Invite on-site (I), Reject (R), Hold (H)")
    System.out.print("> ") // we  needs input validation and error handling in real life systems
  }

  ).responseReader(() => new Scanner(System.in).nextLine).build
  // 3. Chain agents into a workflow
  val hiringDecisionWorkflow: UntypedAgent = AgenticServices
    .sequenceBuilder.subAgents(decisionProposer, humanValidator)
    .outputKey("finalDecision").build
  // 4. Prepare input arguments
  val input: util.Map[String, AnyRef] = util.Map.of("cvReview", new CvReview(0.85,
    """
                                Strong technical skills except for required React experience.
                                Seems a fast and independent learner though. Good cultural fit.
                                Potential issue with work permit that seems solvable.
                                Salary expectation slightly over planned budget.
                                Decision to proceed with onsite-interview.
                                """))
  // 5. Run workflow
  val finalDecision: String = hiringDecisionWorkflow.invoke(input).asInstanceOf[String]
  System.out.println("\n=== FINAL DECISION BY HUMAN ===")
  System.out.println("(Invite on-site (I), Reject (R), Hold (H))\n")
  System.out.println(finalDecision)
  // Note: human-in-the-loop and human validation can typically take long for the user to respond.
  // In this case, async agents are recommended so they don't block the rest of the workflow
  // that can potentially be executed before the user answer comes.
}


}
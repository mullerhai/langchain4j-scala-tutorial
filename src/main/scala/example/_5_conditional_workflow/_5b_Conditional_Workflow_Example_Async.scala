package example._5_conditional_workflow

import example._4_parallel_workflow.ManagerCvReviewer
import dev.langchain4j.agentic.scope.AgenticScope
import dev.langchain4j.agentic.{AgenticServices, UntypedAgent}
import dev.langchain4j.model.chat.ChatModel
import domain.CvReview

import java.io.IOException
import java.util
import utils.{ChatModelProvider, StringLoader}
import utils.{CustomLogging, LogLevels}

object _5b_Conditional_Workflow_Example_Asyncs {
  /**
   * This example demonstrates multiple fulfilled conditions and async agents that will
   * allow consecutive agents to be called in parallel for faster execution.
   * In this example:
   * - condition 1: if the HrReview is good, the CV is passed to the manager for review,
   * - condition 2: if the HrReview indicates missing information, the candidate is contacted for more info.
   */
  private val CHAT_MODEL = ChatModelProvider.createChatModel
  try CustomLogging.setLevel(LogLevels.PRETTY, 150)
  
  @throws[IOException]
  def main(args: Array[String]): Unit = {
    // 1. Create all async agents
    val managerCvReviewer = AgenticServices.agentBuilder(classOf[ManagerCvReviewer]).chatModel(CHAT_MODEL).async(true).outputKey("managerReview").build() // async agent.outputKey("managerReview").build
    val emailAssistant = AgenticServices.agentBuilder(classOf[EmailAssistant]).chatModel(CHAT_MODEL).async(true).tools(new OrganizingTools).outputKey("sentEmailId").build
    val infoRequester = AgenticServices.agentBuilder(classOf[InfoRequester]).chatModel(CHAT_MODEL).async(true).tools(new OrganizingTools).outputKey("sentEmailId").build
    // 2. Build async conditional workflow
    val candidateResponder = AgenticServices.conditionalBuilder
      .subAgents((scope: AgenticScope) => {
      val hrReview = scope.readState("cvReview").asInstanceOf[CvReview]
      hrReview.score >= 0.8 // if HR passes, send to manager for review
    }, managerCvReviewer)
      .subAgents((scope: AgenticScope) => {
      val hrReview = scope.readState("cvReview").asInstanceOf[CvReview]
      hrReview.score < 0.8 // if HR does not pass, send rejection email
    }, emailAssistant)
      .subAgents((scope: AgenticScope) => {
      val hrReview = scope.readState("cvReview").asInstanceOf[CvReview]
      hrReview.feedback.toLowerCase.contains("missing information:")
    }, infoRequester) // if needed, request more info from candidate.output((agenticScope: AgenticScope) => (agenticScope.readState("managerReview", new CvReview(0, "no manager review needed"))).toString + "\n" + agenticScope.readState("sentEmailId", 0))// final output is the manager review (if any).build
      .output(agenticScope =>{
        (agenticScope.readState("managerReview", new CvReview(0, "no manager review needed"))).toString() +
          "\n" + agenticScope.readState("sentEmailId", 0)
      }).build()
    // 3. Input arguments
    val candidateCv = StringLoader.loadFromResource("/documents/tailored_cv.txt")
    val candidateContact = StringLoader.loadFromResource("/documents/candidate_contact.txt")
    val jobDescription = StringLoader.loadFromResource("/documents/job_description_backend.txt")
    val hrReview = new CvReview(0.85,
      """
                        Solid candidate, salary expectations in scope and able to start within desired timeframe.
                        Missing information: details about work authorization status in Belgium.
                        """)
    val arguments = util.Map.of("candidateCv", candidateCv.asInstanceOf[Object], "candidateContact", candidateContact.asInstanceOf[Object], "jobDescription", jobDescription.asInstanceOf[Object], "cvReview", hrReview.asInstanceOf[Object])
    // 4. Run the conditional async workflow
    candidateResponder.invoke(arguments)
    System.out.println("=== Finished execution of async conditional workflow ===")
  }


}
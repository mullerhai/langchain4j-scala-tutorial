package example._9_human_in_the_loop

import dev.langchain4j.agentic.Agent
import dev.langchain4j.service.{MemoryId, SystemMessage, UserMessage, V}
import domain.CvReview

trait HiringDecisionProposer {
  @Agent("Summarizes hiring decision for final validation")
  @SystemMessage(Array(
    """
        You summarize the hiring reasons in 3 lines max for a given review,
        for a human to make the final decision whether to proceed or not.
        """))
  @UserMessage(Array(
    """
        Feedback from all parties involved in the hiring process: {{cvReview}}
        """)) 
  def propose(@V("cvReview") cvReview: CvReview): String
}
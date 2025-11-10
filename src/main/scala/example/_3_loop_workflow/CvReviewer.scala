package example._3_loop_workflow

import dev.langchain4j.agentic.Agent
import dev.langchain4j.service.{SystemMessage, UserMessage, V}
import domain.CvReview

trait CvReviewer {
  @Agent("Reviews a CV according to specific instructions, gives feedback and a score. Factor in how well the CV is tailored to the job")
  @SystemMessage(Array(
    """
            You are the hiring manager for this job:
            {{jobDescription}}
            Your review applicant CVs and need to decide who of the many applicants you invite for an on-site interview.
            You give each CV a score and feedback (both the good and the bad things).
            You can ignore things like missing address and placeholders.
            """))
  @UserMessage(Array(
    """
            Review this CV: {{cv}}
            """))
  def reviewCv(@V("cv") cv: String, @V("jobDescription") jobDescription: String): CvReview
}
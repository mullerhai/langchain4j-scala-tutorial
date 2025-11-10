package example._4_parallel_workflow

import dev.langchain4j.agentic.Agent
import dev.langchain4j.service.{SystemMessage, UserMessage, V}
import domain.CvReview

trait HrCvReviewer {
  @Agent(name = "hrReviewer", description = "Reviews a CV to check if candidate fits HR requirements, gives feedback and a score")
  @SystemMessage(Array(
    """
            You are working for HR and review CVs to fill a position with these requirements:
            {{hrRequirements}}
            You give each CV a score and feedback (both the good and the bad things).
            You can ignore things like missing address and placeholders.
            
            IMPORTANT: Return your response as valid JSON only, new lines as \\n, without any markdown formatting or code blocks.
            """))
  @UserMessage(Array(
    """
            Review this CV: {{candidateCv}} with accompanying phone interview notes: {{phoneInterviewNotes}}
            """)) def reviewCv(@V("candidateCv") cv: String, @V("phoneInterviewNotes") phoneInterviewNotes: String, @V("hrRequirements") hrRequirements: String): CvReview
}
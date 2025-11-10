package example._8_non_ai_agents

import dev.langchain4j.agentic.Agent
import dev.langchain4j.service.V
import domain.CvReview

/**
 * Non-AI agent that aggregates multiple CV reviews into a combined review.
 * This demonstrates how plain Java operators can be used as first-class agents
 * in agentic workflows, making them interchangeable with AI-powered agents.
 */
class ScoreAggregator {
  @Agent(description = "Aggregates HR/Manager/Team reviews into a combined review", outputKey = "combinedCvReview")
  def aggregate(@V("hrReview") hr: CvReview, @V("managerReview") mgr: CvReview, @V("teamMemberReview") team: CvReview): CvReview = {
    System.out.println("ScoreAggregator called with hrReview: " + hr + ", managerReview: " + mgr + ", teamMemberReview: " + team)
    val avgScore = (hr.score + mgr.score + team.score) / 3.0
    val combinedFeedback = String.join("\n\n", "HR Review: " + hr.feedback, "Manager Review: " + mgr.feedback, "Team Member Review: " + team.feedback)
    new CvReview(avgScore, combinedFeedback)
  }
}
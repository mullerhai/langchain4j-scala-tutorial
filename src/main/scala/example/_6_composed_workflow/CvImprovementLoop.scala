package example._6_composed_workflow

import dev.langchain4j.agentic.Agent
import dev.langchain4j.service.V

trait CvImprovementLoop {
  @Agent("Improves CV through iterative tailoring and review until passing score") 
  def improveCv(@V("cv") cv: String, @V("jobDescription") jobDescription: String): String
}
package example._2_sequential_workflow

import dev.langchain4j.agentic.Agent
import dev.langchain4j.agentic.scope.ResultWithAgenticScope
import dev.langchain4j.service.V

import java.util

trait SequenceCvGenerator {
  @Agent("Generates a CV based on user-provided information and tailored to instructions, don't make it too long, avoid empty lines") 
  def generateTailoredCv(@V("lifeStory") lifeStory: String, @V("instructions") instructions: String): ResultWithAgenticScope[util.Map[String, String]]
}
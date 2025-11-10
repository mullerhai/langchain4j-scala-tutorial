package domain

import dev.langchain4j.model.output.structured.Description

class Cv {
  @Description(Array("skills of the cadidate, comma-concatenated")) 
  private val skills = null
  @Description(Array("professional experience of the candidate")) 
  private val professionalExperience = null
  @Description(Array("studies of the candidate")) 
  private val studies = null

  override def toString: String = "CV:\n" + "skills = \"" + skills + "\"\n" + "professionalExperience = \"" + professionalExperience + "\"\n" + "studies = \"" + studies + "\"\n"
}
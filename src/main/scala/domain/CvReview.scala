package domain

import dev.langchain4j.model.output.structured.Description

class CvReview {
  // no args constructor needed for deserialization, bcs other constructor is present!
  @Description(Array("Score from 0 to 1 how likely you would invite this candidate to an interview")) 
  var score = .0
  @Description(Array("Feedback on the CV, what is good, what needs improvement, what skills are missing, what red flags, ...")) 
  var feedback: String = null

  def this(score: Double, feedback: String)= {
    this()
    this.score = score
    this.feedback = feedback
  }

  override def toString: String = "\nCvReview: " + " - score = " + score + "\n- feedback = \"" + feedback + "\"\n"
}
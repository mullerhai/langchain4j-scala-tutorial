package mongodb_movieagent

import com.opencsv.bean.CsvBindByPosition

case class Movie (
  @CsvBindByPosition(position = 0) var posterLink: String,
  @CsvBindByPosition(position = 1) var title: String,
  @CsvBindByPosition(position = 2) var year: String,
  @CsvBindByPosition(position = 3) var certificate: String,
  @CsvBindByPosition(position = 4) var runtime: String,
  @CsvBindByPosition(position = 5) var genre: String,
  @CsvBindByPosition(position = 6) var imdbRating: String,
  @CsvBindByPosition(position = 7) var overview: String,
  @CsvBindByPosition(position = 8) var metaScore: String,
  @CsvBindByPosition(position = 9) var director: String,
  @CsvBindByPosition(position = 10) var star1: String,
  @CsvBindByPosition(position = 11) var star2: String,
  @CsvBindByPosition(position = 12) var star3: String,
  @CsvBindByPosition(position = 13) var star4: String,
  @CsvBindByPosition(position = 14) var numberOfVotes: String,
  @CsvBindByPosition(position = 15) var gross: String)

//  def getPosterLink: String = posterLink
//
//  def setPosterLink(posterLink: String): Unit = {
//    this.posterLink = posterLink
//  }
//
//  def getTitle: String = title
//
//  def setTitle(title: String): Unit = {
//    this.title = title
//  }
//
//  def getYear: String = year
//
//  def setYear(year: String): Unit = {
//    this.year = year
//  }
//
//  def getCertificate: String = certificate
//
//  def setCertificate(certificate: String): Unit = {
//    this.certificate = certificate
//  }
//
//  def getRuntime: String = runtime
//
//  def setRuntime(runtime: String): Unit = {
//    this.runtime = runtime
//  }
//
//  def getGenre: String = genre
//
//  def setGenre(genre: String): Unit = {
//    this.genre = genre
//  }
//
//  def getImdbRating: String = imdbRating
//
//  def setImdbRating(imdbRating: String): Unit = {
//    this.imdbRating = imdbRating
//  }
//
//  def getOverview: String = overview
//
//  def setOverview(overview: String): Unit = {
//    this.overview = overview
//  }
//
//  def getMetaScore: String = metaScore
//
//  def setMetaScore(metaScore: String): Unit = {
//    this.metaScore = metaScore
//  }
//
//  def getDirector: String = director
//
//  def setDirector(director: String): Unit = {
//    this.director = director
//  }
//
//  def getStar1: String = star1
//
//  def setStar1(star1: String): Unit = {
//    this.star1 = star1
//  }
//
//  def getStar2: String = star2
//
//  def setStar2(star2: String): Unit = {
//    this.star2 = star2
//  }
//
//  def getStar3: String = star3
//
//  def setStar3(star3: String): Unit = {
//    this.star3 = star3
//  }
//
//  def getStar4: String = star4
//
//  def setStar4(star4: String): Unit = {
//    this.star4 = star4
//  }
//
//  def getNumberOfVotes: String = numberOfVotes
//
//  def setNumberOfVotes(numberOfVotes: String): Unit = {
//    this.numberOfVotes = numberOfVotes
//  }
//
//  def getGross: String = gross
//
//  def setGross(gross: String): Unit = {
//    this.gross = gross
//  }
//
//  override def toString: String = String.format("%s (%s) - %s - Rating: %s\nGenre: %s | Director: %s\n%s", title, year, genre, imdbRating, genre, director, overview)
//}
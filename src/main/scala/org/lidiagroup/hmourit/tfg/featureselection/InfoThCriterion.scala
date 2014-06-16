package org.lidiagroup.hmourit.tfg.featureselection

/**
 * Trait which declares needed methods to define a criterion for Feature Selection
 */
trait InfoThCriterion extends Serializable with Ordered[InfoThCriterion] {

  var relevance: Double = 0.0

  /**
   * Used internally to initialize relevance value
   */
  protected def setRelevance(relevance: Double): InfoThCriterion = {
    this.relevance = relevance
    this
  }

  /**
   * Implements ordered trait
   */
  override def compare(that: InfoThCriterion): Int = {
    this.score.compare(that.score)
  }

  /**
   * Initializes a new criterion value from relevance value
   */
  def init(relevance: Double): InfoThCriterion

  /**
   * Updates criterion value with mutual information of the feature with the last selected one and
   * conditional mutual information.
   */
  def update(mi: Double, cmi: Double): InfoThCriterion

  /**
   * Returns the value of the criterion for in a precise moment.
   */
  def score: Double

}

/**
 * Declares the method needed to define a criterion which can be bounden for optimization.
 */
trait Bound extends Serializable { self: InfoThCriterion =>

  /**
   * Returns the maximum value the criterion can reach given the relevance.
   */
  def bound: Double
}

/**
 * Joint Mutual Information criterion
 */
class Jmi extends InfoThCriterion with Bound {

  var redundance: Double = 0.0
  var conditionalRedundance: Double = 0.0
  var selectedSize: Int = 0

  override def bound = 2 * relevance

  override def score = {
    if (selectedSize != 0) {
      relevance - redundance / selectedSize + conditionalRedundance / selectedSize
    } else {
      relevance
    }
  }

  override def init(relevance: Double): InfoThCriterion = {
    this.setRelevance(relevance)
  }

  override def update(mi: Double, cmi: Double): InfoThCriterion = {
    redundance += mi
    conditionalRedundance += cmi
    selectedSize += 1
    this
  }

  override def toString: String = "JMI"
}

/**
 * Minimum-Redundancy Maximum-Relevance criterion
 */
class Mrmr extends InfoThCriterion with Bound {

  var redundance: Double = 0.0
  var selectedSize: Int = 0

  override def bound = 2 * relevance

  override def score = {
    if (selectedSize != 0) {
      relevance - redundance / selectedSize
    } else {
      relevance
    }
  }

  override def init(relevance: Double): InfoThCriterion = {
    this.setRelevance(relevance)
  }

  override def update(mi: Double, cmi: Double): InfoThCriterion = {
    redundance += mi
    selectedSize += 1
    this
  }

  override def toString: String = "MRMR"

}

/**
 * Conditional Mutual Information Maximization
 */
class Cmim extends InfoThCriterion {

  var modifier: Double = 0.0

  override def score: Double = {
    relevance - modifier
  }

  override def update(mi: Double, cmi: Double): InfoThCriterion = {
    modifier = math.max(modifier, mi - cmi)
    this
  }

  override def init(relevance: Double): InfoThCriterion = {
    this.setRelevance(relevance)
  }

  override def toString: String = "CMIM"

}


/**
 * Informative Fragments
 */
class If extends Cmim {

  override def toString: String = "IF"

}


/**
 * Interaction Capping
 */
class Icap extends InfoThCriterion {

  var modifier: Double = 0.0

  override def score: Double = {
    relevance - modifier
  }

  override def update(mi: Double, cmi: Double): InfoThCriterion = {
    modifier += math.max(0.0, mi - cmi)
    this
  }

  override def init(relevance: Double): InfoThCriterion = {
    this.setRelevance(relevance)
  }

  override def toString: String = "ICAP"

}
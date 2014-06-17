package org.lidiagroup.hmourit.tfg.featureselection

trait CriterionFactory extends Serializable {

	def getCriterion(criterion: String): Criterion

}
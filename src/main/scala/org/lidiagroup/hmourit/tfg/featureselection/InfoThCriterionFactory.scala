package org.lidiagroup.hmourit.tfg.featureselection

class InfoThCriterionFactory(val criterion: String) extends Serializable {
	
	val JMI  = "jmi"
  val MRMR = "mrmr"
  val ICAP = "icap"
  val CMIM = "cmim"
  val IF   = "if"

	def getCriterion: InfoThCriterion = {
		criterion match {
      case JMI  => new Jmi
      case MRMR => new Mrmr
      case ICAP => new Icap
      case CMIM => new Cmim
      case IF   => new If
      case _    => throw new IllegalArgumentException("criterion unknown")
    }
	}

}
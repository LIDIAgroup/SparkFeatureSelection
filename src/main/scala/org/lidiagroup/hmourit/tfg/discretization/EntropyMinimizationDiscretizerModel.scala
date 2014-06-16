package org.lidiagroup.hmourit.tfg.discretization

import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.linalg.Vectors

/**
 * This class provides the methods to discretize data with the given thresholds.
 * @param thresholds Thresholds used to discretize.
 */
class EntropyMinimizationDiscretizerModel (val thresholds: Map[Int, Seq[Double]])
  extends DiscretizerModel[LabeledPoint] with Serializable {

  /**
   * Discretizes values for the given data set using the model trained.
   *
   * @param data Data point to discretize.
   * @return Data point with values discretized
   */
  override def discretize(data: LabeledPoint): LabeledPoint = {
    val newValues = data.features.toArray.zipWithIndex.map({ case (value, i) =>
      if (this.thresholds.keySet contains i) {
        assignDiscreteValue(value, thresholds(i))
      } else {
        value
      }
    })
    LabeledPoint(data.label, Vectors.dense(newValues))
  }

  /**
   * Discretizes values for the given data set using the model trained.
   *
   * @param data RDD representing data points to discretize.
   * @return RDD with values discretized
   */
  override def discretize(data: RDD[LabeledPoint]): RDD[LabeledPoint] = {
    val bc_thresholds = data.context.broadcast(this.thresholds)

    // applies thresholds to discretize every continuous feature
    data.map({ case LabeledPoint(label, values) =>
      val newValues = values.toArray.zipWithIndex.map({ case (value, i) =>
        if (bc_thresholds.value.keySet contains i) {
          assignDiscreteValue(value, bc_thresholds.value(i))
        } else {
          value
        }
      })
      LabeledPoint(label, Vectors.dense(newValues))
    })
  }


  /**
   * Discretizes a value with a set of intervals.
   *
   * @param value The value to be discretized
   * @param thresholds Thresholds used to asign a discrete value
   */
  private def assignDiscreteValue(value: Double, thresholds: Seq[Double]) = {
    var aux = thresholds.zipWithIndex
    while (value > aux.head._1) aux = aux.tail
    aux.head._2
  }

}

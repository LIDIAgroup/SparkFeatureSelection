package org.lidiagroup.hmourit.tfg.featureselection

import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.rdd._

class InfoThFeatureSelectionModel (val features: Array[Int])
    extends FeatureSelectionModel[LabeledPoint] with Serializable {

  /**
   * Applies trained model to select the most relevant features of data.
   *
   * @param data Data point to be reduced.
   * @return Data point with its dimensionality reduced.
   */
  override def select(data: LabeledPoint): LabeledPoint = {
    data match {
      case LabeledPoint(label, values) =>
        val array = values.toArray
        LabeledPoint(label, Vectors.dense(features.map(array(_))))
    }
  }

  /**
   * Applies trained model to select the most relevant features of each element of the RDD.
   *
   * @param data RDD with elements to reduce dimensionality.
   * @return RDD with all elements reduced.
   */
  def select(data: RDD[LabeledPoint]): RDD[LabeledPoint] = {
    data.map({ case LabeledPoint(label, values) =>
      val array = values.toArray
      LabeledPoint(label, Vectors.dense(features.map(array(_))))
    })
  }
}

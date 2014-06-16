package org.lidiagroup.hmourit.tfg.featureselection

import org.apache.spark.rdd._

/**
 * FeatureSelectionModel provides an interface with basic methods for future Feature Selection
 * implementations.
 */
trait FeatureSelectionModel[T] extends Serializable{

  /**
   * Applies trained model to select the most relevant features of each element of the RDD.
   *
   * @param data RDD with elements to reduce dimensionality.
   * @return RDD with all elements reduced.
   */
  def select(data: RDD[T]): RDD[T]

  /**
   * Applies trained model to select the most relevant features of data.
   *
   * @param data Data point to be reduced.
   * @return Data point with its dimensionality reduced.
   */
  def select(data: T): T
}

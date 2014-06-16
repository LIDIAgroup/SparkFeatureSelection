package org.lidiagroup.hmourit.tfg.featureselection

import org.apache.spark.rdd.RDD
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.SparkContext._
import org.lidiagroup.hmourit.tfg.featureselection.{InfoTheory => IT}

class InfoThFeatureSelection private (
    val criterionFactory: InfoThCriterionFactory,
    var poolSize:  Int = 30)
  extends Serializable {

  private type Pool = RDD[(Int, InfoThCriterion)]
  private case class F(feat: Int, crit: Double)

  def setPoolSize(poolSize: Int) = {
    this.poolSize = poolSize
    this
  }

  private def selectFeaturesWithoutPool(
      data: RDD[Array[Double]],
      nToSelect: Int,
      nFeatures: Int,
      label: Int,
      nElements: Long)
    : Seq[F] = {

    // calculate relevance
    var pool = IT.miAndCmi(data, 1 to nFeatures, label, None, nElements)
      .map({ case (k, (mi, _)) => (k, criterionFactory.getCriterion.init(mi)) })
      .toArray

    // get maximum and select it
    var max = pool.maxBy(_._2.score)
    var selected = Seq(F(max._1, max._2.score))
    var toSelect = pool.map(_._1) diff Seq(max._1)

    while (selected.size < nToSelect) {
      // update pool
      val newMiAndCmi = IT.miAndCmi(data, toSelect, selected.head.feat, Some(label), nElements)
      pool = pool.flatMap({ case (k, crit) =>
        newMiAndCmi.get(k) match {
          case Some((mi, cmi)) => Seq((k, crit.update(mi, cmi)))
          case None => Seq.empty[(Int, InfoThCriterion)]
        }
      })

      // look for maximum
      max = pool.maxBy(_._2.score)

      // select feature
      selected = F(max._1, max._2.score) +: selected
      toSelect = toSelect diff Seq(max._1)

    }

    selected.reverse
  }

  private def selectFeaturesWithPool(
      data: RDD[Array[Double]],
      nToSelect: Int,
      nFeatures: Int,
      label: Int,
      nElements: Long)
    : Seq[F] = {

    // calculate relevance
    var rels = IT.miAndCmi(data, 1 to nFeatures, label, None, nElements)
        .toArray
        .map({ case (k, (mi, _)) => (k, mi) })
        .sortBy(-_._2)

    // extract pool
    val initialPoolSize = math.min(math.max(poolSize, nToSelect), rels.length)
    var pool = rels.take(initialPoolSize).map({ case (k, mi) =>
      (k, criterionFactory.getCriterion.init(mi))
    })
    var min = pool.last._2.asInstanceOf[InfoThCriterion with Bound]
    var toSelect = pool.map(_._1)
    rels = rels.drop(initialPoolSize)

    // select feature with top relevancy
    var max = pool.head
    var selected = Seq(F(max._1, max._2.score))
    toSelect = toSelect diff Seq(max._1)

    while (selected.size < nToSelect) {

      // update pool
      val newMiAndCmi = IT.miAndCmi(data, toSelect, selected.head.feat, Some(label), nElements)
      pool = pool.flatMap({ case (k, crit) =>
        newMiAndCmi.get(k) match {
          case Some((mi, cmi)) => Seq((k, crit.update(mi, cmi)))
          case None => Seq.empty[(Int, InfoThCriterion)]
        }
      })

      // look for maximum
      max = pool.maxBy(_._2.score)

      // increase pool if necessary
      while (max._2.score < min.bound && toSelect.size + selected.size < nFeatures) {

        // increase pool
        val realPoolSize = math.min(poolSize, rels.length)
        pool ++= rels.take(realPoolSize).map({ case (k, mi) => (k, criterion.init(mi)) })
        rels = rels.drop(realPoolSize)
        min = pool.last._2.asInstanceOf[InfoThCriterion with Bound]

        // do missed calculations
        for (i <- (pool.length - realPoolSize) until pool.length) {
          val missed_calc = IT.miAndCmi(data, selected.map(_.feat), i, Some(label), nElements)
          missed_calc.foreach({ case (_, (mi, cmi)) => pool(i)._2.update(mi, cmi)})
          toSelect = pool(i)._1 +: toSelect
        }

        // look for maximum
        max = pool.maxBy(_._2.score)

      }
      // select feature
      selected = F(max._1, max._2.score) +: selected
      toSelect = toSelect diff Seq(max._1)

    }

    selected.reverse

  }

  def run(data: RDD[LabeledPoint], nToSelect: Int): InfoThFeatureSelectionModel = {
    val nFeatures = data.first.features.size

    if (nToSelect > nFeatures) {
      throw new IllegalArgumentException("data doesn't have so many features")
    }

    val array = data.map({ case LabeledPoint(label, values) => (label +: values.toArray) }).cache

    var selected = Seq.empty[F]
    criterion match {
      case _: InfoThCriterion with Bound if poolSize != 0 =>
        selected = selectFeaturesWithPool(array, nToSelect, nFeatures, 0, data.count)
      case _: InfoThCriterion =>
        selected = selectFeaturesWithoutPool(array, nToSelect, nFeatures, 0, data.count)
      case _ =>
    }

    new InfoThFeatureSelectionModel(selected.map({ case F(feat, rel) => feat - 1 }).toArray)
  }
}

object InfoThFeatureSelection {

  def train(criterionFactory: InfoThCriterionFactory,
      data: RDD[LabeledPoint],
      nToSelect: Int,
      poolSize: Int = 30) = {
    new InfoThFeatureSelection(criterionFactory, poolSize).run(data, nToSelect)
  }
}

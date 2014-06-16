package org.lidiagroup.hmourit.tfg.generators

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.mllib.regression.LabeledPoint
import scala.util.Random
import org.apache.spark.mllib.linalg.Vectors

/**
 * Generates discrete data
 */
class DiscreteDataGenerator extends Serializable {

  def generateDataWithDT(
      sc: SparkContext,
      nRelevantFeatures: Int,
      noiseOnRelevant: Double,
      redundantNoises: Seq[Double],
      nRandomFeatures: Int,
      maxBins: Int,
      maxDepth: Int,
      nLabels: Int = 2,
      nDataPoints: Long,
      seed: Long = System.currentTimeMillis()) = {

    val generator =
      new RandomDecisionTreeGenerator(nRelevantFeatures, maxBins, maxDepth, nLabels, seed)
    val tree = generator.generateTree
    val bcTree = sc.broadcast(tree)

    var rdd = sc.parallelize(Seq.empty[LabeledPoint], sc.defaultParallelism)
    val dataPointsPerPartition = nDataPoints/sc.defaultParallelism

    val rnd = new Random(seed)
    val innerSeed = rnd.nextLong()

    // Generate relevant features
    rdd = rdd.mapPartitions({ case _ =>
      val random = new Random(innerSeed)
      val data = for (_ <- 1L to dataPointsPerPartition) yield {
        val features = Array.fill(nRelevantFeatures)(random.nextInt(maxBins).toDouble)
        var label = bcTree.value.decide(features)
        if (random.nextDouble() < noiseOnRelevant) {
          val oldLabel = label
          while (label == oldLabel) {
            label = random.nextInt(nLabels)
          }
        }
        LabeledPoint(label, Vectors.dense(features))
      }

      data.iterator
    })

    // Generate redundant features
    for (noise <- redundantNoises) {
      val feat2Replicate = rnd.nextInt(nRelevantFeatures)
      rdd = rdd.map({ case LabeledPoint(label, features) =>
        val random = new Random(innerSeed ^ features.hashCode)
        val oldValue = features.toArray(feat2Replicate)
        var newValue = oldValue
        if (random.nextDouble < noise) {
          while (oldValue == newValue) {
            newValue = random.nextInt(maxBins)
          }
        }
        LabeledPoint(label, Vectors.dense(features.toArray :+ newValue))
      })
    }


    // Generate random features
    for (_ <- 1 to nRandomFeatures) {
      rdd = rdd.map({ case LabeledPoint(label, features) =>
        val random = new Random(innerSeed ^ features.hashCode)
        LabeledPoint(label, Vectors.dense(features.toArray :+ random.nextInt(maxBins).toDouble))
      })
    }

    rdd

  }

}

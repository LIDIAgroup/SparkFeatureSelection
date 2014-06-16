package org.lidiagroup.hmourit.tfg.util

import org.apache.spark.rdd.RDD
import org.apache.spark.SparkContext._

object AuxInfoTheory {

  private val log2 = { x: Double => math.log(x) / math.log(2) }

  def slowMutualInfo(
      data: RDD[Array[Double]],
      varX: Seq[Int],
      varY: Int) = {
    
    val mis = for (k <- varX) yield {
      val xy = data
        .map({ case dp => ((dp(k), dp(varY)), 1) })
        .reduceByKey(_+_)
        .collect

      val qx = data
        .map({ case dp => (dp(k), 1) })
        .reduceByKey(_+_)
        .collectAsMap()

      val qy = data
        .map({ case dp => (dp(varY), 1) })
        .reduceByKey(_+_)
        .collectAsMap()

      val n = data.count.toDouble

      val mi =
        (for (((x, y), qxy) <- xy) yield {
          (qxy / n) * log2((qxy / n) / ((qx(x) / n) * (qy(y) / n)))
        })
          .reduce(_+_)

      (k, mi)
    }

    mis
  }
  
  def slowConditionalMutualInfo(
      data: RDD[Array[Double]],
      varX: Seq[Int],
      varY: Int,
      varZ: Int) = {

    val cmis = for (k <- varX) yield {

      val n = data.count.toDouble

      val aux = data
        .map(dp => (dp(varZ), 1))
        .reduceByKey(_+_)
        .collect

      val cmi = (for ((z, qz) <- aux) yield {
        val data_z = data.filter(dp => dp(varZ) == z)

        val xyz = data_z
          .map({ case dp => ((dp(k), dp(varY)), 1) })
          .reduceByKey(_+_)
          .collect

        val qxz = data_z
          .map({ case dp => (dp(k), 1) })
          .reduceByKey(_+_)
          .collectAsMap()

        val qyz = data_z
          .map({ case dp => (dp(varY), 1) })
          .reduceByKey(_+_)
          .collectAsMap()

        val pz = qz / n

        val cmi_xy =
          (for (((x, y), qxyz) <- xyz) yield {
            val pxyz = (qxyz / n) / pz
            val pxz = (qxz(x) / n) / pz
            val pyz = (qyz(y) / n) / pz

            pxyz * log2(pxyz / (pxz * pyz))

          })
            .reduce(_+_)

        pz * cmi_xy
      })
        .reduce(_+_)

      (k, cmi)
    }

    cmis
    
  }
}

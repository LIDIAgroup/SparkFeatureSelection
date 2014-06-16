package org.lidiagroup.hmourit.tfg.util

import org.apache.spark.{SparkConf, SparkContext}
import org.apache.log4j.{Level, Logger}
import org.apache.spark.mllib.regression.LabeledPoint
import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.rdd.RDD

/**
 * Created by cluster-spark on 4/30/14.
 */
object MySparkUtils {

  def getSparkContext: SparkContext = {

    Logger.getLogger("org").setLevel(Level.WARN)
    Logger.getLogger("akka").setLevel(Level.WARN)

    val SPARK_VERSION="1.0.0-SNAPSHOT"
    val MASTER = "mesos://cluster02:5050"

    val conf = new SparkConf()
      .setAppName("My application")
      .setSparkHome("/home/cluster-spark/workspace/spark")

    MASTER match {
      case local if local.startsWith("local") =>
        conf.setMaster(local)
      case mesos if mesos.startsWith("mesos") =>
        conf.setMaster(mesos)
          .set("spark.executor.uri", "hdfs://cluster06:8020/lib/mesos/spark-" + SPARK_VERSION + ".tar.gz")
          .set("spark.mesos.coarse", "true")
          .setJars(List("./target/scala-2.10/simple-mllib-project_2.10-1.0.jar"))
          .set("spark.executor.memory", "6900m")
          .set("spark.default.parallelism", "6")
    }

    new SparkContext(conf)
  }

  def getRDD(sc: SparkContext, dataset: String, format: String) = {

    val hdfsPrefix = "hdfs://cluster06:8020"
    val folder = "/home/cluster/input/"
    val sep = format match {
      case ".csv" => ","
      case ".tsv" => "\t"
      case _ => ""
    }

    sc.textFile(hdfsPrefix + folder + dataset + format)
      .map(_.split(sep))
      .map(_.map(_.toDouble))
      .map({ values => LabeledPoint(values.head, Vectors.dense(values.tail)) })
  }

  def datasetHeader(dataset: String, lps: RDD[LabeledPoint]) = {
    "*********************************************\n" +
    s"Dataset: ${dataset}\n" +
    s"Number of features: ${lps.first.features.size}\n" +
    s"Number of data points: ${lps.count}\n" +
    "*********************************************\n"
  }

}

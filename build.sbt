import sbtassembly.Plugin.AssemblyKeys
import AssemblyKeys._
import sbt._ 
import Keys._

assemblySettings

name := "SparkFeatureSelection"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
	"org.apache.spark"  %% "spark-core"     % "1.0.0-SNAPSHOT",
  "org.apache.spark"  %% "spark-mllib"    % "1.0.0-SNAPSHOT",
	"org.apache.hadoop" %  "hadoop-client"  % "2.0.0-cdh4.4.0"
)

resolvers ++= Seq(
  "Local Maven Repository" at "file://"+Path.userHome.absolutePath+"/.m2/repository",
	"Akka Repository"        at "http://repo.akka.io/releases/",
  "cloudera"               at "https://repository.cloudera.com/artifactory/cloudera-repos/"
)


lazy val copyDependencies = TaskKey[Unit]("copy-dependencies")

def copyDepTask = copyDependencies <<= (update, crossTarget, scalaVersion) map {
  (updateReport, out, scalaVer) =>
  updateReport.allFiles foreach { srcPath =>
    val destPath = out / "lib" / srcPath.getName
    IO.copyFile(srcPath, destPath, preserveLastModified=true)
  }
}

lazy val root = Project(
  "root",
  file("."),
  settings = Defaults.defaultSettings ++ Seq(
    copyDepTask
  )
)



val excludes = Set("")

excludedJars in assembly <<= (fullClasspath in assembly) map { cp => 
  cp filter { jar => excludes(jar.data.getName) }
}

mainClass in assembly := Some("spark.simple.main.SimpleMLlib")

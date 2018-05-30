javacOptions ++= Seq("-encoding", "UTF-8")

lazy val root = (project in file(".")).
  settings(
    name := "spark-hadoop-performance",
    version := "1.0",
    scalaVersion := "2.10.5",
    mainClass in Compile := Some("io.witlox.spark.Performance")
  )

val sparkVersion = "1.6.0"
val hadoopVersion = "2.6.0"
val scoptVersion = "3.5.0"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion % "provided",
  "org.apache.spark" %% "spark-sql" % sparkVersion % "provided",
  "org.apache.hadoop" % "hadoop-client" % hadoopVersion % "provided",
  "com.github.scopt" %% "scopt" % scoptVersion
)
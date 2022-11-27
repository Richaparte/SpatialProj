val SparkVersion = "3.0.3"

val SparkCompatibleVersion = "3.0"

val HadoopVersion = "2.7.2"

val SedonaVersion = "1.2.0-incubating"

val ScalaCompatibleVersion = "2.12"

// Change the dependency scope to "provided" when you run "sbt assembly"
val dependencyScope = "compile"

val geotoolsVersion = "1.1.0-25.2"
lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.twitter",
      scalaVersion := "2.12.12",
      version := "1.0"
    )),
    name := "quickstart",
    libraryDependencies ++= Seq(
      "com.twitter" %% "finagle-http" % "22.7.0",
      "org.apache.spark" %% "spark-core" % SparkVersion % dependencyScope exclude("org.apache.hadoop", "*"),
      "org.apache.spark" %% "spark-sql" % SparkVersion % dependencyScope exclude("org.apache.hadoop", "*"),
      "org.apache.hadoop" % "hadoop-mapreduce-client-core" % HadoopVersion % dependencyScope,
      "org.apache.hadoop" % "hadoop-common" % HadoopVersion % dependencyScope,
      "org.apache.hadoop" % "hadoop-hdfs" % HadoopVersion % dependencyScope,
      "org.apache.sedona" % "sedona-core-".concat(SparkCompatibleVersion).concat("_").concat(ScalaCompatibleVersion) % SedonaVersion,
      "org.apache.sedona" % "sedona-sql-".concat(SparkCompatibleVersion).concat("_").concat(ScalaCompatibleVersion) % SedonaVersion,
      "org.apache.sedona" % "sedona-viz-".concat(SparkCompatibleVersion).concat("_").concat(ScalaCompatibleVersion) % SedonaVersion,
      "org.locationtech.jts" % "jts-core" % "1.18.0" % "compile",
      "org.wololo" % "jts2geojson" % "0.14.3" % "compile", // Only needed if you read GeoJSON files. Under MIT License
      //  The following GeoTools packages are only required if you need CRS transformation. Under GNU Lesser General Public License (LGPL) license
      "org.datasyslab" % "geotools-wrapper" % geotoolsVersion % "compile"

    )
  )
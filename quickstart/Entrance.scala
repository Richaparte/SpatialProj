import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{DataFrame, SaveMode, SparkSession}
import org.apache.spark.serializer.KryoSerializer
import org.apache.sedona.sql.utils.SedonaSQLRegistrator
import org.apache.sedona.viz.core.Serde.SedonaVizKryoRegistrator
import org.apache.sedona.viz.sql.utils.SedonaVizRegistrator
//import org.apache.spark.sql.functions.to_json
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.{read, write}
import org.json4s.DefaultFormats


object Entrance extends App {
  Logger.getLogger("org.spark_project").setLevel(Level.WARN)
  Logger.getLogger("org.apache").setLevel(Level.WARN)
  Logger.getLogger("akka").setLevel(Level.WARN)
  Logger.getLogger("com").setLevel(Level.WARN)

  def result(): String= {
    val spark: SparkSession = SparkSession.builder()
    .config("spark.serializer",classOf[KryoSerializer].getName)
    .config("spark.kryo.registrator", classOf[SedonaVizKryoRegistrator].getName)
    .master("local[*]")
    .appName("quickstart")
    .getOrCreate()

    SedonaSQLRegistrator.registerAll(spark)
    SedonaVizRegistrator.registerAll(spark)
    val dfTrajecotry = ManageTrajectory.loadTrajectoryData(spark, "data/simulated_trajectories.json")
    implicit val formats = DefaultFormats
    //print("dffffff")
    val dataFrame = queryLoader(spark, dfTrajecotry, "get-spatial-range" , "33.41415667570768 -111.92518396810091 33.414291502635706 -111.92254858414022", "/home/richa/Downloads/finagle/doc/src/sphinx/code/quickstart/output" + "get-spatial-range")

    val maps = dataFrame
      .collect
      .map(
        row => dataFrame
          .columns
          .foldLeft(Map.empty[String, Any])
          (
            (acc, item) => acc + (item -> row.getAs[Any](item))
          )
      )
    val json = Serialization.write(maps)


    print("maps issss")
    println(json)
//    print("data frame issss")
//    print(df)
////      print(df.toJSON.toString())
////    df.toJSON.toString()
    json
  }


  private def queryLoader(spark: SparkSession, dfTrajecotry: DataFrame, queryName: String, queryParams: String, outputPath: String): DataFrame =  {

    val queryParam = queryParams.split(" ")

    if (queryName.equalsIgnoreCase("get-spatial-range")) {
      if (queryParam.length != 4) throw new ArrayIndexOutOfBoundsException("Query " + queryName + " needs 4 parameter but you entered " + queryParam.length)
      ManageTrajectory.getSpatialRange(spark, dfTrajecotry, queryParam(0).toDouble, queryParam(1).toDouble, queryParam(2).toDouble, queryParam(3).toDouble)

    }
    else if (queryName.equalsIgnoreCase("get-spatiotemporal-range")) {
      if (queryParam.length != 6) throw new ArrayIndexOutOfBoundsException("Query " + queryName + " needs 6 parameter but you entered " + queryParam.length)
      ManageTrajectory.getSpatioTemporalRange(spark, dfTrajecotry, queryParam(0).toLong, queryParam(1).toLong, queryParam(2).toDouble, queryParam(3).toDouble, queryParam(4).toDouble, queryParam(5).toDouble)
    }
    else if (queryName.equalsIgnoreCase("get-knn")) {
      if (queryParam.length != 2) throw new ArrayIndexOutOfBoundsException("Query " + queryName + " needs 2 parameter but you entered " + queryParam.length)
      ManageTrajectory.getKNNTrajectory(spark, dfTrajecotry, queryParam(0).toLong, queryParam(1).toInt)
    }
    else {
      throw new NoSuchElementException("The given query name " + queryName + " is wrong. Please check your input.")
    }

    //ManageTrajectory.getKNNTrajectory(spark, dfTrajecotry, queryParam(0).toLong, queryParam(1).toInt)

  }
}



import org.apache.log4j.{Level, Logger}
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.functions._

object ManageTrajectory {

  Logger.getLogger("org.spark_project").setLevel(Level.WARN)
  Logger.getLogger("org.apache").setLevel(Level.WARN)
  Logger.getLogger("akka").setLevel(Level.WARN)
  Logger.getLogger("com").setLevel(Level.WARN)


  def loadTrajectoryData(spark: SparkSession, filePath: String): DataFrame =
    {
      

      var df = spark.read.format("json").option("inferSchema", "true").option("multiLine", "true").load(filePath)

      df = df.withColumn("input", explode(df.col("trajectory")))

      df = df.withColumn("location", df.col("input").getItem("location"))
        .withColumn("timestamp", df.col("input").getItem("timestamp"))

      df = df.withColumn("longitude", df.col("location").getItem(0))
        .withColumn("latitude", df.col("location").getItem(1))

      df = df.select("trajectory_id", "vehicle_id", "latitude", "longitude", "timestamp")

      df.createOrReplaceTempView("updated_ip")

      df = spark.sql("SELECT ST_Point(longitude, latitude) AS point, trajectory_id, vehicle_id, timestamp FROM updated_ip")

      df.createOrReplaceTempView("table")

      df

    }


  def getSpatialRange(spark: SparkSession, dfTrajectory: DataFrame, latMin: Double, lonMin: Double, latMax: Double, lonMax: Double): DataFrame =
  {
    
    dfTrajectory.createOrReplaceTempView("trajectory")

    var df = spark.sql("SELECT ARRAY(ST_X(trajectory.point), ST_Y(trajectory.point)) AS loc, vehicle_id, trajectory_id, timestamp FROM trajectory WHERE ST_Contains(ST_Envelope(ST_GeomFromText('LINESTRING(%s %s, %s %s)')), trajectory.point)".format(latMin, lonMin, latMax, lonMax))

    df = df.groupBy("trajectory_id", "vehicle_id").agg(collect_list("timestamp").alias("timestamp"), collect_list("loc").alias("location")).sort(desc("trajectory_id"))

    df

  }


  def getSpatioTemporalRange(spark: SparkSession, dfTrajectory: DataFrame, timeMin: Long, timeMax: Long, latMin: Double, lonMin: Double, latMax: Double, lonMax: Double): DataFrame =
  {
  
    dfTrajectory.createOrReplaceTempView("trajectory")

    var df = spark.sql("SELECT ARRAY(ST_X(trajectory.point), ST_Y(trajectory.point)) AS loc, vehicle_id, trajectory_id, timestamp FROM trajectory WHERE ST_Contains(ST_Envelope(ST_GeomFromText('LINESTRING(%s %s, %s %s)')), trajectory.point) AND timestamp BETWEEN %s AND %s".format(latMin, lonMin, latMax, lonMax, timeMin, timeMax))

    df = df.groupBy("trajectory_id", "vehicle_id").agg(collect_list("timestamp").alias("timestamp"), collect_list("loc").alias("location")).sort(desc("trajectory_id"))

    df
  }


  def getKNNTrajectory(spark: SparkSession, dfTrajectory: DataFrame, trajectoryId: Long, neighbors: Int): DataFrame =
  {
    

    var df = dfTrajectory.groupBy("trajectory_id").agg(collect_list("point").alias("point"))

    df.createOrReplaceTempView("trajectory")

    df = spark.sql("SELECT ST_Collect(point) AS multipoint, trajectory_id FROM trajectory")

    df.createOrReplaceTempView("trajectory")

    df = spark.sql("SELECT ST_Distance(A.multipoint, B.multipoint) AS dist, B.trajectory_id AS trajectory_id FROM trajectory AS A, trajectory AS B WHERE A.trajectory_id = %s AND B.trajectory_id != %s ORDER BY dist, trajectory_id LIMIT %s".format(trajectoryId, trajectoryId, neighbors))

    df = df.drop("dist")
    df.show()
    df


  }


}


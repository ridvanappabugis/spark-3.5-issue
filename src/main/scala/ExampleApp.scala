import org.apache.spark.sql.SparkSession

object ExampleApp {
  def main(args: Array[String]): Unit = {

    // Create SparkSession
    val session = SparkSession.builder()
      .appName("TestExample")
      .getOrCreate()

    try {
      // Read parent
      val parentData = session.read.format("avro").load("/data/shared/test/parent")

      // Self join parent and cache + materialize
      val parent = parentData.join(parentData, Seq("PID")).cache()
      parent.count()

      // Read child
      val child = session.read.format("avro").load("/data/shared/test/child")

      // Basic join
      val resultBasic = child.join(
        parent,
        parent("PID") === child("PARENT_ID")
      )
      // Count: 16781 (Wrong)
      println(s"Count no repartition: ${resultBasic.count()}")

      // Repartition parent join
      val resultRepartition = child.join(
        parent.repartition(),
        parent("PID") === child("PARENT_ID")
      )
      // Count: 50094 (Correct)
      println(s"Count with repartition: ${resultRepartition.count()}")

    } finally {
      session.stop()
    }
  }
}
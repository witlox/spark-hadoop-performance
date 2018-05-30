package io.witlox.spark

import org.apache.log4j.{Level, LogManager, Logger}
import org.apache.spark.sql.SQLContext
import org.apache.spark.{SparkConf, SparkContext}
import scopt.OptionParser

import scala.util.Random


object Performance {
  val log: Logger = LogManager.getLogger(getClass.getName)

  val outputColumn = "filtered"

  val parser: OptionParser[Config] = new OptionParser[Config]("scopt") {
    head("spark-hadoop-performance", "1.0")

    opt[Int]("records") required() action { (x, c) => c.copy(records = x) } text "number of records to generate (large integer, ex 1000000000)"
    opt[Int]("partitions") required() action { (x, c) => c.copy(partitions = x) } text "number of partitions to use (number of CPUs * 3)"
    opt[String]("output") required() action { (x, c) => c.copy(output = x) } text "hadoop output path"

    opt[Unit]("verbose") action { (_, c) => c.copy(verbose = true) } text "let's be very chatty (note that setting this will slow down everything)"
    opt[Unit]("very-verbose") action { (_, c) => c.copy(very_verbose = true) } text "let's all be very very chatty (note that setting this will severely slow down everything)"

    help("help") text "Prototype for analysing Hadoop performance using Spark"
  }

  def main(args: Array[String]) {
    parser.parse(args, Config()) match {
      case Some(config) =>
        val verbosity = config.verbose || config.very_verbose
        if (verbosity) {
          LogManager.getLogger("io.witlox").setLevel(Level.DEBUG)
        } else {
          LogManager.getLogger("io.witlox").setLevel(Level.INFO)
        }
        if (config.very_verbose) {
          LogManager.getRootLogger.setLevel(Level.DEBUG)
        }
        val conf = new SparkConf().setAppName("spark-hadoop-performance")
        val spark = new SparkContext(conf)
        val sql = new SQLContext(spark)
        import sql.implicits._
        log.info("generating random data")
        val t0 = System.nanoTime()
        val generatedData = spark.parallelize(Seq[Int](), config.partitions)
          .mapPartitions { _ => {
            (1 to config.records).map{_ => Random.nextInt}.iterator
          }}
        val t1 = System.nanoTime()
        log.info("writing rdd to hadoop")
        generatedData.toDF().write.parquet(config.output)
        val t2 = System.nanoTime()
        log.info("reading rdd back")
        sql.read.parquet(config.output)
        val t3 = System.nanoTime()
        log.info("seeking al primes in rdd")
        val primes = generatedData.map{ x =>
          smallestDivisor(x) == x
        }.filter(v => v).count()
        val t4 = System.nanoTime()
        log.info()
        log.info(
            "Summary:\n" +
            "Generated a dataset of %d integers (%d GB) and found %d primes\n" +
            "- generation took : %f ms\n" +
            "- writing took    : %f ms\n" +
            "- reading took    : %f ms\n" +
            "- seeking took    : %f ms\n" format (config.partitions * config.records,
                                                  config.partitions * config.records * 32 / 1000000000,
                                                  primes,
                                                  t1 - t0 / 1000,
                                                  t2 - t1 / 1000,
                                                  t3 - t2 / 1000,
                                                  t4 - t3 / 1000)
        )

      case None =>
        log.error("invalid program options")
        sys.exit(-1)
    }
  }

  def smallestDivisor(n : Int) : Int = {
    findDivisor(n, 2)
  }

  def findDivisor(n : Int, testDivisor : Int) : Int = {
    if (square(testDivisor) > n) {
      n
    } else if (divides(testDivisor, n)) {
      testDivisor
    } else {
      findDivisor(n, testDivisor + 1)
    }
  }

  def square(n : Int) : Int = n * n

  def divides(d : Int, n : Int) : Boolean = (n % d) == 0

  case class Config(records: Int = Int.MaxValue,
                    partitions: Int = 1,
                    output: String = "",
                    verbose: Boolean = false,
                    very_verbose: Boolean = false)

}

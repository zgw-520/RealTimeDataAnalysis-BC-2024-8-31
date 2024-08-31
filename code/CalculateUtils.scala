package com.dolphin

import org.apache.spark.rdd.RDD
import redis.clients.jedis.{Jedis, JedisPool, JedisPoolConfig}

object CalculateUtils {

  val config = new JedisPoolConfig()
  val pool = new JedisPool(config, "127.0.0.1", 6379, 10000, "123456")

  def getConnection(): Jedis = {
    pool.getResource
  }

  /**
    * 统计每个设备产生日志的条数，结果保存在Redis中
    * @param lines
    */
  def calculateDeviceLogCount(lines: RDD[String]): Unit = {
    val device01 = lines.filter(l => {l.split(",")(0) == "2400"})
    val device02 = lines.filter(l => {l.split(",")(0) == "7361"})
    val device03 = lines.filter(l => {l.split(",")(0) == "7363"})

    val jedis = getConnection()

    jedis.incrByFloat("DEVICE_2400_C", device01.count())
    jedis.incrByFloat("DEVICE_7361_C", device02.count())
    jedis.incrByFloat("DEVICE_7363_C", device03.count())

    jedis.close()
  }

  /**
    * 统计各个设备生产的物品数量，结果保存在Redis中
    * @param lines
    */
  def calculateTotalCount(lines: RDD[String]): Unit = {
    val device01 = lines.filter(l => {l.split(",")(0) == "2400"})
    val device02 = lines.filter(l => {l.split(",")(0) == "7361"})
    val device03 = lines.filter(l => {l.split(",")(0) == "7363"})

    device01.foreachPartition(part => {
      val jedis = getConnection()

      var sum = 0.0
      part.foreach(l => {
        val count = l.split(",")(3).toFloat
        sum = sum + count
      })

      jedis.incrByFloat("DEVICE_2400", sum)
      jedis.close()
    })

    device02.foreachPartition(part => {
      val jedis = getConnection()

      var sum = 0.0
      part.foreach(l => {
        val count = l.split(",")(3).toFloat
        sum = sum + count
      })

      jedis.incrByFloat("DEVICE_7361", sum)
      jedis.close()
    })

    device03.foreachPartition(part => {
      val jedis = getConnection()

      var sum = 0.0
      part.foreach(l => {
        val count = l.split(",")(3).toFloat
        sum = sum + count
      })

      jedis.incrByFloat("DEVICE_7363", sum)
      jedis.close()
    })
  }


  /**
    * 统计各个设备生产物品时出现异常的次数，结果保存在Redis中
    * @param lines
    */
  def calculateInvalidCount(lines: RDD[String]): Unit = {
    val device01 = lines.filter(l => {l.split(",")(0) == "2400"})
    val device02 = lines.filter(l => {l.split(",")(0) == "7361"})
    val device03 = lines.filter(l => {l.split(",")(0) == "7363"})

    device01.foreachPartition(part => {
      val jedis = getConnection()

      var sum = 0.0
      part.foreach(l => {
        val count = l.split(",")(4).toFloat
        sum = sum + count
      })

      jedis.incrByFloat("DEVICE_2400_INVALID", sum)
      jedis.close()
    })

    device02.foreachPartition(part => {
      val jedis = getConnection()

      var sum = 0.0
      part.foreach(l => {
        val count = l.split(",")(4).toFloat
        sum = sum + count
      })

      jedis.incrByFloat("DEVICE_7361_INVALID", sum)
      jedis.close()
    })

    device03.foreachPartition(part => {
      val jedis = getConnection()

      var sum = 0.0
      part.foreach(l => {
        val count = l.split(",")(4).toFloat
        sum = sum + count
      })

      jedis.incrByFloat("DEVICE_7363_INVALID", sum)
      jedis.close()
    })
  }

  /**
    * 统计各个设备运行时的平均温度，结果保存在Redis中
    * @param lines
    */
  def calculateAverageTemp(lines: RDD[String]): Unit = {
    val device01 = lines.filter(l => {l.split(",")(0) == "2400"})
    val device02 = lines.filter(l => {l.split(",")(0) == "7361"})
    val device03 = lines.filter(l => {l.split(",")(0) == "7363"})

    device01.foreachPartition(part => {
      val jedis = getConnection()

      var sum = 0.0
      part.foreach(l => {
        val count = l.split(",")(5).toFloat
        sum = sum + count
      })

      jedis.incrByFloat("DEVICE_2400_T", sum)
      jedis.close()
    })

    device02.foreachPartition(part => {
      val jedis = getConnection()

      var sum = 0.0
      part.foreach(l => {
        val count = l.split(",")(5).toFloat
        sum = sum + count
      })

      jedis.incrByFloat("DEVICE_7361_T", sum)
      jedis.close()
    })

    device03.foreachPartition(part => {
      val jedis = getConnection()

      var sum = 0.0
      part.foreach(l => {
        val count = l.split(",")(5).toFloat
        sum = sum + count
      })

      jedis.incrByFloat("DEVICE_7363_T", sum)
      jedis.close()
    })
  }
}
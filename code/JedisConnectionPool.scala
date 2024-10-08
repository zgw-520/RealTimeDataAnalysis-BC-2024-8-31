package com.dolphin

import redis.clients.jedis.{Jedis, JedisPool, JedisPoolConfig}

/**
  * Redis连接池
  */
object JedisConnectionPool{

  val config = new JedisPoolConfig()
  //最大连接数,
  config.setMaxTotal(20)
  //最大空闲连接数,
  config.setMaxIdle(10)
  //当调用borrow Object方法时，是否进行有效性检查 -->
  config.setTestOnBorrow(true)
  //10000代表超时时间（10秒）
  val pool = new JedisPool(config, "127.0.0.1", 6379, 10000, "123456")

  def getConnection(): Jedis = {
    pool.getResource
  }

  def main(args: Array[String]) {
    val conn = JedisConnectionPool.getConnection()
    //    val r1 = conn.get("yz")
    //    println(r1)
    //    conn.incrBy("yz", -50)
    //    val r2 = conn.get("yz")
    //    println(r2)

    val r = conn.keys("*")
    import scala.collection.JavaConversions._
    for(p <- r) {
      println(p + " : " + conn.get(p))
    }
  }
}

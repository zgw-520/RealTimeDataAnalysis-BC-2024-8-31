package com.dolphin

import kafka.common.TopicAndPartition
import kafka.message.MessageAndMetadata
import kafka.serializer.StringDecoder
import kafka.utils.{ZKGroupTopicDirs, ZkUtils}
import org.I0Itec.zkclient.ZkClient
import org.apache.spark.SparkConf
import org.apache.spark.rdd.RDD
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.kafka.{HasOffsetRanges, KafkaUtils, OffsetRange}
import org.apache.spark.streaming.{Duration, StreamingContext}

object RealtimeLogAnalysis {
  def main(args: Array[String]): Unit = {
    // 指定组名
    val group = "g001"
    // 创建SparkConf
    val conf = new SparkConf().setAppName("RealtimeLogAnalysis").setMaster("local[*]")
    // 创建StringContext，并设置时间间隔
    val ssc = new StreamingContext(conf, Duration(5000))
    // 指定消费者的topic名字
    val topic = "topic01"
    // 指定Kafka的broker地址（sparkStream的Task直连到Kafka的分区上，用更加底层的API消费，效率更高）
    val brokerList = "localhost:9092"
    // 指定zk地址，后期更新消费的偏移量时使用，以后可以使用redis或者MySQL来记录偏移量
    val zkQuorum = "localhost:2181"
    // 创建stream时使用的topic名字集合，SparkStreaming可以从多个topic中同时消费
    val topics = Set(topic)
    // 创建一个ZKGroupTopicDirs对象，其实是指定往zk中写入数据的目录，用于保存偏移量
    val topicDirs = new ZKGroupTopicDirs(group, topic)
    // 获取zookeeper中的路径 "/g001/offsets/wordcount"
    val zkTopicPath = s"${topicDirs.consumerOffsetDir}"

    // 准备Kafka的参数
    val kafkaParams = Map(
      "metadata.broker.list" -> brokerList,
      "group.id" -> group,
      //"zookeeper.connect" -> zkQuorum,
      // 从头开始读数据
      "auto.offset.reset" -> kafka.api.OffsetRequest.SmallestTimeString
    )

    // zookeeper的host和ip，创建一个client，用于更新偏移量的
    // 是zookeeper的客户端，可以从zk中读取偏移量数据，并更新偏移量
    val zkClient = new ZkClient(zkQuorum)

    // 查询该路径下是否子节点（默认有字节点为我们自己保存不同Partition时生成的）
    val children = zkClient.countChildren(zkTopicPath)

    var kafkaStream: InputDStream[(String, String)] = null

    var fromOffsets: Map[TopicAndPartition, Long] = Map()

    // 如果保存过 offset
    if (children > 0) {
      for (i <- 0 until children) {
        // /g001/offsets/wordcount/0
        val partitionOffset = zkClient.readData[String](s"$zkTopicPath/${i}")
        // wordcount/0
        val tp = TopicAndPartition(topic, i)
        // 将不同partition对应的offset增加到fromOffsets
        // wordcount/0 -> 10001
        fromOffsets += (tp -> partitionOffset.toLong)
      }

      // Key: kafka的Key values: "hello tom hello jerry"
      // 这个会将Kafka的消息进行transform，最终Kafka的数据都会变成（KafkaKey， message）这样的tuple
      val messageHandler = (mmd: MessageAndMetadata[String, String]) => (mmd.key(), mmd.message())

      // 通过KafkaUtils创建直连的DStream（fromOffsets参数的作用：按照前面计算好了的偏移量继续消费数据）
      // [String, String, StringDecoder, StringDecoder, (String, String)]
      // key value key的解码方式 value的解码方式
      kafkaStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder, (String, String)](ssc, kafkaParams, fromOffsets, messageHandler)
    } else {
      // 如果未保存，根据KafkaParam的配置使用最新（largest）或者最旧的（smallest）offset
      kafkaStream = KafkaUtils.createDirectStream[String, String, StringDecoder, StringDecoder](ssc, kafkaParams, topics)
    }

    // 偏移量的范围
    var offsetRanges = Array[OffsetRange]()

    kafkaStream.foreachRDD { kafkaRDD =>
      if (!kafkaRDD.isEmpty()) {
        // 只有KafkaRDD可以强转成HasOffsetRanges，并获取偏移量
        offsetRanges = kafkaRDD.asInstanceOf[HasOffsetRanges].offsetRanges

        val lines: RDD[String] = kafkaRDD.map(_._2)

        // 业务逻辑
        // 统计各个设备生产的物品数量
        CalculateUtils.calculateTotalCount(lines)
        // 统计各个设备运行时的平均温度
        CalculateUtils.calculateAverageTemp(lines)
        // 统计各个设备生产物品时出现异常的次数
        CalculateUtils.calculateInvalidCount(lines)
        // 统计每个设备日志的条数
        CalculateUtils.calculateDeviceLogCount(lines)


        for (o <- offsetRanges) {
          val zkPath = s"${topicDirs.consumerOffsetDir}/${o.partition}"
          // 将该partition的offset保存到zookeeper
          ZkUtils.updatePersistentPath(zkClient, zkPath, o.untilOffset.toString)
        }
      }
    }

    ssc.start()
    ssc.awaitTermination()
  }
}

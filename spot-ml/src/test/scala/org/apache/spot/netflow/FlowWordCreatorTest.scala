package org.apache.spot.netflow

import org.scalatest.{FlatSpec, Matchers}


class FlowWordCreatorTest extends FlatSpec with Matchers {

  // Replace ports in index 10 and 11
  val srcIP = "10.0.2.115"
  val dstIP = "172.16.0.107"
  val hour = 12
  val minute = 59
  val second = 32

  val ibyts = 222L
  val ipkts = 3L

  val timeCuts = Array(2.4, 4.8, 7.2, 9.6, 12.0, 14.4, 16.8, 19.2, 21.6, 24.0)
  val ipktCuts = Array(10d, 20d, 30d, 40d, 50d, 60d, 70d, 80d, 90d, 100d)
  val ibytCuts = Array(100d, 200d, 300d, 400d, 500d)

  val expectedIpktBin = 0
  val expectedIbytBin = 2
  val expectedTimeBin = 5


  val flowWordCreator = new FlowWordCreator(timeCuts, ibytCuts, ipktCuts)


  // 1. Test when sip is less than dip and sip is not 0 and dport is <= 1024 & sport > 1024 and min(dport, sport) !=0 +
  "flowWords" should "create word with ip_pair as sourceIp-destIp, port is dport and dest_word direction is -1" in {
    val srcPort = 2132
    val dstPort = 23

    val FlowWords(srcWord, dstWord) =
      flowWordCreator.flowWords(hour, minute, second, srcPort, dstPort, ipkts, ibyts)


    dstWord shouldBe "-1_23_5_2_0"
    srcWord shouldBe "23_5_2_0"

  }

  // 2. Test when sip is less than dip and sip is not 0 and sport is <= 1024 & dport > 1024 and min(dport, sport) !=0 +
  it should "create word with ip_pair as sourceIp-destIp, port is sport and src_word direction is -1" in {

    val srcPort = 23
    val dstPort = 2132

    val FlowWords(srcWord, dstWord) =
      flowWordCreator.flowWords(hour, minute, second, srcPort, dstPort, ipkts, ibyts)

    dstWord shouldBe "23_5_2_0"
    srcWord shouldBe "-1_23_5_2_0"
  }

  // 3. Test when sip is less than dip and sip is not 0 and dport and sport are > 1024 +
  it should "create word with ip_pair as sourceIp-destIp, port is 333333 and both words direction is 1 (not showing)" in {
    val srcPort = 8392
    val dstPort = 9874

    val FlowWords(srcWord, dstWord) =
      flowWordCreator.flowWords(hour, minute, second, srcPort, dstPort, ipkts, ibyts)

    dstWord shouldBe "333333_5_2_0"
    srcWord shouldBe "333333_5_2_0"
  }

  // 4. Test when sip is less than dip and sip is not 0 and dport is 0 but sport is not +
  it should "create word with ip_pair as sourceIp-destIp, port is sport and source_word direction is -1" in {
    val srcPort = 80
    val dstPort = 0

    val FlowWords(srcWord, dstWord) =
      flowWordCreator.flowWords(hour, minute, second, srcPort, dstPort, ipkts, ibyts)


    dstWord shouldBe "80_5_2_0"
    srcWord shouldBe "-1_80_5_2_0"
  }

  // 5. Test when sip is less than dip and sip is not 0 and sport is 0 but dport is not +
  it should "create word with ip_pair as sourceIp-destIp, port is dport and dest_word direction is -1 II" in {

    val srcPort = 0
    val dstPort = 43


    val FlowWords(srcWord, dstWord) =
      flowWordCreator.flowWords(hour, minute, second, srcPort, dstPort, ipkts, ibyts)


    dstWord shouldBe "-1_43_5_2_0"
    srcWord shouldBe "43_5_2_0"
  }

  // 6. Test when sip is less than dip and sip is not 0 and sport and dport are less or equal than 1024 +
  it should "create word with ip_pair as sourceIp-destIp, port is 111111 and both words direction is 1 (not showing)" in {
    val srcPort = 1024
    val dstPort = 80


    val FlowWords(srcWord, dstWord) =
      flowWordCreator.flowWords(hour, minute, second, srcPort, dstPort, ipkts, ibyts)

    dstWord shouldBe "111111_5_2_0"
    srcWord shouldBe "111111_5_2_0"
  }

  // 7. Test when sip is less than dip and sip is not 0 and sport and dport are 0+
  it should "create word with ip_pair as sourceIp-destIp, port is max(0,0) and both words direction is 1 (not showing)" in {
    val srcPort = 0
    val dstPort = 0


    val FlowWords(srcWord, dstWord) =
      flowWordCreator.flowWords(hour, minute, second, srcPort, dstPort, ipkts, ibyts)

    dstWord shouldBe "0_5_2_0"
    srcWord shouldBe "0_5_2_0"
  }

  // 8. Test when sip is not less than dip and dport is <= 1024 & sport > 1024 and min(dport, sport) !=0+
  it should "create word with ip_pair as destIp-sourceIp, port is dport and dest_word direction is -1" in {
    val srcPort = 3245
    val dstPort = 43


    val FlowWords(srcWord, dstWord) =
      flowWordCreator.flowWords(hour, minute, second, srcPort, dstPort, ipkts, ibyts)

    dstWord shouldBe "-1_43_5_2_0"
    srcWord shouldBe "43_5_2_0"

  }

  // 9. Test when sip is not less than dip and sport is <= 1024 & dport > 1024 and min(dport, sport) !=0 +
  it should "create word with ip_pair as destIp-sourceIp, port is sport and src_word direction is -1" in {
    val srcPort = 80
    val dstPort = 2435

    val FlowWords(srcWord, dstWord) =
      flowWordCreator.flowWords(hour, minute, second, srcPort, dstPort, ipkts, ibyts)

    dstWord shouldBe "80_5_2_0"
    srcWord shouldBe "-1_80_5_2_0"

  }

  // 10. Test when sip is not less than dip and dport and sport are > 1024 +
  it should "create word with ip_pair as destIp-sourceIp, port is 333333 and both words direction is 1 (not showing)" in {
    val srcPort = 2354
    val dstPort = 2435


    val FlowWords(srcWord, dstWord) =
      flowWordCreator.flowWords(hour, minute, second, srcPort, dstPort, ipkts, ibyts)

    dstWord shouldBe "333333_5_2_0"
    srcWord shouldBe "333333_5_2_0"
  }

  // 11. Test when sip is not less than dip and dport is 0 but sport is not +
  it should "create word with ip_pair as destIp-sourceIp, port is sport and src_word direction is -1 II" in {
    val srcPort = 80
    val dstPort = 0


    val FlowWords(srcWord, dstWord) =
      flowWordCreator.flowWords(hour, minute, second, srcPort, dstPort, ipkts, ibyts)

    dstWord shouldBe "80_5_2_0"
    srcWord shouldBe "-1_80_5_2_0"
  }

  // 12. Test when sip is not less than dip and sport is 0 but dport is not +
  it should "create word with ip_pair as destIp-sourceIp, port is dport and dest_word direction is -1 II" in {
    val srcPort = 0
    val dstPort = 2435


    val FlowWords(srcWord, dstWord) =
      flowWordCreator.flowWords(hour, minute, second, srcPort, dstPort, ipkts, ibyts)

    dstWord shouldBe "-1_2435_5_2_0"
    srcWord shouldBe "2435_5_2_0"
  }

  // 13. Test when sip is not less than dip and sport and dport are less or equal than 1024
  it should "create word with ip_pair as destIp-sourceIp, port 111111 and both words direction is 1 (not showing)" in {
    val srcPort = 80
    val dstPort = 1024


    val FlowWords(srcWord, dstWord) =
      flowWordCreator.flowWords(hour, minute, second, srcPort, dstPort, ipkts, ibyts)

    dstWord shouldBe "111111_5_2_0"
    srcWord shouldBe "111111_5_2_0"
  }

  // 14. Test when sip is not less than dip and sport and dport are 0
  it should "create word with ip_pair as destIp-sourceIp, port is max(0,0) and both words direction is 1 (not showing)" in {
    val srcPort = 0
    val dstPort = 0


    val FlowWords(srcWord, dstWord) =
      flowWordCreator.flowWords(hour, minute, second, srcPort, dstPort, ipkts, ibyts)

    dstWord shouldBe "0_5_2_0"
    srcWord shouldBe "0_5_2_0"
  }
}

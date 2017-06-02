/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spot.netflow

import org.scalatest.{FlatSpec, Matchers}


class FlowWordCreatorTest extends FlatSpec with Matchers {

  // Replace ports in index 10 and 11
  val srcIP = "10.0.2.115"
  val dstIP = "172.16.0.107"
  val hour = 12
  val minute = 59
  val second = 32

  val protocol = "UDP"
  val ibyts = 222L  // ceil of log2 is 8
  val ipkts = 3L // ceil of log2 is 2
  
  

  // 1. Test when sip is less than dip and sip is not 0 and dport is <= 1024 & sport > 1024 and min(dport, sport) !=0 +
  "flowWords" should "create word with ip_pair as sourceIp-destIp, port is dport and dest_word direction is -1" in {
    val srcPort = 2132
    val dstPort = 23

    val FlowWords(srcWord, dstWord) =
      FlowWordCreator.flowWords(hour,  srcPort, dstPort, protocol, ibyts, ipkts)


    dstWord shouldBe "-1_23_UDP_12_8_2"
    srcWord shouldBe "23_UDP_12_8_2"

  }

  // 2. Test when sip is less than dip and sip is not 0 and sport is <= 1024 & dport > 1024 and min(dport, sport) !=0 +
  it should "create word with ip_pair as sourceIp-destIp, port is sport and src_word direction is -1" in {

    val srcPort = 23
    val dstPort = 2132

    val FlowWords(srcWord, dstWord) =
      FlowWordCreator.flowWords(hour,  srcPort, dstPort, protocol, ibyts, ipkts)

    dstWord shouldBe "23_UDP_12_8_2"
    srcWord shouldBe "-1_23_UDP_12_8_2"
  }

  // 3. Test when sip is less than dip and sip is not 0 and dport and sport are > 1024 +
  it should "create word with ip_pair as sourceIp-destIp, port is 333333 and both words direction is positive" in {
    val srcPort = 8392
    val dstPort = 9874

    val FlowWords(srcWord, dstWord) =
      FlowWordCreator.flowWords(hour,  srcPort, dstPort, protocol, ibyts, ipkts)

    dstWord shouldBe "333333_UDP_12_8_2"
    srcWord shouldBe "333333_UDP_12_8_2"
  }

  // 4. Test when sip is less than dip and sip is not 0 and dport is 0 but sport is not +
  it should "create word with ip_pair as sourceIp-destIp, port is sport and source_word direction is -1" in {
    val srcPort = 80
    val dstPort = 0

    val FlowWords(srcWord, dstWord) =
      FlowWordCreator.flowWords(hour,  srcPort, dstPort, protocol, ibyts, ipkts)


    dstWord shouldBe "80_UDP_12_8_2"
    srcWord shouldBe "-1_80_UDP_12_8_2"
  }

  // 5. Test when sip is less than dip and sip is not 0 and sport is 0 but dport is not +
  it should "create word with ip_pair as sourceIp-destIp, port is dport and dest_word direction is -1 (case 2)" in {

    val srcPort = 0
    val dstPort = 43


    val FlowWords(srcWord, dstWord) =
      FlowWordCreator.flowWords(hour,  srcPort, dstPort, protocol, ibyts, ipkts)


    dstWord shouldBe "-1_43_UDP_12_8_2"
    srcWord shouldBe "43_UDP_12_8_2"
  }

  // 6. Test when sip is less than dip and sip is not 0 and sport and dport are less or equal than 1024 +
  it should "create word with ip_pair as sourceIp-destIp, port is 111111 and both words in positive direction" in {
    val srcPort = 1024
    val dstPort = 80


    val FlowWords(srcWord, dstWord) =
      FlowWordCreator.flowWords(hour,  srcPort, dstPort, protocol, ibyts, ipkts)

    dstWord shouldBe "111111_UDP_12_8_2"
    srcWord shouldBe "111111_UDP_12_8_2"
  }

  // 7. Test when sip is less than dip and sip is not 0 and sport and dport are 0+
  it should "create word with ip_pair as sourceIp-destIp, port is max(0,0) and both words in positive direction" in {
    val srcPort = 0
    val dstPort = 0


    val FlowWords(srcWord, dstWord) =
      FlowWordCreator.flowWords(hour, srcPort, dstPort, protocol, ibyts, ipkts)

    dstWord shouldBe "0_UDP_12_8_2"
    srcWord shouldBe "0_UDP_12_8_2"
  }

  // 8. Test when sip is not less than dip and dport is <= 1024 & sport > 1024 and min(dport, sport) !=0+
  it should "create word with ip_pair as destIp-sourceIp, port is dport and dest_word direction is -1" in {
    val srcPort = 3245
    val dstPort = 43


    val FlowWords(srcWord, dstWord) =
      FlowWordCreator.flowWords(hour,  srcPort, dstPort, protocol, ibyts, ipkts)

    dstWord shouldBe "-1_43_UDP_12_8_2"
    srcWord shouldBe "43_UDP_12_8_2"

  }

  // 9. Test when sip is not less than dip and sport is <= 1024 & dport > 1024 and min(dport, sport) !=0 +
  it should "create word with ip_pair as destIp-sourceIp, port is sport and src_word direction is -1" in {
    val srcPort = 80
    val dstPort = 2435

    val FlowWords(srcWord, dstWord) =
      FlowWordCreator.flowWords(hour, srcPort, dstPort, protocol, ibyts, ipkts)

    dstWord shouldBe "80_UDP_12_8_2"
    srcWord shouldBe "-1_80_UDP_12_8_2"

  }

  // 10. Test when sip is not less than dip and dport and sport are > 1024 +
  it should "create word with ip_pair as destIp-sourceIp, port is 333333 and both words direction is positive" in {
    val srcPort = 2354
    val dstPort = 2435


    val FlowWords(srcWord, dstWord) =
      FlowWordCreator.flowWords(hour,  srcPort, dstPort, protocol, ibyts, ipkts)

    dstWord shouldBe "333333_UDP_12_8_2"
    srcWord shouldBe "333333_UDP_12_8_2"
  }

  // 11. Test when sip is not less than dip and dport is 0 but sport is not +
  it should "create word with ip_pair as destIp-sourceIp, port is sport and src_word direction is -1 (Case 2)" in {
    val srcPort = 80
    val dstPort = 0


    val FlowWords(srcWord, dstWord) =
      FlowWordCreator.flowWords(hour,  srcPort, dstPort, protocol, ibyts, ipkts)

    dstWord shouldBe "80_UDP_12_8_2"
    srcWord shouldBe "-1_80_UDP_12_8_2"
  }

  // 12. Test when sip is not less than dip and sport is 0 but dport is not +
  it should "create word with ip_pair as destIp-sourceIp, port is dport and dest_word direction is -1 (case 2)" in {
    val srcPort = 0
    val dstPort = 2435


    val FlowWords(srcWord, dstWord) =
      FlowWordCreator.flowWords(hour, srcPort, dstPort, protocol, ibyts, ipkts)

    dstWord shouldBe "-1_2435_UDP_12_8_2"
    srcWord shouldBe "2435_UDP_12_8_2"
  }

  // 13. Test when sip is not less than dip and sport and dport are less or equal than 1024
  it should "create word with ip_pair as destIp-sourceIp, port 111111 and both words direction is positive" in {
    val srcPort = 80
    val dstPort = 1024


    val FlowWords(srcWord, dstWord) =
      FlowWordCreator.flowWords(hour,  srcPort, dstPort, protocol, ibyts, ipkts)

    dstWord shouldBe "111111_UDP_12_8_2"
    srcWord shouldBe "111111_UDP_12_8_2"
  }

  // 14. Test when sip is not less than dip and sport and dport are 0
  it should "create word with ip_pair as destIp-sourceIp, port is max(0,0) and both words direction is positive, protocol UDP" in {
    val srcPort = 0
    val dstPort = 0


    val FlowWords(srcWord, dstWord) =
      FlowWordCreator.flowWords(hour, srcPort, dstPort, protocol, ibyts, ipkts)

    dstWord shouldBe "0_UDP_12_8_2"
    srcWord shouldBe "0_UDP_12_8_2"
  }

  // 15. Test when sip is less than dip and sip is not 0 and dport is <= 1024 & sport > 1024 and min(dport, sport) !=0
  "flowWords" should "create word with ip_pair as sourceIp-destIp, port is dport and dest_word direction is -1 and protocol is TCP" in {
    val srcPort = 2132
    val dstPort = 23

    val protocol = "TCP"
    val FlowWords(srcWord, dstWord) =
      FlowWordCreator.flowWords(hour,  srcPort, dstPort, protocol, ibyts, ipkts)


    dstWord shouldBe "-1_23_TCP_12_8_2"
    srcWord shouldBe "23_TCP_12_8_2"

  }


  // 16. Test when sip is not less than dip and sport and dport are 0
  it should "create word with ip_pair as destIp-sourceIp, port is max(0,0) and both words direction is positive, protocol TCP" in {
    val srcPort = 0
    val dstPort = 0
    val protocol = "TCP"
    val FlowWords(srcWord, dstWord) =
      FlowWordCreator.flowWords(hour, srcPort, dstPort, protocol, ibyts, ipkts)

    dstWord shouldBe "0_TCP_12_8_2"
    srcWord shouldBe "0_TCP_12_8_2"
  }


  // 17. Test when sip is less than dip and sip is not 0 and dport is <= 1024 & sport > 1024 and min(dport, sport) !=0
  "flowWords" should "create word with ip_pair as sourceIp-destIp, port is dport and dest_word direction is -1 and protocol is UDP" in {
    val srcPort = 2132
    val dstPort = 23

    val FlowWords(srcWord, dstWord) =
      FlowWordCreator.flowWords(hour, srcPort, dstPort, protocol, ibyts, ipkts)


    dstWord shouldBe "-1_23_UDP_12_8_2"
    srcWord shouldBe "23_UDP_12_8_2"

  }


  // 18. Test when sip is not less than dip and sport and dport are 0
  it should "create word with ip_pair as destIp-sourceIp, port is max(0,0) and both words direction is positive " in {
    val srcPort = 0
    val dstPort = 0
    val protocol = "TCP"
    val FlowWords(srcWord, dstWord) =
      FlowWordCreator.flowWords(hour, srcPort, dstPort, protocol, ibyts, ipkts)

    dstWord shouldBe "0_TCP_12_8_2"
    srcWord shouldBe "0_TCP_12_8_2"
  }


  // 19. Test when sip is not less than dip and sport and dport are 0 and bytecount is 0
  it should "create word with ip_pair as destIp-sourceIp, port is max(0,0) and both words direction is positive with zero bytes" in {
    val srcPort = 0
    val dstPort = 0
    val protocol = "TCP"
    val ibyts = 0
    val FlowWords(srcWord, dstWord) =
      FlowWordCreator.flowWords(hour, srcPort, dstPort, protocol, ibyts, ipkts)

    dstWord shouldBe "0_TCP_12_0_2"
    srcWord shouldBe "0_TCP_12_0_2"
  }


  // 20. Test when sip is not less than dip and sport and dport are 0 and packet count is 0
  it should "create word with ip_pair as destIp-sourceIp, port is max(0,0) and both words direction is positive with protocol with zero packets" in {
    val srcPort = 0
    val dstPort = 0
    val protocol = "TCP"
    val ipkts = 0
    val FlowWords(srcWord, dstWord) =
      FlowWordCreator.flowWords(hour, srcPort, dstPort, protocol, ibyts, ipkts)

    dstWord shouldBe "0_TCP_12_8_0"
    srcWord shouldBe "0_TCP_12_8_0"
  }



}

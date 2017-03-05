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

object FlowColumnIndex extends Enumeration {
    val HOUR = 4
    val MINUTE = 5
    val SECOND = 6
    val SOURCEIP = 8
    val DESTIP = 9
    val SOURCEPORT = 10
    val DESTPORT = 11
    val IPKT = 16
    val IBYT = 17
    val NUMTIME = 27
    val IBYTBIN = 28
    val IPKTYBIN = 29
    val TIMEBIN = 30
    val PORTWORD = 31
    val IPPAIR = 32
    val SOURCEWORD = 33
    val DESTWORD = 34
  }


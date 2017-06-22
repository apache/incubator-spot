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

package org.apache.spark.sql
import org.apache.spark.sql.catalyst.ScalaReflection
import org.apache.spark.sql.expressions.UserDefinedFunction

import scala.language.implicitConversions
import scala.reflect.runtime.universe.{TypeTag, typeTag}
import scala.util.Try

/**
  * Defines custom udf implementations to accept 10 to 12 parameters.
  */
object WideUDFs {

  /**
    * Defines a user-defined function of 10 arguments as user-defined function (UDF).
    * The data types are automatically inferred based on the function's signature.
    *
    * @group udf_funcs
    */
  def udf[RT: TypeTag, A1: TypeTag, A2: TypeTag, A3: TypeTag, A4: TypeTag, A5: TypeTag, A6: TypeTag,
  A7: TypeTag, A8: TypeTag, A9: TypeTag, A10: TypeTag, A11: TypeTag](f: Function10[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, RT]):
  UserDefinedFunction = {
    val inputTypes = Try(ScalaReflection.schemaFor(typeTag[A1]).dataType
      :: ScalaReflection.schemaFor(typeTag[A2]).dataType
      :: ScalaReflection.schemaFor(typeTag[A3]).dataType
      :: ScalaReflection.schemaFor(typeTag[A4]).dataType
      :: ScalaReflection.schemaFor(typeTag[A5]).dataType
      :: ScalaReflection.schemaFor(typeTag[A6]).dataType
      :: ScalaReflection.schemaFor(typeTag[A7]).dataType
      :: ScalaReflection.schemaFor(typeTag[A8]).dataType
      :: ScalaReflection.schemaFor(typeTag[A9]).dataType
      :: ScalaReflection.schemaFor(typeTag[A10]).dataType
      :: Nil).getOrElse(Nil)
    UserDefinedFunction(f, ScalaReflection.schemaFor(typeTag[RT]).dataType, Option(inputTypes))
  }


  /**
    * Defines a user-defined function of 11 arguments as user-defined function (UDF).
    * The data types are automatically inferred based on the function's signature.
    *
    * @group udf_funcs
    */
  def udf[RT: TypeTag, A1: TypeTag, A2: TypeTag, A3: TypeTag, A4: TypeTag, A5: TypeTag, A6: TypeTag,
  A7: TypeTag, A8: TypeTag, A9: TypeTag, A10: TypeTag, A11: TypeTag](f: Function11[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, RT]):
  UserDefinedFunction = {
    val inputTypes = Try(ScalaReflection.schemaFor(typeTag[A1]).dataType
      :: ScalaReflection.schemaFor(typeTag[A2]).dataType
      :: ScalaReflection.schemaFor(typeTag[A3]).dataType
      :: ScalaReflection.schemaFor(typeTag[A4]).dataType
      :: ScalaReflection.schemaFor(typeTag[A5]).dataType
      :: ScalaReflection.schemaFor(typeTag[A6]).dataType
      :: ScalaReflection.schemaFor(typeTag[A7]).dataType
      :: ScalaReflection.schemaFor(typeTag[A8]).dataType
      :: ScalaReflection.schemaFor(typeTag[A9]).dataType
      :: ScalaReflection.schemaFor(typeTag[A10]).dataType
      :: ScalaReflection.schemaFor(typeTag[A11]).dataType
      :: Nil).getOrElse(Nil)
    UserDefinedFunction(f, ScalaReflection.schemaFor(typeTag[RT]).dataType, Option(inputTypes))
  }

  /**
    * Defines a user-defined function of 12 arguments as user-defined function (UDF).
    * The data types are automatically inferred based on the function's signature.
    *
    * @group udf_funcs
    */
  def udf[RT: TypeTag, A1: TypeTag, A2: TypeTag, A3: TypeTag, A4: TypeTag, A5: TypeTag, A6: TypeTag,
  A7: TypeTag, A8: TypeTag, A9: TypeTag, A10: TypeTag, A11: TypeTag, A12: TypeTag](f: Function12[A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, RT]):
  UserDefinedFunction = {
    val inputTypes = Try(ScalaReflection.schemaFor(typeTag[A1]).dataType
      :: ScalaReflection.schemaFor(typeTag[A2]).dataType
      :: ScalaReflection.schemaFor(typeTag[A3]).dataType
      :: ScalaReflection.schemaFor(typeTag[A4]).dataType
      :: ScalaReflection.schemaFor(typeTag[A5]).dataType
      :: ScalaReflection.schemaFor(typeTag[A6]).dataType
      :: ScalaReflection.schemaFor(typeTag[A7]).dataType
      :: ScalaReflection.schemaFor(typeTag[A8]).dataType
      :: ScalaReflection.schemaFor(typeTag[A9]).dataType
      :: ScalaReflection.schemaFor(typeTag[A10]).dataType
      :: ScalaReflection.schemaFor(typeTag[A11]).dataType
      :: ScalaReflection.schemaFor(typeTag[A12]).dataType
      :: Nil).getOrElse(Nil)
    UserDefinedFunction(f, ScalaReflection.schemaFor(typeTag[RT]).dataType, Option(inputTypes))
  }
}

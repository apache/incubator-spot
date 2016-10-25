package org.apache.spark.sql

import org.apache.spark.sql.catalyst.ScalaReflection

import scala.language.implicitConversions
import scala.reflect.runtime.universe.{TypeTag, typeTag}
import scala.util.Try

object WideUDFs {
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
    UserDefinedFunction(f, ScalaReflection.schemaFor(typeTag[RT]).dataType, inputTypes)
  }
}

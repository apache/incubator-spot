package org.apache.spot.utilities.data.validation

import org.apache.spark.sql.types.{DataType, StructType}

import scala.collection.mutable.ListBuffer


/**
  * Input schema routines
  */
object InputSchema {

  /**
    * Validate the incoming data schema matches the schema required for model creation and scoring.
    *
    * @param inSchema       incoming data frame
    * @param expectedSchema schema expected by model training and scoring methods
    * @return
    */
  def validate(inSchema: StructType, expectedSchema: StructType): InputSchemaValidationResponse = {
    val response: ListBuffer[String] = ListBuffer("Schema not compatible:")

    // reduce schema from struct field to only field name and type
    val inSchemaMap: Map[String, DataType] = inSchema.map(field => (field.name -> field.dataType)).toMap

    expectedSchema
      .map(field => (field.name, field.dataType))
      .foreach({ case (expectedFieldName: String, expectedDataType: DataType) => {

        val inFieldDataType = inSchemaMap.getOrElse(expectedFieldName, None)

        inFieldDataType match {
          case None => response.append(s"Field $expectedFieldName is not present. $expectedFieldName is required for " +
            s"model training and scoring.")
          case inputDataType: DataType =>
            if (inputDataType != expectedDataType)
              response.append(s"Field $expectedFieldName type ${inputDataType.typeName} is not the expected type " +
                s"${expectedDataType.typeName}")
        }
      }
      })

    response.length match {
      case 1 => InputSchemaValidationResponse(isValid = true, Seq())
      case _ => InputSchemaValidationResponse(isValid = false, response)
    }
  }

  case class InputSchemaValidationResponse(final val isValid: Boolean, final val errorMessages: Seq[String])

}

package com.gensler.scalavro.io.complex

import com.gensler.scalavro.io.AvroTypeIO
import com.gensler.scalavro.io.primitive.AvroLongIO
import com.gensler.scalavro.types.complex.AvroArray
import com.gensler.scalavro.error.{ AvroSerializationException, AvroDeserializationException }
import com.gensler.scalavro.util.ReflectionHelpers

import org.apache.avro.Schema
import org.apache.avro.Schema.Parser
import org.apache.avro.generic.{ GenericData, GenericArray, GenericDatumWriter }
import org.apache.avro.io.{ BinaryEncoder, BinaryDecoder }

import spray.json._

import scala.collection.mutable
import scala.util.{ Try, Success, Failure }
import scala.reflect.runtime.universe.TypeTag

import java.io.{ InputStream, OutputStream }

case class AvroArrayIO[T, S <: Seq[T]](avroType: AvroArray[T, S]) extends AvroTypeIO[S]()(avroType.originalTypeTag) {

  implicit def itemTypeTag = avroType.itemType.tag
  implicit def originalTypeTag = avroType.originalTypeTag

  val originalTypeVarargsApply = ReflectionHelpers.companionVarargsApply[S] match {
    case Some(methodMirror) => methodMirror
    case None => throw new IllegalArgumentException(
      "Sequence subclasses must have a companion object with a public varargs " +
        "apply method, but no such method was found for type [%s].".format(avroType.originalTypeTag.tpe)
    )
  }

  ////////////////////////////////////////////////////////////////////////////
  // BINARY ENCODING
  ////////////////////////////////////////////////////////////////////////////

  protected[scalavro] def write[G <: Seq[T]: TypeTag](
    items: G,
    encoder: BinaryEncoder,
    references: mutable.Map[Any, Long],
    topLevel: Boolean): Unit = {

    try {
      encoder.writeArrayStart
      encoder.setItemCount(items.size)
      for (item <- items) {
        encoder.startItem
        avroType.itemType.io.write(item, encoder, references, false)
      }
      encoder.writeArrayEnd
      encoder.flush
    }
    catch {
      case cause: Throwable =>
        throw new AvroSerializationException(items, cause)
    }
  }

  protected[scalavro] def read(
    decoder: BinaryDecoder,
    references: mutable.ArrayBuffer[Any],
    topLevel: Boolean) = {

    val items = new scala.collection.mutable.ArrayBuffer[T]

    def readBlock(): Long = {
      val numItems = (AvroLongIO read decoder)
      val absNumItems = math abs numItems
      if (numItems < 0L) { val bytesInBlock = (AvroLongIO read decoder) }
      (0L until absNumItems) foreach { _ =>
        items += avroType.itemType.io.read(decoder, references, false)
      }
      absNumItems
    }

    var itemsRead = readBlock()
    while (itemsRead != 0L) { itemsRead = readBlock() }
    originalTypeVarargsApply(items).asInstanceOf[S] // a Seq is passed to varargs MethodMirror.apply
  }

  ////////////////////////////////////////////////////////////////////////////
  // JSON ENCODING
  ////////////////////////////////////////////////////////////////////////////

  def writeJson[G <: Seq[T]: TypeTag](items: G) = {
    val itemsAsJson = items.map { item =>
      avroType.itemType.io.writeJson(item)
    }
    JsArray(itemsAsJson: _*)
  }

  def readJson(json: JsValue) = Try {
    json match {
      case JsArray(itemsAsJson) => {
        val items = itemsAsJson.map { json =>
          avroType.itemType.io.readJson(json).get
        }
        originalTypeVarargsApply(items).asInstanceOf[S] // a Seq is passed to varargs MethodMirror.apply
      }
      case _ => throw new AvroDeserializationException[S]
    }
  }

}
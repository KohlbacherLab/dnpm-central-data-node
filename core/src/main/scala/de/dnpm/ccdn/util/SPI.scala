package de.dnpm.ccdn.util


import java.util.ServiceLoader
import scala.util.{
  Try,
  Failure
}
import scala.reflect.ClassTag
//import scala.collection.Factory



trait SPI[T]:
  def getInstance: T


type ServiceType[S] = S match
  case SPI[t] => t


abstract class SPILoader[S <: SPI[?]](
  using spi: ClassTag[S]
)
extends Logging:

  def getInstance: Try[ServiceType[S]] =
    Try {
      ServiceLoader
        .load(spi.runtimeClass.asInstanceOf[Class[S]])
        .iterator
        .next
    }
    .recoverWith {
      case t =>
        log.warn(
          s"""Failed to load Service Provider Interface instance for ${spi.runtimeClass.getName}.
          Unless handled properly with a fallback option in the client component, this is the cause of any occurring exception!"""
        )
        Failure(t)
    }
    .map(
      _.getInstance.asInstanceOf[ServiceType[S]]
    )


  def getInstances: Iterator[ServiceType[S]] = 

    import scala.jdk.CollectionConverters._

    ServiceLoader.load(spi.runtimeClass.asInstanceOf[Class[S]])
      .iterator
      .asScala
      .map(
        _.getInstance.asInstanceOf[ServiceType[S]]
      )


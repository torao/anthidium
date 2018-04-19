package at.hazm.anthidium.block

import java.nio.ByteBuffer
import java.nio.channels.AsynchronousFileChannel

import at.hazm.anthidium.resolution.Resolution
import at.hazm.anthidium.{Shape, io}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

case class MetaInfo(shape:Shape, resolution:Resolution, vectorPackMethod:Byte,)

object MetaInfo {

  def read(channel:AsynchronousFileChannel, position:Long)(implicit _ec:ExecutionContext):Future[MetaInfo] = {
    val promise = Promise[MetaInfo]()
    val heading = ByteBuffer.allocate(1 + 4)
    channel.read(heading, position, (), io.onComplete[Integer, Unit] {
      case Success(length) =>
        val buffer = ByteBuffer.allocate
      case Failure(ex) => promise.failure(ex)
    })
    promise.future
  }
}


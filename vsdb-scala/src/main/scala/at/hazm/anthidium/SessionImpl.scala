package at.hazm.anthidium

import java.nio.ByteBuffer
import java.nio.channels.{AsynchronousFileChannel, FileLock}
import java.nio.file.StandardOpenOption.{CREATE, READ, WRITE}

import at.hazm.anthidium.DataStore._
import at.hazm.anthidium.block.Block
import at.hazm.anthidium.io._

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.{Failure, Success}

private class SessionImpl private[SessionImpl](
                                                         ds:DataStore, shared:Boolean, channel:AsynchronousFileChannel
                                                       )(implicit _ec:ExecutionContext) extends Session {

  override def read(buffer:ByteBuffer, pos:Long):Future[Int] = {
    val promise = Promise[Int]()
    channel.read(buffer, pos, (), io.onComplete[Integer, Unit] {
      case Success(length) => promise.success(length)
      case Failure(ex) => promise.failure(ex)
    })
    promise.future
  }

  def block(position:Long):Future[Block] = Block(channel, position)

  def reset():Unit = ()

  def next():Boolean = false

  override def close():Unit = channel.close()

}

private object SessionImpl {

  def apply(ds:DataStore, shared:Boolean)(implicit _ec:ExecutionContext):Future[SessionImpl] = {
    val promise = Promise[SessionImpl]()
    val channel = AsynchronousFileChannel.open(ds.path, Seq(CREATE, READ) ++ (if(!shared) Seq(WRITE) else Seq.empty):_*)
    channel.lock(0, SignatureLength, shared, (), io.onComplete[FileLock, Unit] {
      case Success(_) =>
        try {
          promise.success(new SessionImpl(ds, shared, channel))
        } catch {
          case ex:Throwable =>
            logger.error("fail to create session", ex)
            promise.failure(ex)
            channel.close()
        }
      case Failure(ex) =>
        logger.error(s"fhail to acquire ${if(shared) "shared" else "exclusive"} lock", ex)
        channel.close()
        promise.failure(ex)
    })
    promise.future
  }

}
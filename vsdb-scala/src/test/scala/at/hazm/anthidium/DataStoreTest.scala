package at.hazm.anthidium

import java.nio.file.{Files, Paths}

import org.scalatest._

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

class DataStoreTest extends FlatSpec with Matchers {
  private[this] val dir = Paths.get(".").toAbsolutePath

  "DataStore" should "create" in {
    val path = Files.createTempFile(dir, "test-", ".vsdb")
    Files.size(path) should be(0)
    val ds = Await.result(DataStore.create(path, Shape()), Duration.Inf)
  }

}

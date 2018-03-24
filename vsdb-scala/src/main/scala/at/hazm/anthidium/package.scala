package at.hazm

import org.slf4j.{Logger, LoggerFactory}

package object anthidium {
  val logger:Logger = LoggerFactory.getLogger(getClass.getName.dropRight(1).replaceAll("\\$", "."))

}

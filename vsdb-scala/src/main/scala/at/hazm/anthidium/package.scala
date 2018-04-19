package at.hazm

import org.slf4j.LoggerFactory

package object anthidium {
  private[anthidium] val logger = LoggerFactory.getLogger(getClass.getName.dropRight(1).replaceAll("\\$", "."))

}

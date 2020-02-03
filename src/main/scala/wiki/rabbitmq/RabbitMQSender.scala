package wiki.rabbitmq

/**
 * Created by Bondarenko on Feb, 03, 2020
 16:57.
 Project: Wikipedia
 */
object RabbitMQSender extends App {}

import cats.effect._
import com.rabbitmq.client.{DefaultSaslConfig, SaslConfig}
import dev.profunktor.fs2rabbit.config.Fs2RabbitConfig
import dev.profunktor.fs2rabbit.interpreter.RabbitClient
import javax.net.ssl.SSLContext

object RabbitClient {
  def apply[F[_]: ConcurrentEffect: ContextShift](
    config: Fs2RabbitConfig,
    blocker: Blocker,
    sslContext: Option[SSLContext] = None,
    saslConfig: SaslConfig = DefaultSaslConfig.PLAIN
  ): F[RabbitClient[F]] = ???
}

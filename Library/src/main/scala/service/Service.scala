package service

import org.http4s.HttpRoutes

trait Service[F[_]] {
  def prefixPath: String
  def httpRoutes: HttpRoutes[F]
}

package service

import cats.effect.Concurrent
import cats.implicits._
import io.circe.syntax._
import io.circe.generic.auto._
import model.UserRegisterForm
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe.jsonOf
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}
import repository.UserRepository
import org.http4s.circe._


final class UserService[F[_] : Concurrent](userRepository: UserRepository[F]) extends Http4sDsl[F] {
  val prefixPath = "api/v1/users"

  private implicit val userRegisterFormDecoder: EntityDecoder[F, UserRegisterForm] = jsonOf
  private implicit val booleanDecoder: EntityDecoder[F, Boolean] = jsonOf

  val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req@POST -> Root =>
      for {
        registerForm <- req.as[UserRegisterForm]
        id <- userRepository.create(registerForm)
        res <- Ok(s"User registered with id = $id")
      } yield res

    case GET -> Root / LongVar(userId) =>
      for {
        user <- userRepository.findById(userId).value
        res <- user match {
          case Some(u) => Ok(u.asJson)
          case None => NotFound("User not found")
        }
      } yield res

    case GET -> Root =>
      for {
        users <- userRepository.getAll
        res <- Ok(users.asJson)
      } yield res

    case req@PATCH -> Root / LongVar(userId) =>
      for {
        newAdminValue <- req.as[Boolean]
        isOk <- userRepository.updateAdmin(userId, newAdminValue).value
        res <- isOk match {
          case Some(_) => Ok(s"User with $userId updated")
          case None => BadRequest(s"User with id = $userId not found")
        }
      } yield res

    case DELETE -> Root / LongVar(userId) =>
      for {
        isDeleted <- userRepository.delete(userId).value
        res <- isDeleted match {
          case Some(user) => Ok(s"User = $user successfully deleted")
          case None => BadRequest(s"User with id = $userId not found")
        }
      } yield res
  }
}

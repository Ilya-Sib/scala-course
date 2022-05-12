package service.impl

import cats.effect.Concurrent
import cats.implicits._
import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.syntax._
import model.AuthorForm
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes, Response}
import repository.{AuthorRepository, BookRepository}
import service.Service

final class AuthorServiceImpl[F[_] : Concurrent](authorRepository: AuthorRepository[F],
                                                 bookRepository: BookRepository[F])
  extends Service[F] with Http4sDsl[F] {
  override lazy val prefixPath = "api/v1/authors"

  private implicit val authorFormDecoder: EntityDecoder[F, AuthorForm] = jsonOf

  def matchResult[T: Encoder](author: Option[T]): F[Response[F]] = {
    author match {
      case Some(v) => Ok(v.asJson)
      case None => NotFound("Author not found")
    }
  }

  override lazy val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req@POST -> Root =>
      for {
        author <- req.as[AuthorForm]
        id <- authorRepository.save(author)
        res <- Ok(s"Author with id = $id saved")
      } yield res

    case GET -> Root / LongVar(authorId) =>
      for {
        author <- authorRepository.findById(authorId).value
        res <- matchResult(author)
      } yield res

    case GET -> Root =>
      for {
        authors <- authorRepository.getAll
        res <- Ok(authors.asJson)
      } yield res

    case GET -> Root / LongVar(authorId) / "books" =>
      for {
        books <- authorRepository
          .findById(authorId)
          .semiflatMap(bookRepository.getBooksByAuthor)
          .value
        res <- matchResult(books)
      } yield res

    case DELETE -> Root / LongVar(authorId) =>
      for {
        isDeleted <- authorRepository.delete(authorId).value
        res <- isDeleted match {
          case Some(author) => Ok(s"Author = $author deleted")
          case None => BadRequest(s"Author with id = $authorId not found")
        }
      } yield res
  }
}

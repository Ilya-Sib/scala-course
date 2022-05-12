package service.impl

import cats.effect.Concurrent
import cats.implicits._
import io.circe.generic.auto._
import io.circe.syntax._
import model.BookForm
import org.http4s.circe.CirceEntityCodec.circeEntityEncoder
import org.http4s.circe._
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityDecoder, HttpRoutes}
import repository.{AuthorRepository, BookRepository, UserRepository}
import service.Service


final class BookServiceImpl[F[_] : Concurrent](bookRepository: BookRepository[F],
                                               authorRepository: AuthorRepository[F],
                                               userRepository: UserRepository[F])
  extends Service[F] with Http4sDsl[F] {
  override lazy val prefixPath = "api/v1/books"

  implicit val bookDecoder: EntityDecoder[F, BookForm] = jsonOf
  implicit val longDecoder: EntityDecoder[F, Long] = jsonOf

  override lazy val httpRoutes: HttpRoutes[F] = HttpRoutes.of[F] {
    case req@POST -> Root =>
      for {
        bookForm <- req.as[BookForm]
        id <- bookForm.authorsID
          .traverse(authorRepository.findById)
          .semiflatMap(_ => bookRepository.save(bookForm))
          .value
        res <- id match {
          case Some(value) => Ok(s"Book saved with id = $value")
          case None => BadRequest(s"Found wrong author id")
        }
      } yield res

    case GET -> Root / LongVar(bookId) =>
      for {
        book <- bookRepository.findById(bookId).value
        res <- book match {
          case Some(book) => Ok(book.asJson)
          case None => NotFound("Book not found")
        }
      } yield res

    case GET -> Root =>
      for {
        books <- bookRepository.getAll
        res <- Ok(books.asJson)
      } yield res

    case req@PATCH -> Root / LongVar(bookId) =>
      for {
        userId <- req.as[Long]
        isOk <- userRepository.findById(userId)
          .flatMap { user =>
            bookRepository.findById(bookId).semiflatMap { book =>
              bookRepository.giveToUser(book.id, user.id)
            }
          }.value
        res <- isOk match {
          case Some(_) => Ok(s"Book with id = $bookId, given to user with id = $userId")
          case None => BadRequest(s"User or Book not found")
        }
      } yield res

    case GET -> Root / LongVar(bookId) / "available" =>
      for {
        isAvailable <- bookRepository.isBookAvailable(bookId).value
        res <- isAvailable match {
          case Some(userId) => Ok(s"Book with id = $bookId, given to user with id = $userId")
          case None => BadRequest(s"Book available")
        }
      } yield res

    case req@PATCH -> Root / LongVar(bookId) / "return" =>
      for {
        userId <- req.as[Long]
        isOk <- bookRepository.returnBook(bookId, userId).value
        res <- isOk match {
          case Some(id) => Ok(s"Book $id returned")
          case None => BadRequest("Bad user or book")
        }
      } yield res

    case DELETE -> Root / LongVar(bookId) =>
      for {
        isDeleted <- bookRepository.delete(bookId).value
        res <- isDeleted match {
          case Some(book) => Ok(s"Book = $book deleted")
          case None => BadRequest(s"Book with id = $bookId not found")
        }
      } yield res
  }
}

/*
  TODO:
    1) encode/decode List    +
    2) validate giveToUser userId, id    +
    3) connect doobie +
    4) validate forms
    5) encode password
    6) jwt tokens
 */

package repository

import cats.data.OptionT
import cats.implicits._
import doobie._
import doobie.implicits._
import model.{Author, Book, BookForm}
import cats.effect._
import doobie.postgres._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor._

trait BookRepository[F[_]] {
  def save(book: BookForm): F[Long]
  def getAll: F[List[Book]]
  def giveToUser(bookId: Long, userId: Long): F[Unit]
  def isBookAvailable(id: Long): OptionT[F, Int]
  def findById(id: Long): OptionT[F, Book]
  def returnBook(bookId: Long, userId: Long): OptionT[F, Int]
  def getBooksByAuthor(author: Author): F[List[Book]]
  def delete(id: Long): OptionT[F, Book]
}

class BookRepositoryImpl[F[_] : Async](val xa: Transactor[F]) extends BookRepository[F] {
  override def save(book: BookForm): F[Long] = {
    val insertAuthorsQuery = "INSERT INTO library.public.books_author (book_id, author_id) VALUES (?, ?)"

    for {
      id <- saveQuery(book)
        .update
        .withUniqueGeneratedKeys[Long]("id")
        .transact(xa)
      authors = book.authorsID.map(aId => (id, aId))
      _ <- Update[(Long, Long)](insertAuthorsQuery)
        .updateMany(authors)
        .transact(xa)
    } yield id
  }

  override def getAll: F[List[Book]] =
    getAllQuery
      .query[Book]
      .to[List]
      .transact(xa)

  override def giveToUser(bookId: Long, userId: Long): F[Unit] =
    giveQuery(bookId, userId)
      .update
      .run
      .transact(xa)
      .as(())

  override def isBookAvailable(id: Long): OptionT[F, Int] =
    OptionT {
      isAvailableQuery(id)
        .query[Int]
        .option
        .transact(xa)
    }

  override def findById(id: Long): OptionT[F, Book] =
    OptionT {
      findByIdQuery(id)
        .query[Book]
        .option
        .transact(xa)
    }

  override def returnBook(bookId: Long, userId: Long): OptionT[F, Int] =
    OptionT {
      returnQuery(bookId, userId)
        .update
        .run
        .map(i => if (i == 0) None else Some(i))
        .transact(xa)
    }

  override def getBooksByAuthor(author: Author): F[List[Book]] =
    for (books <- getAll)
      yield books.filter(_.authors.contains(author.id))

  override def delete(id: Long): OptionT[F, Book] =
    findById(id).semiflatMap { book =>
      deleteQuery(id)
        .update
        .run
        .transact(xa)
        .as(book)
    }

  private def saveQuery(book: BookForm): Fragment =
    sql"""
        INSERT INTO library.public.books (title, description)
        VALUES (${book.title}, ${book.description})
       """

  private def getAllQuery: Fragment =
    sql"""
        SELECT b.id, array_agg(ba.author_id) AS authors, b.title, b.description
        FROM library.public.books b
        JOIN library.public.books_author ba ON b.id = ba.book_id
        GROUP BY (b.id, b.title, b.description)
        ORDER BY b.id
         """

  private def giveQuery(bookId: Long, userId: Long): Fragment =
    sql"""
        INSERT INTO library.public.books_user (book_id, user_id)
        VALUES ($bookId, $userId)
       """

  private def isAvailableQuery(id: Long): Fragment =
    sql"""
        SELECT user_id FROM library.public.books_user
        WHERE book_id = $id
        """

  private def findByIdQuery(id: Long): Fragment =
    sql"""
        SELECT b.id, array_agg(ba.author_id) AS authors, b.title, b.description
        FROM library.public.books b
        JOIN library.public.books_author ba ON b.id = ba.book_id
        WHERE b.id = $id
        GROUP BY (b.id, b.title, b.description)
        """

  private def returnQuery(bookId: Long, userId: Long): Fragment =
    sql"""
        DELETE FROM library.public.books_user
        WHERE book_id = $bookId AND user_id = $userId
        """

  private def deleteQuery(id: Long): Fragment =
    sql"""
        DELETE FROM library.public.books
        WHERE id = $id
         """
}
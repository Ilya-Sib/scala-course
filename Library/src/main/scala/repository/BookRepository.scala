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

class BookRepository[F[_] : Async](val xa: Transactor[F]) {
  def save(book: BookForm): F[Long] = {
    val insertAuthorsQuery = "INSERT INTO library.public.books_author (book_id, author_id) VALUES (?, ?)"

    for {
      id <- sql"""
        INSERT INTO library.public.books (title, description)
        VALUES (${book.title}, ${book.description})
       """
        .update
        .withUniqueGeneratedKeys[Long]("id")
        .transact(xa)
      authors = book.authorsID.map(aId => (id, aId))
      _ <- Update[(Long, Long)](insertAuthorsQuery).updateMany(authors).transact(xa)
    } yield id
  }

  def getAll: F[List[Book]] =
    sql"""
        SELECT b.id, array_agg(ba.author_id) AS authors, b.title, b.description
        FROM library.public.books b
        JOIN library.public.books_author ba ON b.id = ba.book_id
        GROUP BY (b.id, b.title, b.description)
        ORDER BY b.id
         """
      .query[Book]
      .to[List]
      .transact(xa)

  def giveToUser(bookId: Long, userId: Long): F[Unit] =
    sql"""
        INSERT INTO library.public.books_user (book_id, user_id)
        VALUES ($bookId, $userId)
       """
      .update
      .run
      .transact(xa)
      .as(())

  def isBookAvailable(id: Long): OptionT[F, Int] =
    OptionT {
      sql"""
        SELECT user_id FROM library.public.books_user
        WHERE book_id = $id
        """
        .query[Int]
        .option
        .transact(xa)
    }

  def findById(id: Long): OptionT[F, Book] =
    OptionT {
      sql"""
        SELECT b.id, array_agg(ba.author_id) AS authors, b.title, b.description
        FROM library.public.books b
        JOIN library.public.books_author ba ON b.id = ba.book_id
        WHERE b.id = $id
        GROUP BY (b.id, b.title, b.description)
        """
        .query[Book]
        .option
        .transact(xa)
    }

  def returnBook(bookId: Long, userId: Long): OptionT[F, Int] =
    OptionT {
      sql"""
        DELETE FROM library.public.books_user
        WHERE book_id = $bookId AND user_id = $userId
        """
        .update
        .run
        .map(i => if (i == 0) None else Some(i))
        .transact(xa)
    }

  def getBooksByAuthor(author: Author): F[List[Book]] =
    for (books <- getAll)
      yield books.filter(_.authors.contains(author.id))

  def delete(id: Long): OptionT[F, Book] =
    findById(id).semiflatMap { book =>
      sql"""
        DELETE FROM library.public.books
        WHERE id = $id
         """
        .update
        .run
        .transact(xa)
        .as(book)
    }
}
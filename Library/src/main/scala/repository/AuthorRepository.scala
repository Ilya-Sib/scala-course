package repository

import cats.data.OptionT
import cats.effect.Async
import cats.implicits._
import doobie._
import doobie.implicits._
import model.{Author, AuthorForm}

class AuthorRepository[F[_] : Async](val xa: Transactor[F]) {
  def save(author: AuthorForm): F[Long] =
    sql"""
        INSERT INTO library.public.authors (first_name, last_name)
        VALUES (${author.firstName}, ${author.secondName})
       """
    .update
    .withUniqueGeneratedKeys[Long]("id")
    .transact(xa)

  def getAll: F[List[Author]] =
    sql"""
        SELECT id, first_name, last_name
        FROM library.public.authors
        ORDER BY id
       """
      .query[Author]
      .to[List]
      .transact(xa)

  def findById(id: Long): OptionT[F, Author] =
    OptionT {
      sql"""
        SELECT id, first_name, last_name
        FROM library.public.authors
        WHERE id = $id
         """
        .query[Author]
        .option
        .transact(xa)
    }

  def delete(id: Long): OptionT[F, Author] =
    findById(id).semiflatMap { author =>
      sql"""
        DELETE FROM library.public.authors
        WHERE id = $id
         """
        .update
        .run
        .transact(xa)
        .as(author)
    }
}

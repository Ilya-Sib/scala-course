package repository

import cats.data.OptionT
import cats.effect.Async
import cats.implicits._
import doobie._
import doobie.implicits._
import model.{Author, AuthorForm}

trait AuthorRepository[F[_]] {
  def save(author: AuthorForm): F[Long]

  def getAll: F[List[Author]]

  def findById(id: Long): OptionT[F, Author]

  def delete(id: Long): OptionT[F, Author]
}

class AuthorRepositoryImpl[F[_] : Async](val xa: Transactor[F]) extends AuthorRepository[F] {
  override def save(author: AuthorForm): F[Long] =
    saveQuery(author)
      .update
      .withUniqueGeneratedKeys[Long]("id")
      .transact(xa)

  override def getAll: F[List[Author]] =
    getAllQuery
      .query[Author]
      .to[List]
      .transact(xa)

  override def findById(id: Long): OptionT[F, Author] =
    OptionT {
      findByIdQuery(id)
        .query[Author]
        .option
        .transact(xa)
    }

  override def delete(id: Long): OptionT[F, Author] =
    findById(id).semiflatMap { author =>
      deleteQuery(id)
        .update
        .run
        .transact(xa)
        .as(author)
    }

  private def saveQuery(author: AuthorForm): Fragment =
    sql"""
        INSERT INTO library.public.authors (first_name, last_name)
        VALUES (${author.firstName}, ${author.secondName})
       """

  private def getAllQuery: Fragment =
    sql"""
        SELECT id, first_name, last_name
        FROM library.public.authors
        ORDER BY id
       """

  private def findByIdQuery(id: Long): Fragment =
    sql"""
        SELECT id, first_name, last_name
        FROM library.public.authors
        WHERE id = $id
         """

  private def deleteQuery(id: Long): Fragment =
    sql"""
        DELETE FROM library.public.authors
        WHERE id = $id
         """
}

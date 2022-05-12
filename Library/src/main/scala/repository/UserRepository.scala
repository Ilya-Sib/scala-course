package repository

import cats.implicits._
import cats.data.OptionT
import cats.effect.Async
import doobie._
import doobie.implicits._
import model.{UserDTO, UserRegisterForm}

trait UserRepository[F[_]] {
  def create(registerForm: UserRegisterForm): F[Long]
  def getAll: F[List[UserDTO]]
  def findById(id: Long): OptionT[F, UserDTO]
  def updateAdmin(id: Long, admin: Boolean): OptionT[F, Unit]
  def delete(id: Long): OptionT[F, UserDTO]
}

class UserRepositoryImpl[F[_] : Async](val xa: Transactor[F]) extends UserRepository[F] {
  override def create(registerForm: UserRegisterForm): F[Long] =
    createQuery(registerForm)
      .update
      .withUniqueGeneratedKeys[Long]("id")
      .transact(xa)

  override def getAll: F[List[UserDTO]] =
    getAllQuery
      .query[UserDTO]
      .to[List]
      .transact(xa)

  override def findById(id: Long): OptionT[F, UserDTO] =
    OptionT {
      findByIdQuery(id)
        .query[UserDTO]
        .option
        .transact(xa)
    }

  override def updateAdmin(id: Long, admin: Boolean): OptionT[F, Unit] =
    findById(id).semiflatMap { _ =>
      updateQuery(id, admin)
        .update
        .run
        .transact(xa)
        .as(())
    }

  override def delete(id: Long): OptionT[F, UserDTO] =
    findById(id).semiflatMap { user =>
      deleteQuery(id)
        .update
        .run
        .transact(xa)
        .as(user)
    }

  private def createQuery(registerForm: UserRegisterForm): Fragment =
    sql"""
        INSERT INTO library.public.users (username, email, password_sha, creation_time)
        VALUES (${registerForm.username}, ${registerForm.email}, ${registerForm.password}, NOW())
       """

  private def getAllQuery: Fragment =
    sql"""
        SELECT id, username, email, admin
        FROM library.public.users
        ORDER BY id
       """

  private def findByIdQuery(id: Long): Fragment =
    sql"""
        SELECT id, username, email, admin FROM library.public.users
        WHERE id = $id
         """

  private def updateQuery(id: Long, admin: Boolean): Fragment =
    sql"""
        UPDATE library.public.users
        SET admin = $admin
        WHERE id = $id
        """

  private def deleteQuery(id: Long): Fragment =
    sql"""
        DELETE FROM library.public.users
        WHERE id = $id
         """
}
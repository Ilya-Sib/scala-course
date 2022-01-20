package repository

import cats.implicits._
import cats.data.OptionT
import cats.effect.Async
import doobie._
import doobie.implicits._
import model.{UserDTO, UserRegisterForm}

class UserRepository[F[_] : Async](val xa: Transactor[F]) {
  def create(registerForm: UserRegisterForm): F[Long] =
    sql"""
        INSERT INTO library.public.users (username, email, password_sha, creation_time)
        VALUES (${registerForm.username}, ${registerForm.email}, ${registerForm.password}, NOW())
       """
      .update
      .withUniqueGeneratedKeys[Long]("id")
      .transact(xa)

  def getAll: F[List[UserDTO]] =
    sql"""
        SELECT id, username, email, admin
        FROM library.public.users
        ORDER BY id
       """
      .query[UserDTO]
      .to[List]
      .transact(xa)

  def findById(id: Long): OptionT[F, UserDTO] =
    OptionT {
      sql"""
        SELECT id, username, email, admin FROM library.public.users
        WHERE id = $id
         """
        .query[UserDTO]
        .option
        .transact(xa)
    }

  def updateAdmin(id: Long, admin: Boolean): OptionT[F, Unit] =
    findById(id).semiflatMap { _ =>
      sql"""
        UPDATE library.public.users
        SET admin = $admin
        WHERE id = $id
        """
        .update
        .run
        .transact(xa)
        .as(())
    }

  def delete(id: Long): OptionT[F, UserDTO] =
    findById(id).semiflatMap { user =>
      sql"""
        DELETE FROM library.public.users
        WHERE id = $id
         """
        .update
        .run
        .transact(xa)
        .as(user)
    }
}
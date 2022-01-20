import doobie.{Read, Write}
import doobie.implicits._
import doobie._
import doobie.postgres._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor._

package object model {
  case class UserRegisterForm(username: String,
                              email: String,
                              password: String)

  case class UserDTO(id: Long,
                     username: String,
                     email: String,
                     isAdmin: Boolean)

  case class Author(id: Long,
                    firstName: String,
                    secondName: String)

  case class AuthorForm(firstName: String,
                        secondName: String)

  case class Book(id: Int,
                  authors: List[Int],
                  title: String,
                  description: String)

  case class BookForm(authorsID: List[Long],
                      title: String,
                      description: String)
}

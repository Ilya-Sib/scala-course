import cats.effect._
import doobie.Transactor
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server._
import repository.{AuthorRepository, BookRepository, UserRepository}
import service.{AuthorService, BookService, UserService}


object Server extends IOApp {
  import scala.concurrent.ExecutionContext.global

  def createServer[F[_] : Async]: Resource[F, Server] = {
    val xa: Transactor[F] = Transactor.fromDriverManager[F](
      "org.postgresql.Driver",
      "jdbc:postgresql:library",
      "postgres", // username
      "pass" // password
    )

    val userRepository = new UserRepository[F](xa)
    val bookRepository = new BookRepository[F](xa)
    val authorRepository = new AuthorRepository[F](xa)
    val userService = new UserService[F](userRepository)
    val bookService = new BookService[F](bookRepository, authorRepository, userRepository)
    val authorService = new AuthorService[F](authorRepository, bookRepository)

    val router = Router(
      userService.prefixPath -> userService.httpRoutes,
      bookService.prefixPath -> bookService.httpRoutes,
      authorService.prefixPath -> authorService.httpRoutes
    ).orNotFound

    BlazeServerBuilder[F](global)
         .bindHttp(8080, "localhost")
         .withHttpApp(router)
         .resource
  }

  override def run(args: List[String]): IO[ExitCode] = createServer[IO].use(_ => IO.never).as(ExitCode.Success)
}

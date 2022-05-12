import cats.effect._
import doobie.Transactor
import org.http4s.implicits._
import org.http4s.server._
import org.http4s.server.blaze.BlazeServerBuilder
import repository.{AuthorRepositoryImpl, BookRepositoryImpl, UserRepositoryImpl}
import service.impl.{AuthorServiceImpl, BookServiceImpl, UserServiceImpl}


object Server extends IOApp {
  import scala.concurrent.ExecutionContext.global

  def createServer[F[_] : Async]: Resource[F, Server] = {
    val xa: Transactor[F] = Transactor.fromDriverManager[F](
      "org.postgresql.Driver",
      "jdbc:postgresql:library",
      "admin", // username
      "admin" // password
    )

    val userRepository = new UserRepositoryImpl[F](xa)
    val bookRepository = new BookRepositoryImpl[F](xa)
    val authorRepository = new AuthorRepositoryImpl[F](xa)
    val userService = new UserServiceImpl[F](userRepository)
    val bookService = new BookServiceImpl[F](bookRepository, authorRepository, userRepository)
    val authorService = new AuthorServiceImpl[F](authorRepository, bookRepository)

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

  override def run(args: List[String]): IO[ExitCode] =
    createServer[IO].use(_ => IO.never).as(ExitCode.Success)
}

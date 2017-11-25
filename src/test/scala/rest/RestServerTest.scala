package rest

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.apc.model.{Gibberish, Gibberishes}
import com.apc.repository.Repository
import com.apc.rest.Router
import org.joda.time.DateTime
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class RestServerTest extends WordSpec with Matchers with ScalatestRouteTest with Router {

  val now: DateTime = DateTime.parse("2017-11-25T14:17:36")

  "The service" should {

    "return a Gibberishes object containing an empty list response for initial GET request to /gibberishes" in {
      Get("/gibberishes") ~> route ~> check {
        responseAs[Gibberishes] shouldEqual Gibberishes(Nil)
      }
    }

    "return 404 for GET request to /gibberish with id that doesn't exist" in {
      Get("/gibberish?id=0") ~> route ~> check {
        status shouldEqual StatusCodes.NotFound
      }
    }

    "return a Gibberishes object containing all previously inserted gibberish entries for GET request to /gibberishes" in {
      Await.result(Repository.insertGibberish("apc", now), Duration.Inf)
      Await.result(Repository.insertGibberish("ftw", now), Duration.Inf)
      Get("/gibberishes") ~> route ~> check {
        responseAs[Gibberishes] shouldEqual Gibberishes(List(Gibberish(1L, "apc", now), Gibberish(2, "ftw", now)))
      }
    }

    "return a Gibberish object with the given id containing the corresponding entry for GET request to /gibberish" in {
      Await.result(Repository.insertGibberish("apc", now), Duration.Inf)
      Get("/gibberish?id=1") ~> route ~> check {
        responseAs[Gibberish] shouldEqual Gibberish(1L, "apc", now)
      }
    }

    "leave GET requests to other paths unhandled" in {
      Get("/kermit") ~> route ~> check {
        handled shouldBe false
      }
    }

    "return a MethodNotAllowed error for PUT requests to the root path" in {
      Post("/gibberish?length=3", "apc apc com ftw ftw, lol lol yes no yes") ~> route ~> check {
        status shouldEqual StatusCodes.Created
      }
    }
  }
}

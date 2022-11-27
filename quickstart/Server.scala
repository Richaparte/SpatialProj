  import com.twitter.finagle.{Http, Service}
  import com.twitter.finagle.http
  import com.twitter.util.{Await, Future}
  import com.twitter.finagle.http.{Request, Response}

  object Server extends App {
    val service = new Service[http.Request, http.Response] {
      def apply(request: Request): Future[Response] = Future {
          print("request params are: ")
          print(request.getParams())
          val rep = Response()
          //rep.contentString = Entrance.result()
          rep.contentString = "hello"
          rep.setContentTypeJson()
          rep
        }
    }
    val server = Http.serve(":8080", service)
    Await.ready(server)
  }

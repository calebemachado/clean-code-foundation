package dev.clsax.cleancodefoundation

import io.vertx.core.Vertx
import io.vertx.ext.web.Route
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


fun Route.coroutineHandler(fn: suspend (RoutingContext) -> Unit) {
  var v = Dispatchers.Unconfined
  v = Vertx.currentContext().dispatcher()
  handler { ctx ->
    GlobalScope.launch(v) {
      try {
        fn(ctx)
      } catch (e: Exception) {
        ctx.fail(e)
      }
    }
  }
}

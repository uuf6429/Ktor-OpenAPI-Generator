package com.papsign.ktor.openapigen.route.response

import com.papsign.ktor.openapigen.annotations.Response
import com.papsign.ktor.openapigen.modules.providers.AuthProvider
import com.papsign.ktor.openapigen.route.OpenAPIRoute
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import io.ktor.util.pipeline.PipelineContext
import kotlin.reflect.full.findAnnotation

interface Responder {
    suspend fun <T: Any> respond(response: T, request: PipelineContext<Unit, ApplicationCall>)
    suspend fun <T: Any> respond(statusCode: HttpStatusCode, response: T, request: PipelineContext<Unit, ApplicationCall>)
}

interface OpenAPIPipelineContext {
    val route: OpenAPIRoute<*>
    val pipeline: PipelineContext<Unit, ApplicationCall>
    val responder: Responder
}

interface OpenAPIPipelineResponseContext<R> : OpenAPIPipelineContext
interface OpenAPIPipelineAuthContext<A, R> : OpenAPIPipelineResponseContext<R> {
    val authProvider: AuthProvider<A>
}

class ResponseContextImpl<R>(
        override val pipeline: PipelineContext<Unit, ApplicationCall>,
        override val route: OpenAPIRoute<*>,
        override val responder: Responder
) : OpenAPIPipelineResponseContext<R>

class AuthResponseContextImpl<A, R>(
        override val pipeline: PipelineContext<Unit, ApplicationCall>,
        override val authProvider: AuthProvider<A>,
        override val route: OpenAPIRoute<*>,
        override val responder: Responder
) : OpenAPIPipelineAuthContext<A, R>


suspend inline fun <reified R : Any> OpenAPIPipelineResponseContext<R>.respond(response: R) {
    val statusCode = R::class.findAnnotation<Response>()?.statusCode?.let { HttpStatusCode.fromValue(it) }
    if (statusCode == null) {
        responder.respond(response as Any, pipeline)
    } else {
        responder.respond(statusCode, response as Any, pipeline)
    }
}

package com.muthuraj.cycle.fill.network

import com.muthuraj.cycle.fill.BuildKonfig
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.Url

/**
 * The tunnel hostname is protected by a Cloudflare Access application that only admits
 * requests carrying this service token. The local URL never goes through Cloudflare, so
 * the headers are only attached for the tunnel host.
 */
object CloudflareAccess {
    const val HEADER_CLIENT_ID = "CF-Access-Client-Id"
    const val HEADER_CLIENT_SECRET = "CF-Access-Client-Secret"

    private val tunnelHost = Url(BuildKonfig.TUNNEL_URL).host

    val headers: Map<String, String> = mapOf(
        HEADER_CLIENT_ID to BuildKonfig.CF_ACCESS_CLIENT_ID,
        HEADER_CLIENT_SECRET to BuildKonfig.CF_ACCESS_CLIENT_SECRET,
    )

    fun isTunnelHost(host: String) = host.equals(tunnelHost, ignoreCase = true)

    val KtorPlugin = createClientPlugin("CloudflareAccess") {
        onRequest { request, _ ->
            if (isTunnelHost(request.url.host)) {
                headers.forEach { (name, value) -> request.headers.append(name, value) }
            }
        }
    }
}

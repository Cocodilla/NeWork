package ru.netology.nework.di

import java.net.InetAddress
import okhttp3.Dns
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.dnsoverhttps.DnsOverHttps

object AppDns {
    val dns: Dns by lazy {
        val bootstrapClient = OkHttpClient.Builder().build()

        DnsOverHttps.Builder()
            .client(bootstrapClient)
            .url("https://cloudflare-dns.com/dns-query".toHttpUrl())
            .bootstrapDnsHosts(
                listOf(
                    InetAddress.getByName("1.1.1.1"),
                    InetAddress.getByName("1.0.0.1"),
                )
            )
            .includeIPv6(false)
            .resolvePrivateAddresses(true)
            .build()
    }
}

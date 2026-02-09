package com.polarbookshop.orderservice.config

import org.jetbrains.annotations.NotNull
import org.springframework.boot.context.properties.ConfigurationProperties
import java.net.URI

@ConfigurationProperties(prefix = "polar")
data class ClientProperties(
    @field:NotNull
    val catalogServiceUri: URI
)
package com.mariaruiz.huertopedia.model

import org.jetbrains.compose.resources.DrawableResource

data class Plant(
    val name: String,
    val scientificName: String,
    val category: String,
    val imageRes: DrawableResource
)
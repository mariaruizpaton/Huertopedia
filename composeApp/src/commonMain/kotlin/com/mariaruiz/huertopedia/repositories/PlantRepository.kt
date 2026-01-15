package com.mariaruiz.huertopedia.repositories

import com.mariaruiz.huertopedia.model.Plant
import huertopedia.composeapp.generated.resources.Res
import huertopedia.composeapp.generated.resources.fresa
import huertopedia.composeapp.generated.resources.lechuga
import huertopedia.composeapp.generated.resources.menta
import huertopedia.composeapp.generated.resources.tomate

object PlantRepository {
    val listaDePlantas = listOf(
        Plant("Tomate", "Solanum lycopersicum","Hortalizas", Res.drawable.tomate),
        Plant("Lechuga", "Lactuca sativa", "Hortalizas", Res.drawable.lechuga),
        Plant("Fresa", "Fragaria", "Frutas", Res.drawable.fresa),
        Plant("Menta", "Mentha", "Hierbas", Res.drawable.menta),
        Plant("Tomate", "Solanum lycopersicum","Hortalizas", Res.drawable.tomate),
        Plant("Lechuga", "Lactuca sativa", "Hortalizas", Res.drawable.lechuga),
        Plant("Fresa", "Fragaria", "Frutas", Res.drawable.fresa),
        Plant("Menta", "Mentha", "Hierbas", Res.drawable.menta),
        Plant("Tomate", "Solanum lycopersicum","Hortalizas", Res.drawable.tomate),
        Plant("Lechuga", "Lactuca sativa", "Hortalizas", Res.drawable.lechuga),
        Plant("Fresa", "Fragaria", "Frutas", Res.drawable.fresa),
        Plant("Menta", "Mentha", "Hierbas", Res.drawable.menta)
    )
}
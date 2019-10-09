package com.egroden.teaco.sample.di.modules

import androidx.lifecycle.SavedStateHandle
import com.egroden.teaco.AndroidConnector
import com.egroden.teaco.sample.BuildConfig
import com.egroden.teaco.sample.data.network.NetworkClient
import com.egroden.teaco.sample.data.repo.MovieRepository
import com.egroden.teaco.sample.presentation.movie.*
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class MoviesModule(private val apiModule: ApiModule) {
    @UseExperimental(UnstableDefault::class)
    private val networkClient: NetworkClient by lazy {
        NetworkClient(
            client = apiModule.networkProvider.client,
            baseUrl = BuildConfig.API_URL.toHttpUrlOrNull()!!,
            json = Json.nonstrict
        )
    }

    private val moviesRepository: MovieRepository by lazy {
        MovieRepository(networkClient)
    }

    val factory: (SavedStateHandle) -> AndroidConnector<Action, SideEffect, State, Subscription> = {
        AndroidConnector(
            initialState = State(false, null, null),
            update = movieUpdater,
            effectHandler = MovieEffectHandler(
                moviesRepository
            ),
            savedStateHandle = it
        )
    }
}
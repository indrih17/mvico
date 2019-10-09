package com.egroden.teaco.sample.presentation.movie

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import androidx.recyclerview.widget.GridLayoutManager
import com.egroden.teaco.AndroidConnector
import com.egroden.teaco.androidConnectors
import com.egroden.teaco.sample.R
import com.egroden.teaco.sample.presentation.*
import com.egroden.teaco.sample.presentation.movie.adapter.MovieAdapter
import kotlinx.android.synthetic.main.movie_fragment.view.*

class MovieFragment(
    factory: (SavedStateHandle) -> AndroidConnector<Action, SideEffect, State, Subscription>
) : Fragment(R.layout.movie_fragment) {
    private val connector by androidConnectors(factory)

    private val recyclerValueEffect =
        Effect<List<MovieModel>>(emptyList())
    private val recyclerVisibilityEffect =
        Effect(Visibility.VISIBLE)
    private val progressBarEffect =
        Effect(Visibility.GONE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connector.connect(::render, lifecycle)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(view.movie_recycler_view) {
            val movieAdapter = MovieAdapter()
            layoutManager = GridLayoutManager(context, 2)
            adapter = movieAdapter
            recyclerValueEffect bind { movieAdapter.update(it) }
            recyclerVisibilityEffect bind { visibility(it) }
        }

        with(view.progress_bar) {
            visibility(Visibility.GONE)
            progressBarEffect bind { visibility(it) }
        }

        connector bindAction Action.LoadAction(1)
    }

    private fun render(state: State) {
        if (state.loading) {
            progressBarEffect.value = Visibility.VISIBLE
            recyclerVisibilityEffect.value = Visibility.GONE
        } else {
            progressBarEffect.value = Visibility.GONE
            recyclerVisibilityEffect.value = Visibility.VISIBLE
        }

        state.data?.let { recyclerValueEffect.value = it }
        if (state.error != null) {
            progressBarEffect.value = Visibility.GONE
            recyclerVisibilityEffect.value = Visibility.GONE
            view?.context?.toast(state.error.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        progressBarEffect.unbind()
        recyclerValueEffect.unbind()
        recyclerVisibilityEffect.unbind()
    }

    companion object {
        const val TAG = "MovieFragment"
    }
}
package com.egroden.sample.movies

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import androidx.recyclerview.widget.GridLayoutManager
import com.egroden.mvico.AndroidConnector
import com.egroden.mvico.androidConnectors
import com.egroden.sample.*

class MovieFragment(
    factory: (SavedStateHandle) -> AndroidConnector<Action, SideEffect, State, Subscription>
) : Fragment() {
    private val connector by androidConnectors(factory)

    private val recyclerValueEffect = Effect<List<Movie>>(emptyList())
    private val recyclerVisibilityEffect = Effect(Visibility.VISIBLE)
    private val progressBarEffect = Effect(Visibility.GONE)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        connector.connect(::render, lifecycle)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) =
        frameLayout {
            recyclerView {
                size(Size.MATCH_PARENT, Size.MATCH_PARENT)
                layoutManager(GridLayoutManager(context, 2))
                adapter(MovieAdapter())
                recyclerValueEffect bind { (adapter as MovieAdapter).update(it) }
                recyclerVisibilityEffect bind { visibility(it) }
            }
            progressBar {
                size(Size.WRAP_CONTENT, Size.WRAP_CONTENT)
                visibility(Visibility.GONE)
                gravity(Gravity.CENTER)
                progressBarEffect bind { visibility(it) }
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        connector bindAction Action.LoadAction(1)
    }

    private fun render(state: State) {
        progressBarEffect.value = if (state.loading) Visibility.VISIBLE else Visibility.GONE
        state.data?.let { recyclerValueEffect.value = it }
        recyclerVisibilityEffect.value = if (state.loading) Visibility.GONE else Visibility.VISIBLE
        state.error?.let {
            progressBarEffect.value = Visibility.GONE
            recyclerVisibilityEffect.value = Visibility.GONE
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

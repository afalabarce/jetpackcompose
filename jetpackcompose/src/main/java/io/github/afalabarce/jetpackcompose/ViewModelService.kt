package io.github.afalabarce.jetpackcompose

import androidx.lifecycle.*
import androidx.lifecycle.LifecycleService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

open class ViewModelService: LifecycleService(),
    ViewModelStoreOwner,
    HasDefaultViewModelProviderFactory, CoroutineScope by CoroutineScope(Dispatchers.IO) {
    private val mViewModelStore: ViewModelStore = ViewModelStore()

    override val  viewModelStore: ViewModelStore
        get() = mViewModelStore

    private var mDefaultViewModelProviderFactory: ViewModelProvider.Factory? = null
    override val defaultViewModelProviderFactory: ViewModelProvider.Factory
        get() {
            if (mDefaultViewModelProviderFactory == null)
                mDefaultViewModelProviderFactory = ViewModelProvider.AndroidViewModelFactory()

            return mDefaultViewModelProviderFactory!!
        }

    override fun onCreate() {
        super.onCreate()
        lifecycle.addObserver(object : LifecycleEventObserver {
            override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                if (source.lifecycle.currentState === Lifecycle.State.DESTROYED) {
                    mViewModelStore.clear()
                    source.lifecycle.removeObserver(this)
                }
            }
        })
    }
}
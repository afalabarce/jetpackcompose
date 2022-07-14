package io.github.afalabarce.jetpackcompose

import androidx.lifecycle.*
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

open class ViewModelService: LifecycleService(),
    ViewModelStoreOwner,
    HasDefaultViewModelProviderFactory, CoroutineScope by CoroutineScope(Dispatchers.IO) {
    private val mViewModelStore: ViewModelStore = ViewModelStore()
    private var mFactory: ViewModelProvider.Factory? = null

    override fun getViewModelStore(): ViewModelStore {
        return mViewModelStore
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

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory =
        if (mFactory != null)
            mFactory!!
        else (ViewModelProvider.AndroidViewModelFactory().also { mFactory = it })

    override fun getDefaultViewModelCreationExtras(): CreationExtras {
        return MutableCreationExtras(super.getDefaultViewModelCreationExtras()).apply {
            set(ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY, application)
        }

    }
}
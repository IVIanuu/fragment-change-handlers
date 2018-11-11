package androidx.fragment.app

internal val Fragment.who get() = mWho
internal val FragmentManager.activeFragments get() = (this as FragmentManagerImpl).activeFragments
internal val FragmentManager.host get() = (this as FragmentManagerImpl).mHost
internal val FragmentManager.parent: Fragment? get() = (this as FragmentManagerImpl).mParent
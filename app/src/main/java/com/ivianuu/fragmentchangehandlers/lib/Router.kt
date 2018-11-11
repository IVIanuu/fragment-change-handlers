package com.ivianuu.fragmentchangehandlers.lib

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.who
import com.ivianuu.fragmentchangehandlers.lib.internal.ChangeManager
import com.ivianuu.fragmentchangehandlers.lib.internal.ChangeTransaction
import com.ivianuu.fragmentchangehandlers.lib.internal.FragmentContainer
import com.ivianuu.fragmentchangehandlers.lib.internal.FragmentWithView
import com.ivianuu.fragmentchangehandlers.lib.internal.NoOpChangeHandler
import com.ivianuu.fragmentchangehandlers.lib.internal.forEachOf
import com.ivianuu.fragmentchangehandlers.util.d

/**
 * @author Manuel Wrage (IVIanuu)
 */
class Router internal constructor(private val fm: FragmentManager, private val containerId: Int) {

    internal var container: FragmentContainer? = null
        set(value) {
            d { "set container $value" }
            field = value
            if (awaitViewChange != null) {
                value?.childInterceptor = childInterceptor
            }
        }

    /**
     * Whether or not this router has root [Fragment]
     */
    val hasRootFragment get() = _backstack.size > 0

    /**
     * The current backstack of this router
     */
    val backstack get() = _backstack.toList()
    private val _backstack = mutableListOf<RouterTransaction>()

    private val changeManager = ChangeManager()

    private val changeListeners = mutableListOf<FragmentChangeListener>()

    private var awaitViewChange: PendingChange? = null

    private val childInterceptor = object : FragmentContainer.ChildInterceptor {

        override fun onAddView(view: View) {
            this@Router.d { "on add view $view" }
            awaitViewChange!!.toView = view
            executePendingChange()
        }

        override fun onRemoveView(view: View) {
            this@Router.d { "on remove view $view" }
            awaitViewChange!!.fromView = view
            executePendingChange()
        }

    }

    /**
     * Adds the [listener]
     */
    fun addChangeListener(listener: FragmentChangeListener) {
        changeListeners.add(listener)
    }

    /**
     * Removes the previously added [listener]
     */
    fun removeChangeListener(listener: FragmentChangeListener) {
        changeListeners.remove(listener)
    }

    internal fun restoreInstanceState(savedInstanceState: Bundle)  {
        _backstack.clear()
        _backstack.addAll(
            savedInstanceState.getParcelableArrayList<Bundle>(KEY_BACKSTACK)!!
                .map { RouterTransaction.fromBundle(it, fm) }
        )
    }

    internal fun saveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList(
            KEY_BACKSTACK,
            ArrayList(_backstack.map { it.saveInstanceState() }))

        _backstack.forEach { changeManager.completeChangeImmediately(it.fragment.who) }
    }

    /**
     * Returns whether or not the top fragment was popped
     * Callers should finish the activity or hide the container when this returns false
     */
    fun handleBack(): Boolean {
        if (_backstack.size > 1) {
            if (popCurrentFragment()) {
                return true
            }
        }

        return false
    }

    /**
     * Returns the hosted fragment that was pushed with the given tag or `null` if no
     * such fragment exists in this Router.
     */
    fun findFragmentByTag(tag: String) =
        backstack.firstOrNull { it.tag == tag }?.fragment

    /**
     * Pushes a new [Fragment] to the backstack
     */
    fun pushFragment(transaction: RouterTransaction) {
        val from = _backstack.lastOrNull()
        pushToBackstack(transaction)
        performFragmentChange(transaction, from, true)
    }

    /**
     * Replaces this Router's top [Fragment] with a new [Fragment]
     */
    fun replaceTopFragment(transaction: RouterTransaction) {
        val topTransaction = _backstack.lastOrNull()

        if (topTransaction != null) {
            _backstack.removeAt(_backstack.lastIndex)
        }

        pushToBackstack(transaction)

        performFragmentChange(
            transaction,
            topTransaction,
            true
        )
    }

    /**
     * Pops the passed [Fragment] from the backstack
     */
    fun popFragment(
        fragment: Fragment,
        changeHandler: FragmentChangeHandler? = null
    ): Boolean {
        val topTransaction = _backstack.lastOrNull()

        val poppingTopFragment =
            topTransaction != null && topTransaction.fragment == fragment

        if (poppingTopFragment) {
            _backstack.removeAt(_backstack.lastIndex)
            if (changeHandler != null) {
                performFragmentChange(_backstack.lastOrNull(), topTransaction, false, changeHandler)
            } else {
                performFragmentChange(_backstack.lastOrNull(), topTransaction, false)
            }
        } else if (topTransaction != null) {
            val index = _backstack.indexOfFirst { it.fragment == fragment }
            if (index != -1) {
                val transaction = _backstack.removeAt(index)
                fm.beginTransaction().apply {
                    remove(transaction.fragment)
                    commitNow()
                }
            }
        }

        return _backstack.isNotEmpty()
    }

    /**
     * Pops the top [Fragment] from the backstack
     */
    fun popCurrentFragment(changeHandler: FragmentChangeHandler? = null): Boolean {
        val transaction = _backstack.lastOrNull()
            ?: throw IllegalStateException("Trying to pop the current fragment when there are none on the backstack.")
        return popFragment(transaction.fragment, changeHandler)
    }

    /**
     * Pops all [Fragment]'s until only the root is left
     */
    fun popToRoot(changeHandler: FragmentChangeHandler? = null): Boolean {
        val rootTransaction = _backstack.firstOrNull()
        return if (rootTransaction != null) {
            popToTransaction(rootTransaction, changeHandler)
            true
        } else {
            false
        }
    }

    /**
     * Pops all [Fragment]s until the [Fragment] with the passed tag is at the top
     */
    fun popToTag(tag: String, changeHandler: FragmentChangeHandler? = null): Boolean {
        val transaction = _backstack.firstOrNull { it.tag == tag } ?: return false
        popToTransaction(transaction, changeHandler)
        return true
    }

    /**
     * Pops all [Fragment]s until the [fragment] is at the top
     */
    fun popToFragment(
        fragment: Fragment,
        changeHandler: FragmentChangeHandler? = null
    ): Boolean {
        val transaction =
            _backstack.firstOrNull { it.fragment == fragment } ?: return false
        popToTransaction(transaction, changeHandler)
        return true
    }

    /**
     * Sets the root fragment. If any [Fragment]s are currently in the backstack, they will be removed.
     */
    fun setRoot(transaction: RouterTransaction) {
        setBackstack(listOf(transaction), transaction.pushChangeHandler)
    }

    /**
     * Sets the backstack, transitioning from the current top fragment to the top of the new stack (if different)
     * using the passed [FragmentChangeHandler]
     */
    fun setBackstack(
        newBackstack: List<RouterTransaction>,
        changeHandler: FragmentChangeHandler? = null
    ) {
        val newBackstack = newBackstack.toMutableList()
        val oldBackstack = _backstack.toMutableList()

        d { "set backstack" }

        d { "new backstack $newBackstack" }
        d { "old backstack $oldBackstack" }

        // noop
        if (backstacksAreEqual(newBackstack, oldBackstack)) {
            d { "backstacks are equal" }
            return
        }

        ensureNoDuplicateFragments(newBackstack)

        val oldTopTransaction = oldBackstack.lastOrNull()

        d { "old top transaction $oldTopTransaction" }

        _backstack.clear()
        newBackstack.forEach { it.attachedToRouter = true }
        _backstack.addAll(newBackstack)

        if (oldTopTransaction != null) {
            oldBackstack.removeAt(oldBackstack.lastIndex)
        }

        val newTopTransaction = newBackstack.lastOrNull()

        d { "new top transaction $newTopTransaction" }

        if (newTopTransaction != null) {
            newBackstack.removeAt(newBackstack.lastIndex)
        }

        fm.beginTransaction().apply {
            // remove all old non visible transactions
            oldBackstack
                .filter { !newBackstack.contains(it) }
                .forEach {
                    this@Router.d { "remove old entry $it" }
                    remove(it.fragment)
                }

            var addedNewFragment = false

            // add all new non visible transactions and hide them
            newBackstack
                .filter { !oldBackstack.contains(it) && it != oldTopTransaction }
                .forEach {
                    this@Router.d { "add new entry $it" }
                    addedNewFragment = true
                    add(containerId, it.fragment)
                    detach(it.fragment)
                }

            // bring the old top transaction back to the front
            if (oldTopTransaction != null && addedNewFragment) {
                hide(oldTopTransaction.fragment)
                show(oldTopTransaction.fragment)
                this@Router.d { "bring old top to front" }
            }

            commitNow()
        }

        // finally push the new top transaction
        if (newTopTransaction != null && oldTopTransaction != newTopTransaction) {
            d { "new top not null and changed" }

            val isPush = oldBackstack.isEmpty()
                    || !oldBackstack.contains(newTopTransaction)

            d { "replace old top with new top.. is push? $isPush" }

            // finally to the actual transition from old top to new top
            performFragmentChange(newTopTransaction,
                oldTopTransaction, isPush, changeHandler)
        }
    }

    private fun performFragmentChange(
        to: RouterTransaction?,
        from: RouterTransaction?,
        isPush: Boolean
    ) {
        val changeHandler = when {
            isPush -> to?.pushChangeHandler
            from != null -> from.popChangeHandler
            else -> null
        }

        performFragmentChange(to, from, isPush, changeHandler)
    }

    private fun performFragmentChange(
        to: RouterTransaction?,
        from: RouterTransaction?,
        isPush: Boolean,
        changeHandler: FragmentChangeHandler?
    ) {
        var changeHandler = changeHandler

        val fragmentTransaction = fm.beginTransaction()

        fragmentTransaction.setReorderingAllowed(true)

        if (from != null) {
            // do not allow child fragment views to detach
            // while animating out
            (from.fragment.view as? ViewGroup)?.let {
                forEachOf(
                    it,
                    FragmentContainer::class
                ) { it.lockedDown = true }
            }

            // detach or remove the old fragment
            if (from.fragment.isAdded) {
                val isInBackstack = _backstack.contains(from)

                if (isInBackstack) {
                    fragmentTransaction.detach(from.fragment)
                } else {
                    fragmentTransaction.remove(from.fragment)
                }
            }
        }

        // attach or add the new fragment
        if (to != null) {
            if (to.fragment.isDetached) {
                fragmentTransaction.attach(to.fragment)
            } else {
                fragmentTransaction.add(containerId, to.fragment)
            }
        }

        // do not animate the last view because it would look strange
        if (_backstack.size == 0) {
            changeHandler = NoOpChangeHandler()
        }

        awaitViewChange = PendingChange(from?.fragment, to?.fragment, isPush, changeHandler)

        container?.childInterceptor = childInterceptor

        fragmentTransaction.commitNow()
    }

    private fun popToTransaction(
        transaction: RouterTransaction,
        changeHandler: FragmentChangeHandler? = null
    ) {
        if (_backstack.isNotEmpty()) {
            val topTransaction = _backstack.last()

            val updatedBackstack = mutableListOf<RouterTransaction>()

            for (existingTransaction in _backstack) {
                updatedBackstack.add(existingTransaction)
                if (existingTransaction == transaction) {
                    break
                }
            }

            setBackstack(updatedBackstack, changeHandler ?: topTransaction.popChangeHandler)
        }
    }

    private fun pushToBackstack(entry: RouterTransaction) {
        if (_backstack.any { it.fragment == entry.fragment }) {
            throw IllegalStateException("Trying to push a fragment that already exists on the backstack.")
        }

        entry.attachedToRouter = true
        _backstack.add(entry)
    }

    private fun executePendingChange() {
        d { "execute pending change $awaitViewChange" }
        val change = awaitViewChange ?: return

        val fromReady = (change.needsFrom && change.fromView != null) || !change.needsFrom
        val toReady = (change.needsTo && change.toView != null) || !change.needsTo

        d { "from ready $fromReady, to ready $toReady" }

        if (fromReady && toReady) {
            awaitViewChange = null
            container!!.childInterceptor = null

            changeManager.executeChange(ChangeTransaction(
                if (change.needsTo) {
                    FragmentWithView(change.to!!, change.toView!!)
                } else {
                    null
                },
                if (change.needsFrom) {
                    FragmentWithView(change.from!!, change.fromView!!)
                } else {
                    null
                },
                change.isPush,
                container!!,
                change.changeHandler,
                changeListeners.toList()
            ))
        }
    }

    private fun backstacksAreEqual(
        lhs: List<RouterTransaction>,
        rhs: List<RouterTransaction>
    ): Boolean {
        if (lhs.size != rhs.size) {
            return false
        }

        for (i in rhs.indices) {
            if (rhs[i].fragment != lhs[i].fragment) {
                return false
            }
        }

        return true
    }

    private fun ensureNoDuplicateFragments(backstack: List<RouterTransaction>) {
        if (backstack.size != backstack.distinctBy { it.fragment }.size) {
            throw IllegalStateException("Trying to push the same fragment to the backstack more than once.")
        }
    }

    private data class PendingChange(
        val from: Fragment?,
        val to: Fragment?,
        val isPush: Boolean,
        val changeHandler: FragmentChangeHandler?
    ) {
        val needsFrom get() = from != null && from.view != null
        val needsTo get() = to != null

        var fromView: View? = null
        var toView: View? = null
    }

    private companion object {
        private const val KEY_BACKSTACK = "Router.backstack"
    }
}
package com.ivianuu.fragmentchangehandlers.app

/**
import androidx.fragment.app.Fragment
import com.ivianuu.fragmentchangehandlers.lib.Router
import com.ivianuu.fragmentchangehandlers.lib.RouterTransaction
import com.ivianuu.traveler.Back
import com.ivianuu.traveler.BackTo
import com.ivianuu.traveler.Command
import com.ivianuu.traveler.Forward
import com.ivianuu.traveler.Replace
import com.ivianuu.traveler.common.ResultNavigator
import com.ivianuu.traveler.fragment.FragmentKey

/**
 * @author Manuel Wrage (IVIanuu)
 */
open class FragmentRouterNavigator(private val router: Router) : ResultNavigator() {

    override fun applyCommandWithResult(command: Command): Boolean {
        return when (command) {
            is Forward -> forward(command)
            is Replace -> replace(command)
            is Back -> back(command)
            is BackTo -> backTo(command)
            else -> unsupportedCommand(command)
        }
    }

    protected open fun forward(command: Forward): Boolean {
        val fragment = createFragment(command.key, command.data)
            ?: return unknownScreen(command.key)

        val tag = getFragmentTag(command.key)

        val transaction = RouterTransaction(fragment, tag)

        router.pushFragment(transaction)

        return true
    }

    protected open fun replace(command: Replace): Boolean {
        val fragment = createFragment(command.key, command.data)
            ?: return unknownScreen(command.key)

        val tag = getFragmentTag(command.key)

        val transaction = RouterTransaction(fragment, tag)

        router.replaceTopFragment(transaction)

        return true
    }

    protected open fun back(command: Back) =
        router.handleBack() || exit()

    protected open fun backTo(command: BackTo): Boolean {
        val key = command.key

        return if (key == null) {
            backToRoot()
        } else {
            val tag = getFragmentTag(key)

            if (router.backstack.any { it.tag == tag }) {
                router.popToTag(tag)
                true
            } else {
                backToUnexisting(key)
            }
        }
    }

    protected open fun backToRoot(): Boolean {
        router.popToRoot()
        return true
    }

    protected open fun backToUnexisting(key: Any): Boolean {
        backToRoot()
        return true
    }

    /**
     * Will be called when the backstack is empty and the hosting activity should be closed
     * This is a no op by default
     */
    protected open fun exit() = true

    /**
     * Creates the corresponding [Fragment] for [key]
     */
    protected open fun createFragment(key: Any, data: Any?): Fragment? {
        return when (key) {
            is FragmentKey -> key.createFragment(data)
            else -> null
        }
    }

    /**
     * Returns the corresponding fragment tag for [key]
     */
    protected open fun getFragmentTag(key: Any) = when (key) {
        is FragmentKey -> key.getFragmentTag()
        else -> key.toString()
    }

    /**
     * Setup the transaction
     */
    protected open fun setupTransaction(
        command: Command,
        currentFragment: Fragment?,
        nextFragment: Fragment,
        transaction: RouterTransaction
    ) {
        val key = when (command) {
            is Forward -> command.key
            is Replace -> command.key
            else -> null
        } as? FragmentKey ?: return

     //   key.setupFragmentTransaction(command, currentFragment, nextFragment, transaction)
    }

    /**
     * Will be called when a unknown screen was requested
     */
    protected open fun unknownScreen(key: Any) = false

    /**
     * Will be called when a unsupported command was send
     */
    protected open fun unsupportedCommand(command: Command) = false

}*/
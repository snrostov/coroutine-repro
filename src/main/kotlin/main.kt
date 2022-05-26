import kotlinx.coroutines.CopyableThreadContextElement
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

val lastId = AtomicInteger()

val currentCoroutine: ThreadLocal<CurrentThread?> = object : ThreadLocal<CurrentThread?>() {
    override fun initialValue(): CurrentThread? = null
}

@OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
class CurrentThread(
    private val coroutineId: Int,
    val parentCoroutineId: Int = -1,
) : CopyableThreadContextElement<Unit>,
    AbstractCoroutineContextElement(Key) {

    companion object Key : CoroutineContext.Key<CurrentThread>

    override fun copyForChild(): CopyableThreadContextElement<Unit> =
        CurrentThread(lastId.incrementAndGet())

    override fun mergeForChild(overwritingElement: CoroutineContext.Element): CoroutineContext =
        CurrentThread(lastId.incrementAndGet())

    override fun updateThreadContext(context: CoroutineContext) {
        check(currentCoroutine.get() == null) // this check is failed
        currentCoroutine.set(this)
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: Unit) {
        currentCoroutine.set(null)
    }
}

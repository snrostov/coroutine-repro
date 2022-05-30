import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

private val lastId = AtomicInteger()

private val currentCoroutine: ThreadLocal<CoroutineCurrentThread?> = object : ThreadLocal<CoroutineCurrentThread?>() {
    override fun initialValue(): CoroutineCurrentThread? = null
}

@OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
class CoroutineCurrentThread(val coroutineId: Int) : CopyableThreadContextElement<Unit>,
    AbstractCoroutineContextElement(Key) {

    var i = 0

    override fun copyForChild(): CopyableThreadContextElement<Unit> =
        newTrackedCoroutine()

    override fun mergeForChild(overwritingElement: CoroutineContext.Element): CoroutineContext =
        newTrackedCoroutine()

    override fun updateThreadContext(context: CoroutineContext) {
//        check(currentCoroutine.get() == null) // this check is failed
        currentCoroutine.set(this)
        i++
        println("thread ${Thread.currentThread().id}: coroutine $coroutineId executing $i")
    }

    override fun restoreThreadContext(context: CoroutineContext, oldState: Unit) {
        currentCoroutine.set(null)
        println("thread ${Thread.currentThread().id}: coroutine $coroutineId suspended $i")
    }

    override fun toString(): String {
        return "CurrentThread(coroutineId=$coroutineId)"
    }

    companion object Key : CoroutineContext.Key<CoroutineCurrentThread>
}

fun newTrackedCoroutine() =
    CoroutineCurrentThread(lastId.incrementAndGet())

fun printState(str: String) {
    val coroutine = currentCoroutine.get()
    println("$str (thread ${Thread.currentThread().id}, coroutine-${coroutine?.coroutineId}, run ${coroutine?.i})")
}

fun main() {
    runBlocking(newTrackedCoroutine() + Dispatchers.Unconfined) {
        (1..2).map {
            launch {
                printState("$it: A")
                delay(10)
                printState("$it: B")
                runBlocking(Dispatchers.Unconfined) {
                    printState("$it: C")
                    delay(10)
                    printState("$it: D")
                }
                printState("$it: E")
                delay(10)
                printState("$it: F")
            }
        }.forEach {
            it.join()
            printState("$it: G")
        }
    }
}

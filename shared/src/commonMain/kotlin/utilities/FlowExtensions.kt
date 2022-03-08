package utilities

import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch

fun Flow<Boolean>.filterTrue(): Flow<Unit> {
    return filter { it }.map {}
}

fun Flow<Boolean>.filterFalse(): Flow<Unit> {
    return filterNot { it }.map {}
}

// https://github.com/Kotlin/kotlinx.coroutines/issues/1446#issuecomment-625244176
fun <T> Flow<T>.throttleFirst(windowDuration: Long): Flow<T> {
    var job: Job = Job().apply { complete() }

    return onCompletion { job.cancel() }.run {
        flow {
            coroutineScope {
                collect { value ->
                    if (!job.isActive) {
                        emit(value)
                        job = launch { delay(windowDuration) }
                    }
                }
            }
        }
    }
}

fun <T> Flow<Result<T>>.getOrCatch(action: suspend FlowCollector<T>.(Throwable) -> Unit): Flow<T> {
    return map { it.getOrThrow() }.catch(action)
}
package archives.tater.lockedloaded.util

import com.mojang.serialization.*
import java.util.stream.Stream

class RecursiveMapCodec<A>(private val name: String, wrapped: (MapCodec<A>) -> MapCodec<A>) : MapCodec<A>() {
    private val wrapped by lazy { wrapped(this) }

    override fun <T> encode(input: A, ops: DynamicOps<T>, prefix: RecordBuilder<T>): RecordBuilder<T> =
        wrapped.encode(input, ops, prefix)

    override fun <T> decode(ops: DynamicOps<T>, input: MapLike<T>): DataResult<A> =
        wrapped.decode(ops, input)

    override fun <T> keys(ops: DynamicOps<T>): Stream<T> = wrapped.keys(ops)

    override fun toString(): String = "RecursiveMapCodec[$name]"
}

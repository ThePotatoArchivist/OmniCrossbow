package archives.tater.omnicrossbow.datagen

import net.minecraft.data.tags.TagAppender
import net.minecraft.tags.TagKey

context(appender: TagAppender<E, *>)
operator fun <E: Any> E.unaryPlus() {
    appender.add(this)
}

context(appender: TagAppender<*, T>)
operator fun <T: Any> TagKey<T>.unaryPlus() {
    appender.addTag(this)
}

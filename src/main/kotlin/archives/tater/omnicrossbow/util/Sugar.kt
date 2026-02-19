@file:Suppress("UnstableApiUsage")

package archives.tater.omnicrossbow.util

import net.fabricmc.fabric.api.attachment.v1.AttachmentTarget
import net.fabricmc.fabric.api.attachment.v1.AttachmentType
import com.mojang.serialization.Codec
import net.minecraft.advancements.criterion.DataComponentMatchers
import net.minecraft.advancements.criterion.EntityPredicate
import net.minecraft.advancements.criterion.ItemPredicate
import net.minecraft.core.BlockPos
import net.minecraft.core.HolderSet
import net.minecraft.core.component.DataComponentHolder
import net.minecraft.core.component.DataComponentType
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializer
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.TagKey
import net.minecraft.util.ExtraCodecs
import net.minecraft.util.context.ContextKey
import net.minecraft.util.context.ContextKeySet
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityReference
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.Property
import net.minecraft.world.level.material.Fluid
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.storage.loot.LootContext
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.phys.Vec3
import java.util.*
import kotlin.jvm.optionals.getOrNull
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

operator fun HolderSet<Item>.contains(stack: ItemStack) = stack.`is`(this)
infix fun BlockState.isOf(block: Block) = `is`(block)
infix fun FluidState.isIn(tag: TagKey<Fluid>) = `is`(tag)
infix fun ItemStack.isIn(tag: TagKey<Item>) = `is`(tag)
infix fun Entity.isIn(tag: TagKey<EntityType<*>>) = type.`is`(tag)

operator fun Vec3.plus(other: Vec3): Vec3 = add(other)
operator fun Vec3.minus(other: Vec3): Vec3 = subtract(other)
operator fun Vec3.times(scale: Double): Vec3 = scale(scale)
operator fun Vec3.unaryMinus(): Vec3 = reverse()
operator fun Vec3.times(other: Vec3) = dot(other)

fun ItemPredicate(init: ItemPredicate.Builder.() -> Unit): ItemPredicate = ItemPredicate.Builder.item().apply(init).build()
fun itemPredicateBuilder(init: ItemPredicate.Builder.() -> Unit): ItemPredicate.Builder = ItemPredicate.Builder.item().apply(init)

fun ItemPredicate.Builder.withComponents(init: DataComponentMatchers.Builder.() -> Unit) {
    withComponents(DataComponentMatchers.Builder.components().apply(init).build())
}

fun DataComponentMatchers.Builder.hasAny(type: DataComponentType<*>) {
    any<DataComponentType<*>>(type)
}

fun EntityPredicate(init: EntityPredicate.Builder.() -> Unit): EntityPredicate = EntityPredicate.Builder.entity().apply(init).build()

fun LootContext(level: ServerLevel, contextKeySet: ContextKeySet, init: LootParams.Builder.() -> Unit): LootContext =
    LootParams.Builder(level)
        .apply(init)
        .create(contextKeySet)
        .let { LootContext.Builder(it) }
        .create(Optional.empty())

fun ContextKeySet(init: ContextKeySet.Builder.() -> Unit): ContextKeySet = ContextKeySet.Builder().apply(init).build()

operator fun <T: Any> LootContext.get(key: ContextKey<T>): T = getParameter(key)

typealias McUnit = net.minecraft.util.Unit

fun <T, U> ifNotNull(value: T?, transform: (T) -> U) = value?.let(transform)
infix fun <T> T?.orElse(value: () -> T) = this ?: value()

fun <T: Any> Codec<T>.singleOrList(): Codec<List<T>> = ExtraCodecs.compactListCodec(this)

operator fun DataComponentHolder.contains(type: DataComponentType<*>) = has(type)

operator fun BlockGetter.get(pos: BlockPos): BlockState = getBlockState(pos)
operator fun Level.set(pos: BlockPos, state: BlockState) = setBlockAndUpdate(pos, state)

operator fun <T: Any> AttachmentTarget.get(type: AttachmentType<T>): T? = getAttached(type)
operator fun <T: Any> AttachmentTarget.set(type: AttachmentType<T>, value: T) {
    setAttached(type, value)
}
operator fun <T: Any> AttachmentTarget.contains(type: AttachmentType<T>) = hasAttached(type)

operator fun <T: Comparable<T>> BlockState.get(property: Property<T>): T = getValue(property)

operator fun <T: Any> EntityDataAccessor<T>.getValue(thisRef: Entity, property: KProperty<*>): T = thisRef.entityData[this]
operator fun <T: Any> EntityDataAccessor<T>.setValue(thisRef: Entity, property: KProperty<*>, value: T) {
    thisRef.entityData[this] = value
}

@JvmName("getValueOptional")
operator fun <T: Any> EntityDataAccessor<Optional<T>>.getValue(thisRef: Entity, property: KProperty<*>): T? = thisRef.entityData[this].getOrNull()
@JvmName("setValueOptional")
operator fun <T: Any> EntityDataAccessor<Optional<T>>.setValue(thisRef: Entity, property: KProperty<*>, value: T?) {
    thisRef.entityData[this] = Optional.ofNullable(value)
}

inline operator fun <reified E: Entity> KMutableProperty0<EntityReference<E>?>.getValue(thisRef: Entity, property: KProperty<*>): E? =
    get()?.getEntity(thisRef.level(), E::class.java)

inline operator fun <reified E: Entity> KMutableProperty0<EntityReference<E>?>.setValue(thisRef: Entity, property: KProperty<*>, value: E?) {
    set(EntityReference.of(value))
}

inline fun <reified E: Entity, T: Any> defineSynchedEntityData(type: EntityDataSerializer<T>): EntityDataAccessor<T> =
    SynchedEntityData.defineId(E::class.java, type)

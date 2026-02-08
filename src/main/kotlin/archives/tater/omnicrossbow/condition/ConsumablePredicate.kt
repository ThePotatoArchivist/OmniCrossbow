package archives.tater.omnicrossbow.condition

import archives.tater.omnicrossbow.util.singleOrList
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.advancements.criterion.MinMaxBounds
import net.minecraft.core.Holder
import net.minecraft.core.component.DataComponentGetter
import net.minecraft.core.component.DataComponents
import net.minecraft.core.component.predicates.DataComponentPredicate
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.sounds.SoundEvent
import net.minecraft.world.item.ItemUseAnimation
import net.minecraft.world.item.consume_effects.ConsumeEffect
import java.util.*
import kotlin.jvm.optionals.getOrNull

@JvmRecord
data class ConsumablePredicate(
    val animation: Optional<List<ItemUseAnimation>> = Optional.empty(),
    val consumeSeconds: MinMaxBounds.Doubles = MinMaxBounds.Doubles.ANY,
    val sound: Optional<List<Holder<SoundEvent>>> = Optional.empty(),
    val hasConsumeParticles: Optional<Boolean> = Optional.empty(),
    val consumeEffects: Optional<List<ConsumeEffect.Type<*>>> = Optional.empty()
) : DataComponentPredicate {

    constructor(
        animation: List<ItemUseAnimation>? = null,
        consumeSeconds: MinMaxBounds.Doubles = MinMaxBounds.Doubles.ANY,
        sound: List<Holder<SoundEvent>>? = null,
        hasConsumeParticles: Boolean? = null,
        consumeEffects: List<ConsumeEffect.Type<*>>? = null
    ) : this(
        Optional.ofNullable(animation),
        consumeSeconds,
        Optional.ofNullable(sound),
        Optional.ofNullable(hasConsumeParticles),
        Optional.ofNullable(consumeEffects)
    )

    override fun matches(components: DataComponentGetter): Boolean = components[DataComponents.CONSUMABLE]?.let { consumable ->
        animation.getOrNull()?.contains(consumable.animation) ?: true
                && consumeSeconds.matches(consumable.consumeSeconds.toDouble())
                && sound.getOrNull()?.contains(consumable.sound) ?: true
                && hasConsumeParticles.getOrNull() != !consumable.hasConsumeParticles
                && consumeEffects.getOrNull()?.all { consumable.onConsumeEffects.any { effect -> effect.type == it } } ?: true
    } ?: false

    companion object {
        val CODEC: Codec<ConsumablePredicate> = RecordCodecBuilder.create { it.group(
            ItemUseAnimation.CODEC.singleOrList().optionalFieldOf("animation").forGetter(ConsumablePredicate::animation),
            MinMaxBounds.Doubles.CODEC.optionalFieldOf("consume_seconds", MinMaxBounds.Doubles.ANY).forGetter(ConsumablePredicate::consumeSeconds),
            SoundEvent.CODEC.singleOrList().optionalFieldOf("sound").forGetter(ConsumablePredicate::sound),
            Codec.BOOL.optionalFieldOf("has_consume_particles").forGetter(ConsumablePredicate::hasConsumeParticles),
            BuiltInRegistries.CONSUME_EFFECT_TYPE.byNameCodec().singleOrList().optionalFieldOf("on_consume_effects").forGetter(ConsumablePredicate::consumeEffects)
        ).apply(it, ::ConsumablePredicate) }
    }
}
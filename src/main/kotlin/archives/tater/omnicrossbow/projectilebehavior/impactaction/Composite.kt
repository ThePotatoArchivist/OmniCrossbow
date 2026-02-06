package archives.tater.omnicrossbow.projectilebehavior.impactaction

//abstract class Composite<T: HitResult>(
//    val actions: List<ImpactAction<T>>,
//    override val codec: MapCodec<out Composite<T>>
//) : ImpactAction.Inline<T> {
//
//    class AllOf<T: HitResult>(actions: List<ImpactAction<T>>, codec: MapCodec<out Composite<T>>) :
//        Composite<T>(actions, codec) {
//        override fun tryImpact(
//            level: ServerLevel,
//            projectile: CustomItemProjectile,
//            hit: T,
//            context: LootContext
//        ): Boolean = actions.all { it.tryImpact(level, projectile, hit, context) }
//    }
//
//    companion object {
//        val ALL_OF = object : CompositeType() {
//            override fun <T : HitResult> reduce(actions: List<ImpactAction<T>>): ImpactAction<T> = { level, projectile, hit, context ->
//                actions.all { it.tryImpact(level, projectile, hit, context) }
//            }
//
//            override fun <T : HitResult> createCodec(actionCodec: Codec<ImpactAction<T>>): MapCodec<out ImpactAction.Inline<T>> =
//                RecursiveMapCodec<Composite<T>>("all_of") { codec ->
//                    actionCodec.listOf().fieldOf("actions").xmap({ Composite(it, codec) }, Composite<T>::actions)
//                }
//        }
//
//        val ANY_OF = object : CompositeType() {
//            override fun <T : HitResult> reduce(actions: List<ImpactAction<T>>): ImpactAction<T> = { level, projectile, hit, context ->
//                actions.any { it.tryImpact(level, projectile, hit, context) }
//            }
//        }
//    }
//}
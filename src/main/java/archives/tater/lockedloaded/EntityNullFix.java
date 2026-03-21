package archives.tater.lockedloaded;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

// Workaround for a bug in kotlin 2.3.0, will be fixed in 2.3.20
@NullMarked
public abstract class EntityNullFix extends Entity {
    public EntityNullFix(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }
}

package archives.tater.omnicrossbow.area;

import dev.doublekekse.area_lib.component.AreaDataComponent;
import dev.doublekekse.area_lib.data.AreaSavedData;
import net.minecraft.nbt.NbtCompound;

public class BlocksModifiableComponent implements AreaDataComponent {

    @Override
    public void load(AreaSavedData savedData, NbtCompound compoundTag) {

    }

    @Override
    public NbtCompound save() {
        return new NbtCompound();
    }
}

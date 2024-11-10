package archives.tater.omnicrossbow.util;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class RaycastUtil {
    public static List<Entity> pierce(World world, Vec3d start, Vec3d stop, double margin, @Nullable Entity except, @Nullable Predicate<Entity> predicate) {
        var areaBox = new Box(start, stop).expand(margin);
        return world.getOtherEntities(except, areaBox, entity -> {
            var box = entity.getBoundingBox().expand(margin + entity.getTargetingMargin());
            return (box.contains(start) || box.raycast(start, stop).isPresent()) && (predicate == null || predicate.test(entity));
        });
    }
    public static List<Entity> pierce(World world, Vec3d start, Vec3d stop, double margin, @Nullable Entity except) {return pierce(world, start, stop, margin, except, null);}
    public static List<Entity> pierce(World world, Vec3d start, Vec3d stop, double margin, @Nullable Predicate<Entity> predicate) {return pierce(world, start, stop, margin, null, predicate);}
    public static List<Entity> pierce(World world, Vec3d start, Vec3d stop, double margin) {return pierce(world, start, stop, margin, null, null);}
}

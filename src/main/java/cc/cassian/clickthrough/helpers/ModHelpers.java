package cc.cassian.clickthrough.helpers;

import cc.cassian.clickthrough.ClickThrough;
import cc.cassian.clickthrough.config.ModLists;
import net.minecraft.block.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.item.PaintingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.state.properties.DoubleBlockHalf;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.LockableLootTileEntity;
import net.minecraft.tileentity.SignTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.common.Tags;

import static cc.cassian.clickthrough.ClickThrough.*;

public class ModHelpers {

    static boolean hasBeenToggled = false;

    // Reusable mutable BlockPos to avoid allocation in the painting scan loop
    private static final BlockPos.Mutable MUTABLE_POS = new BlockPos.Mutable();

    public static void handleKeybind() {
        if (TOGGLE_KEY.isDown()) {
            if (!hasBeenToggled) {
                setActive(!CONFIG.isActive);
                hasBeenToggled = true;
            }
        } else {
            hasBeenToggled = false;
        }
    }

    // Cache the result of isShiftKeyDown so we only call it once per right-click
    private static boolean isTaggedAsContainer(BlockState state) {
        // Check block tags first (cheap) before creating an ItemStack (expensive)
        if (state.is(Tags.Blocks.CHESTS)
                || state.is(Tags.Blocks.BARRELS)) {
            return true;
        }
        // Item tag check — only do this if block tags didn't match
        ItemStack stack = state.getBlock().asItem().getDefaultInstance();
        return stack.is(Tags.Items.CHESTS) || stack.is(Tags.Items.BARRELS);
    }

    public static boolean isClickableBlockAt(BlockPos pos, ClientWorld world) {
        if (!CONFIG.onlycontainers) return true;
        BlockState state = world.getBlockState(pos);
        // Check block tags before fetching TileEntity (TileEntity lookup is slower)
        if (isTaggedAsContainer(state) || ModLists.containers.contains(state.getBlock())) return true;
        TileEntity entity = world.getBlockEntity(pos);
        return entity instanceof LockableLootTileEntity;
    }

    private static boolean isDoorOrGate(BlockState state) {
        Block block = state.getBlock();
        return block instanceof DoorBlock || block instanceof FenceGateBlock;
    }

    public static RayTraceResult tryPaintingDoor(PaintingEntity painting, PlayerEntity player, ClientWorld world) {
        if (player.isShiftKeyDown()) return null;

        Direction facing = painting.getDirection();
        Direction intoWall = facing.getOpposite();

        net.minecraft.util.math.AxisAlignedBB bb = painting.getBoundingBox();
        int x1 = (int) Math.floor(bb.minX);
        int y1 = (int) Math.floor(bb.minY);
        int z1 = (int) Math.floor(bb.minZ);
        int x2 = (int) Math.floor(bb.maxX - 1e-7);
        int y2 = (int) Math.floor(bb.maxY - 1e-7);
        int z2 = (int) Math.floor(bb.maxZ - 1e-7);

        int ox = intoWall.getStepX();
        int oy = intoWall.getStepY();
        int oz = intoWall.getStepZ();

        for (int x = x1; x <= x2; x++) {
            for (int y = y1; y <= y2; y++) {
                for (int z = z1; z <= z2; z++) {
                    // Ищем дверь прямо за картиной (1 блок вглубь стены)
                    MUTABLE_POS.set(x + ox, y + oy, z + oz);
                    BlockState state = world.getBlockState(MUTABLE_POS);
                    if (isDoorOrGate(state)) {
                        return buildDoorHit(state, MUTABLE_POS, facing);
                    }
                }
            }
        }
        return null;
    }

    /** Строит хит на нижнюю половину двери/калитки */
    private static BlockRayTraceResult buildDoorHit(BlockState state, BlockPos.Mutable pos, Direction facing) {
        int interactY = (state.getBlock() instanceof DoorBlock
                && state.getValue(DoorBlock.HALF) == DoubleBlockHalf.UPPER)
                ? pos.getY() - 1
                : pos.getY();
        BlockPos interactPos = new BlockPos(pos.getX(), interactY, pos.getZ());
        return new BlockRayTraceResult(Vector3d.atCenterOf(interactPos), facing, interactPos, false);
    }

    public static RayTraceResult switchCrosshairTarget(RayTraceResult crosshairTarget, PlayerEntity player, ClientWorld world) {
        // Fast exit — most common case when mod is inactive
        if (!CONFIG.isActive || crosshairTarget == null) return crosshairTarget;

        ClickThrough.isDyeOnSign = false;

        // Cache shift state once — avoids repeated JNI call into native input layer
        boolean isSneaking = player.isShiftKeyDown();

        if (crosshairTarget.getType() == RayTraceResult.Type.ENTITY) {
            net.minecraft.entity.Entity entity = ((EntityRayTraceResult) crosshairTarget).getEntity();

            // --- PAINTING → DOOR / GATE ---
            if (entity instanceof PaintingEntity) {
                // Pass isSneaking check inside; player ref still needed for future extensions
                if (!isSneaking) {
                    RayTraceResult doorHit = tryPaintingDoor((PaintingEntity) entity, player, world);
                    if (doorHit != null) return doorHit;
                }
            }

            // --- ITEM FRAME → CONTAINER ---
            else if (entity instanceof ItemFrameEntity) {
                if (!isSneaking) {
                    ItemFrameEntity itemFrame = (ItemFrameEntity) entity;
                    Direction dir = itemFrame.getDirection();
                    // Use mutable pos to avoid allocation
                    BlockPos attachedPos = itemFrame.blockPosition().relative(dir.getOpposite());
                    if (isClickableBlockAt(attachedPos, world)) {
                        return new BlockRayTraceResult(
                                crosshairTarget.getLocation(), dir, attachedPos, false);
                    }
                }
            }

        } else if (crosshairTarget instanceof BlockRayTraceResult) {
            BlockRayTraceResult blockHit = (BlockRayTraceResult) crosshairTarget;
            BlockPos blockPos = blockHit.getBlockPos();
            BlockState state = world.getBlockState(blockPos);
            Block block = state.getBlock();

            // --- WALL SIGN → CONTAINER ---
            if (block instanceof WallSignBlock) {
                if (isSneaking) return crosshairTarget; // early exit, no work needed
                BlockPos attachedPos = blockPos.relative(state.getValue(WallSignBlock.FACING).getOpposite());
                if (!isClickableBlockAt(attachedPos, world)) return crosshairTarget;
                // TileEntity fetch only if all other checks passed
                TileEntity te = world.getBlockEntity(blockPos);
                if (!(te instanceof SignTileEntity)) return crosshairTarget;

                Item item = player.getItemInHand(Hand.MAIN_HAND).getItem();
                if (item instanceof DyeItem) {
                    if (CONFIG.sneaktodye) {
                        ClickThrough.isDyeOnSign = true;
                        return new BlockRayTraceResult(
                                crosshairTarget.getLocation(), blockHit.getDirection(), attachedPos, false);
                    }
                } else {
                    return new BlockRayTraceResult(
                            crosshairTarget.getLocation(), blockHit.getDirection(), attachedPos, false);
                }

            // --- WALL BANNER → CONTAINER ---
            } else if (block instanceof WallBannerBlock) {
                if (!isSneaking) {
                    BlockPos attachedPos = blockPos.relative(state.getValue(WallBannerBlock.FACING).getOpposite());
                    if (isClickableBlockAt(attachedPos, world)) {
                        return new BlockRayTraceResult(
                                crosshairTarget.getLocation(), blockHit.getDirection(), attachedPos, false);
                    }
                }
            }
        }

        return crosshairTarget;
    }
}

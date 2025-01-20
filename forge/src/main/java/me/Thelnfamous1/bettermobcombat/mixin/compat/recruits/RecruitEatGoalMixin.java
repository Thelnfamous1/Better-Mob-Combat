package me.Thelnfamous1.bettermobcombat.mixin.compat.recruits;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.entities.ai.RecruitEatGoal;
import me.Thelnfamous1.bettermobcombat.logic.MobAttackHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(RecruitEatGoal.class)
public class RecruitEatGoalMixin {

    @Shadow(remap = false) public AbstractRecruitEntity recruit;
    @Shadow(remap = false) public int slotID;
    @Unique
    public int bettermobcombat$storedMainHandItemSlotId = -1;

    @Inject(method = "start", at = @At(value = "HEAD"))
    private void pre_start(CallbackInfo ci){
        this.bettermobcombat$storedMainHandItemSlotId = -1;
        if(MobAttackHelper.isTwoHandedWielding(this.recruit)){
            ItemStack mainHandItem = this.recruit.getMainHandItem().copy();
            // Try to store the main hand item prior to attempting to eat, to avoid conflicts with Better Combat's dual wielding logic
            // 6 is where the actual inventory begins, as 0-5 correspond to the equipment slots
            for(int i = 6; i < this.recruit.getInventorySize(); ++i) {
                if(i == this.slotID) continue;
                ItemStack stackInSlot = this.recruit.inventory.getItem(i);
                if (stackInSlot.isEmpty()) {
                    this.recruit.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    this.recruit.getInventory().removeItemNoUpdate(this.recruit.getInventorySlotIndex(EquipmentSlot.MAINHAND));
                    this.recruit.getInventory().setItem(i, mainHandItem);
                    this.bettermobcombat$storedMainHandItemSlotId = i;
                    break;
                }
            }
            // We couldn't store the main hand item, so we just drop it
            if(this.bettermobcombat$storedMainHandItemSlotId < 0){
                this.recruit.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                this.recruit.spawnAtLocation(mainHandItem);
            }
        }
    }

    @Inject(method = "stop", at = @At("TAIL"))
    private void post_stop(CallbackInfo ci){
        // If we stored the main hand item, try to re-equip it now that we're done eating
        if(this.bettermobcombat$storedMainHandItemSlotId > -1){
            ItemStack storedMainHandItem = this.recruit.getInventory().getItem(this.bettermobcombat$storedMainHandItemSlotId).copy();
            if(this.recruit.getMainHandItem().isEmpty() && !storedMainHandItem.isEmpty()){
                this.recruit.getInventory().removeItemNoUpdate(this.bettermobcombat$storedMainHandItemSlotId);
                this.recruit.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                this.recruit.setItemSlot(EquipmentSlot.MAINHAND, storedMainHandItem);
                this.recruit.getInventory().setItem(this.recruit.getInventorySlotIndex(EquipmentSlot.MAINHAND), storedMainHandItem);
                Equipable equipable = Equipable.get(storedMainHandItem);
                if (equipable != null) {
                    this.recruit.getCommandSenderWorld().playSound(null, this.recruit.getX(), this.recruit.getY(), this.recruit.getZ(), equipable.getEquipSound(), this.recruit.getSoundSource(), 1.0F, 1.0F);
                }
            }
        }
    }
}

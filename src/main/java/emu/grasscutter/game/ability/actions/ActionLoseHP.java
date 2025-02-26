package emu.grasscutter.game.ability.actions;

import com.google.protobuf.ByteString;
import emu.grasscutter.data.binout.AbilityModifier.AbilityModifierAction;
import emu.grasscutter.game.ability.Ability;
import emu.grasscutter.game.entity.*;
import emu.grasscutter.game.props.FightProperty;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.server.packet.send.PacketServerGlobalValueChangeNotify;

@AbilityAction(AbilityModifierAction.Type.LoseHP)
public final class ActionLoseHP extends AbilityActionHandler {
    @Override
    public boolean execute(
            Ability ability, AbilityModifierAction action, ByteString abilityData, GameEntity target) {
                var owner = ability.getOwner();
                if (owner != null) {
                        Grasscutter.getLogger().debug("Owner: {}", owner);
                        Grasscutter.getLogger().debug("Target: {}", target);
                }
             
                if (owner instanceof EntityClientGadget ownerGadget) {
                    owner = ownerGadget.getScene().getEntityById(ownerGadget.getOwnerEntityId());
        
                     // Caster for EntityClientGadget
            
                    // Handle special gadget cases where the owner needs to be set to the current avatar
                    if (ownerGadget.gadgetId == 41089013 || ownerGadget.gadgetId == 41089012 || ownerGadget.gadgetId == 41089011) {
                        owner = ability.getPlayerOwner().getTeamManager().getCurrentAvatarEntity();
                    }
            
                    // Check if the ability is invulnerable for the owner
                    if (ownerGadget.getOwner().getAbilityManager().isAbilityInvulnerable()) return true;
                }

    

        if (owner == null) {
            owner = findOwnerEntity(ability);
        }   
        String healtag = action.healTag;
        if ("Furina_ElementalArt_LoseHP".equals(healtag)) {
            Grasscutter.getLogger().info("Furina_ElementalArt_LoseHP enabled");
            target.getGlobalAbilityValues().put("Furina_ElementalArt_LoseHP", 1.0f);
            target.getWorld().broadcastPacket(new PacketServerGlobalValueChangeNotify(target, "Furina_ElementalArt_LoseHP", 1.0f));
        }
 
        if (action.enableLockHP && target.isLockHP()) {
            return true;
        }

        if (action.disableWhenLoading
                && target.getScene().getWorld().getHost().getSceneLoadState().getValue() < 2) {
            return true;
        }

        var amountByCasterMaxHPRatio = action.amountByCasterMaxHPRatio.get(ability);
        var amountByCasterAttackRatio = action.amountByCasterAttackRatio.get(ability);
        var amountByCasterCurrentHPRatio = action.amountByCasterCurrentHPRatio.get(ability); // Seems unused on server
        var amountByTargetCurrentHPRatio = action.amountByTargetCurrentHPRatio.get(ability);
        var amountByTargetMaxHPRatio = action.amountByTargetMaxHPRatio.get(ability);
        var limboByTargetMaxHPRatio = action.limboByTargetMaxHPRatio.get(ability);

        var amountToLose = action.amount.get(ability);
        amountToLose +=
                amountByCasterMaxHPRatio * owner.getFightProperty(FightProperty.FIGHT_PROP_MAX_HP);
        amountToLose +=
                amountByCasterAttackRatio * owner.getFightProperty(FightProperty.FIGHT_PROP_CUR_ATTACK);
        amountToLose +=
                amountByCasterCurrentHPRatio * owner.getFightProperty(FightProperty.FIGHT_PROP_CUR_HP);

        var currentHp = target.getFightProperty(FightProperty.FIGHT_PROP_CUR_HP);
        var maxHp = target.getFightProperty(FightProperty.FIGHT_PROP_MAX_HP);
        amountToLose += amountByTargetCurrentHPRatio * currentHp;
        amountToLose += amountByTargetMaxHPRatio * maxHp;

        if (limboByTargetMaxHPRatio > 1.192093e-07)
            amountToLose =
                    (float)
                            Math.min(
                                    Math.max(currentHp - Math.max(limboByTargetMaxHPRatio * maxHp, 1.0), 0.0),
                                    amountToLose);

        if (currentHp < (amountToLose + 0.01) && !action.lethal) amountToLose = 0;
        if (amountToLose == 0) amountToLose = 0.47f * target.getFightProperty(FightProperty.FIGHT_PROP_MAX_HP);

        target.damage(amountToLose);

        return true;
    }
    
    private GameEntity findOwnerEntity(Ability ability) {
        // Get the current owner entity
        GameEntity nextOwner = ability.getPlayerOwner().getScene().getEntityById(16777225);


        // Return the final owner entity once we reach an entity that is not a gadget
        return nextOwner;
    }
}
package emu.grasscutter.game.ability.mixins;

import com.google.protobuf.ByteString;
import emu.grasscutter.data.binout.AbilityMixinData;
import emu.grasscutter.game.ability.Ability;
import emu.grasscutter.game.entity.GameEntity;
import emu.grasscutter.game.managers.stamina.Consumption;
import emu.grasscutter.game.managers.stamina.ConsumptionType;
import emu.grasscutter.server.game.GameSession;
import emu.grasscutter.game.managers.stamina.StaminaManager;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.server.packet.send.PacketServerGlobalValueChangeNotify;
import emu.grasscutter.Grasscutter;

@AbilityMixin(value = AbilityMixinData.Type.CostStaminaMixin)
public class CostStaminaMixin extends AbilityMixinHandler {
    @Override
    public boolean execute(Ability ability, AbilityMixinData mixinData, ByteString abilityData, GameEntity target) {
   float staminaRatio = mixinData.costStaminaDelta.get(ability);
            var player = ability.getPlayerOwner();
            if (player != null && staminaRatio != 0.0f) {
                StaminaManager staminaManager = player.getStaminaManager();
                GameSession session = player.getSession();
                int staminaCost = (int) (staminaRatio * 100);
                Consumption consumption = new Consumption(
                    ConsumptionType.FIGHT, 
                    -Math.abs(staminaCost) // Ensure negative value
                );
                
                staminaManager.updateStaminaRelative(session, consumption, true);
                staminaManager.staminaRecoverDelay = 0;

                // Log for debugging
                Grasscutter.getLogger().info(
                    "MIXIN STAMINA CONSUMPTION: Displayed={}, Internal={}",
                    staminaRatio, staminaCost
                );
            }
        return true;
    }
}
package emu.grasscutter.game.ability.actions;

import com.google.protobuf.ByteString;
import emu.grasscutter.data.binout.AbilityModifier.AbilityModifierAction;
import emu.grasscutter.game.ability.Ability;
import emu.grasscutter.data.common.DynamicFloat;
import emu.grasscutter.game.entity.GameEntity;
import emu.grasscutter.server.packet.send.PacketServerGlobalValueChangeNotify;

@AbilityAction(AbilityModifierAction.Type.AddGlobalValue)
public final class ActionAddGlobalValue extends AbilityActionHandler {
    @Override
    public boolean execute(
            Ability ability, AbilityModifierAction action, ByteString abilityData, GameEntity target) {
        // Get the key and value to add
        String valueKey = action.key;
        float valueToAdd = action.ratio.get(ability);

        // Get the current value from the target's global values
        float currentGlobalValue = target.getGlobalAbilityValues().getOrDefault(valueKey, 1f);

        // Add the specified value to the current global value
        float newValue = currentGlobalValue + valueToAdd;

        // Update the global value in the target's global values
        target.getGlobalAbilityValues().put(valueKey, newValue);

        // Notify the target about the updated global value
        target.onAbilityValueUpdate();

        // Send a value update packet
        target
                .getScene()
                .getHost()
                .sendPacket(new PacketServerGlobalValueChangeNotify(target, valueKey, newValue));

        return true;
    }
}

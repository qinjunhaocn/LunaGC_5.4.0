package emu.grasscutter.game.ability.actions;

import com.google.protobuf.ByteString;
import emu.grasscutter.data.binout.AbilityModifier;
import emu.grasscutter.game.ability.Ability;
import emu.grasscutter.game.entity.GameEntity;
import emu.grasscutter.game.ability.AbilityManager;
import emu.grasscutter.game.ability.AbilityModifierController;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.server.packet.send.PacketServerGlobalValueChangeNotify;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.ability.actions.ActionChangePhlogiston;

@AbilityAction(value = AbilityModifier.AbilityModifierAction.Type.Predicated)
public final class ActionPredicated extends AbilityActionHandler {
    
    @Override
    public boolean execute(Ability ability, AbilityModifier.AbilityModifierAction action, ByteString abilityData, GameEntity target) {
        Grasscutter.getLogger().debug("Predicated action executed for Ability: {}", ability);
        Grasscutter.getLogger().debug("Predicated action details: {}", action);

        // Modify targetPredicates list (adding the necessary keys and values)
        populateTargetPredicates(action, ability);

        // Check predicates first
        boolean predicateSatisfied = checkPredicates(action, target, ability);

        // Execute actions based on predicate check
        if (predicateSatisfied) {
            for (var successAction : action.successActions) {
                this.abilityManager.executeAction(ability, successAction, abilityData, target);
            }
        } else {
            for (var failActions : action.failActions) {
                this.abilityManager.executeAction(ability, failActions, abilityData, target);
            }
        }
        return predicateSatisfied; // Return true if predicates are satisfied, else false
    }

    // Populates the targetPredicates list// Populates the targetPredicates list
    private void populateTargetPredicates(AbilityModifier.AbilityModifierAction action, Ability ability) {
    // Example of adding a predicate
    Map<String, Object> predicate = new HashMap<>();
    predicate.put("$type", AbilityModifier.AbilityModifierAction.Type.ByTargetGlobalValue);
    predicate.put("key", action.key);
    predicate.put("ratio", action.ratio.get(ability));

    // Add this predicate to the list
    action.targetPredicates.add(predicate);
    // Removed unnecessary return statement
    }

    private boolean checkPredicates(AbilityModifier.AbilityModifierAction action, GameEntity target, Ability ability) {
        if (action.targetPredicates == null || action.targetPredicates.isEmpty()) {
            return false;
        }

        for (var predicate : action.targetPredicates) {
            if (predicate.get(AbilityModifier.AbilityModifierAction.Type.ByTargetGlobalValue).equals(AbilityModifier.AbilityModifierAction.Type.ByTargetGlobalValue)) {
            
                String key = (String) predicate.get(action.key);
                float expectedValue = (float) predicate.get(action.ratio.get(ability));

                // Get the current GlobalValue
                float currentValue = (float) target.getGlobalAbilityValues().getOrDefault(key, 0.0f);

                Grasscutter.getLogger().debug("Checking global value: key={}, expected={}, actual={}", key, expectedValue, currentValue);

                if (currentValue >= expectedValue) {
                    return true; // One satisfied predicate is enough to trigger successActions
                }
            }
        }
        return false; // Return false if none of the predicates are satisfied
    }
}
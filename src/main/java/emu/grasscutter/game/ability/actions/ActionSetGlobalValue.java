package emu.grasscutter.game.ability.actions;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Timer;
import java.util.TimerTask;

import com.google.protobuf.ByteString;
import emu.grasscutter.data.binout.AbilityModifier.AbilityModifierAction;
import emu.grasscutter.game.ability.Ability;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.entity.*;
import emu.grasscutter.game.props.FightProperty;
import emu.grasscutter.server.packet.send.PacketServerGlobalValueChangeNotify;

@AbilityAction(AbilityModifierAction.Type.SetGlobalValue)
public final class ActionSetGlobalValue extends AbilityActionHandler {

    private static final AtomicBoolean taskRunning = new AtomicBoolean(false); // To prevent multiple task instances
    private Timer scheduleTimer; // Timer instance for scheduling tasks

    @Override
    public boolean execute(
            Ability ability, AbilityModifierAction action, ByteString abilityData, GameEntity target) {
        Grasscutter.getLogger().info("Set global value" + action.key);
        Grasscutter.getLogger().info("target: " + target);

        // Get the key and value
        var valueKey = action.key;
        var value = action.ratio;

        // Set the global value
        target.getGlobalAbilityValues().put(valueKey, value.get(ability));
        target.onAbilityValueUpdate();

        if ("_ABILITY_ArkheGrade_Attack_CD".equals(valueKey) && taskRunning.compareAndSet(false, true)) {
            // Mark the task as running
            var team = ability.getPlayerOwner().getTeamManager().getActiveTeam(); // Get all avatars in the team

            Runnable damageTask = new Runnable() {
                @Override
                public void run() {
                    // Check if any avatar has more than 50% HP
                    boolean anyAbove50 = team.stream().anyMatch(avatar -> {
                        var maxHp = avatar.getFightProperty(FightProperty.FIGHT_PROP_MAX_HP);
                        var curHp = avatar.getFightProperty(FightProperty.FIGHT_PROP_CUR_HP);
                        return curHp > maxHp * 0.5;
                    });

                    if (anyAbove50) {
                        // Apply the damage with different intervals
                        applyDamageWithDifferentSpeeds(team);
                    } else {
                        // Stop the task if any avatar's HP falls below 50%
                        taskRunning.set(false);
                    }
                }
            };

            // Start the task
            scheduleTask(0, damageTask);
        }

        // Send a value update packet
        target.getScene().getHost().sendPacket(new PacketServerGlobalValueChangeNotify(target, valueKey, value.get(ability)));

        return true;
    }

    private void applyDamageWithDifferentSpeeds(List<EntityAvatar> team) {
        for (EntityAvatar avatar : team) {
            var maxHp = avatar.getFightProperty(FightProperty.FIGHT_PROP_MAX_HP);

            // Apply the 3.6% damage first, with a random delay between 0.5 and 1 second
            float consumeHP = 0.036f * maxHp;
            long delay1 = (long) (500 + Math.random() * 500); // Random delay between 500ms and 1000ms
            scheduleTask(delay1, () -> {
                avatar.damage(consumeHP);
                checkAndContinueDamage(team);
            });

            // Apply 2.4% damage after a longer random delay between 1 and 2 seconds
            float consumeHP2 = 0.024f * maxHp;
            long delay2 = (long) (1000 + Math.random() * 1000); // Random delay between 1000ms and 2000ms
            scheduleTask(delay1 + delay2, () -> {
                avatar.damage(consumeHP2);
                checkAndContinueDamage(team);
            });

            // Apply 1.6% damage after a third longer random delay between 1.5 and 2.5 seconds
            float consumeHP3 = 0.016f * maxHp;
            long delay3 = (long) (1500 + Math.random() * 1000); // Random delay between 1500ms and 2500ms
            scheduleTask(delay1 + delay2 + delay3, () -> {
                avatar.damage(consumeHP3);
                checkAndContinueDamage(team);
            });
        }
    }

    private void checkAndContinueDamage(List<EntityAvatar> team) {
        // Check if any avatar's HP has fallen below 50%
        boolean anyAbove50 = team.stream().anyMatch(avatar -> {
            var maxHp = avatar.getFightProperty(FightProperty.FIGHT_PROP_MAX_HP);
            var curHp = avatar.getFightProperty(FightProperty.FIGHT_PROP_CUR_HP);
            return curHp > maxHp * 0.5;
        });

        if (!anyAbove50) {
            // Stop further damage if any avatar is below 50% HP
            taskRunning.set(false);
        }
    }

    // Function to schedule tasks with delay in milliseconds using Timer
    private void scheduleTask(long delay, Runnable task) {
        if (scheduleTimer == null) {
            scheduleTimer = new Timer(); // Create a new Timer if it doesn't exist
        }

        // Schedule the task to run after the specified delay
        scheduleTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                task.run(); // Execute the task
            }
        }, delay);
    }
}

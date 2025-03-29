package emu.grasscutter.game.ability.actions;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Map;
import emu.grasscutter.net.proto.ChangeHpReasonOuterClass.ChangeHpReason;
import emu.grasscutter.server.packet.send.PacketEntityFightPropChangeReasonNotify;
import emu.grasscutter.server.packet.send.PacketEntityFightPropUpdateNotify;
import emu.grasscutter.net.proto.PropChangeReasonOuterClass.PropChangeReason;
import java.util.HashMap;
import java.util.Random;
import emu.grasscutter.net.proto.PropChangeReasonOuterClass;

import com.google.protobuf.ByteString;
import emu.grasscutter.data.binout.AbilityModifier.AbilityModifierAction;
import emu.grasscutter.game.ability.Ability;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.game.entity.*;
import emu.grasscutter.game.props.FightProperty;
import emu.grasscutter.server.packet.send.PacketServerGlobalValueChangeNotify;

@AbilityAction(AbilityModifierAction.Type.SetGlobalValue)
public final class ActionSetGlobalValue extends AbilityActionHandler {

    private static final float[] DAMAGE_MULTIPLIERS = {0.024f, 0.016f, 0.024f, 0.016f, 0.024f, 0.016f, 0.024f, 0.036f};
    private static final Random RANDOM = new Random(); 

    @Override
    public boolean execute(
            Ability ability, AbilityModifierAction action, ByteString abilityData, GameEntity target) {


        var valueKey = action.key;
        var value = action.ratio;
        target.getGlobalAbilityValues().put(valueKey, value.get(ability));
        target.onAbilityValueUpdate();

        if ("_ABILITY_ArkheGrade_Attack_CD".equals(valueKey)) {
            var team = ability.getPlayerOwner().getTeamManager().getActiveTeam(); float multiplier = DAMAGE_MULTIPLIERS[RANDOM.nextInt(DAMAGE_MULTIPLIERS.length)];
            
            for (EntityAvatar teamMember : team) {
                float curHP = teamMember.getFightProperty(FightProperty.FIGHT_PROP_CUR_HP);
                float maxHP = teamMember.getFightProperty(FightProperty.FIGHT_PROP_MAX_HP);
                float consumeHP = multiplier * teamMember.getFightProperty(FightProperty.FIGHT_PROP_MAX_HP);
                if (curHP >= 0.5f * maxHP) {
                    teamMember.damage(consumeHP);
                    teamMember.getWorld().broadcastPacket(new PacketEntityFightPropChangeReasonNotify(teamMember, FightProperty.FIGHT_PROP_CUR_HP, -consumeHP, PropChangeReasonOuterClass.PropChangeReason.PROP_CHANGE_REASON_ABILITY, ChangeHpReason.CHANGE_HP_SUB_ABILITY));
                }
            }
       }
        target.getScene().getHost().sendPacket(new PacketServerGlobalValueChangeNotify(target, valueKey, value.get(ability)));

        return true;
    } 
}

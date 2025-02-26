package emu.grasscutter.game.ability.actions;

import com.google.protobuf.ByteString;
import emu.grasscutter.data.binout.AbilityModifier;
import emu.grasscutter.data.binout.AbilityMixinData;
import emu.grasscutter.game.ability.Ability;
import emu.grasscutter.game.avatar.Avatar;
import emu.grasscutter.game.entity.EntityAvatar;
import emu.grasscutter.game.entity.GameEntity;
import emu.grasscutter.game.entity.EntityWeapon;
import emu.grasscutter.game.props.FightProperty;
import emu.grasscutter.net.proto.ChangeHpDebtsReasonOuterClass;
import emu.grasscutter.net.proto.PropChangeReasonOuterClass;
import emu.grasscutter.server.packet.send.PacketEntityFightPropChangeReasonNotify;
import emu.grasscutter.server.packet.send.PacketEntityFightPropUpdateNotify;
import emu.grasscutter.Grasscutter;

@AbilityAction(value = AbilityModifier.AbilityModifierAction.Type.AddHPDebts)
public final class ActionAddHPDebts extends AbilityActionHandler {
    @Override
    public boolean execute(Ability ability, AbilityModifier.AbilityModifierAction action, ByteString abilityData, GameEntity target) {
        float debt = action.ratio.get(ability) * target.getFightProperty(FightProperty.FIGHT_PROP_MAX_HP);
        Avatar avatar = ability.getPlayerOwner().getCurrentAvatar();
        float maxValue = action.maxValue.get(ability) * target.getFightProperty(FightProperty.FIGHT_PROP_MAX_HP);
        Grasscutter.getLogger().warn("[ActionAddHPDebts] Called with debt {}", debt);
        Grasscutter.getLogger().warn("[ActionAddHPDebts] Called with avatar {}", avatar.getAvatarId());

        if (!(target instanceof EntityAvatar)) {
            target = ability.getOwner();
        }
        if (target instanceof EntityWeapon) {
            target = ability.getOwner();
        }
        
            float curDebt = target.getFightProperty(FightProperty.FIGHT_PROP_CUR_HP_DEBTS);
            float newDebt = curDebt + debt;
            if (newDebt < 0) {
                newDebt = 0;
            } else if (newDebt > 2 * target.getFightProperty(FightProperty.FIGHT_PROP_MAX_HP)) {
                newDebt = 2 * target.getFightProperty(FightProperty.FIGHT_PROP_MAX_HP);
            } else if (newDebt > maxValue && maxValue > 0) {
                newDebt = maxValue;
            }
            float changeDebt = newDebt - curDebt;
            target.setFightProperty(FightProperty.FIGHT_PROP_CUR_HP_DEBTS, newDebt);
            target.getWorld().broadcastPacket(new PacketEntityFightPropUpdateNotify(target, FightProperty.FIGHT_PROP_CUR_HP_DEBTS));
   
            Grasscutter.getLogger().warn("[ActionAddHPDebts] target {}", target);
            if (changeDebt != 0) {
                if (newDebt == 0) {
                    target.getWorld().broadcastPacket(new PacketEntityFightPropChangeReasonNotify(target, FightProperty.FIGHT_PROP_CUR_HP_DEBTS, changeDebt, PropChangeReasonOuterClass.PropChangeReason.PROP_CHANGE_REASON_ABILITY, ChangeHpDebtsReasonOuterClass.ChangeHpDebtsReason.CHANGE_HP_DEBTS_PAY_FINISH));
                } else if (changeDebt > 0) {
                    target.getWorld().broadcastPacket(new PacketEntityFightPropChangeReasonNotify(target, FightProperty.FIGHT_PROP_CUR_HP_DEBTS, changeDebt, PropChangeReasonOuterClass.PropChangeReason.PROP_CHANGE_REASON_ABILITY, ChangeHpDebtsReasonOuterClass.ChangeHpDebtsReason.CHANGE_HP_DEBTS_ADD_ABILITY));
                } else if (changeDebt < 0) {
                    target.getWorld().broadcastPacket(new PacketEntityFightPropChangeReasonNotify(target, FightProperty.FIGHT_PROP_CUR_HP_DEBTS, changeDebt, PropChangeReasonOuterClass.PropChangeReason.PROP_CHANGE_REASON_ABILITY, ChangeHpDebtsReasonOuterClass.ChangeHpDebtsReason.CHANGE_HP_DEBTS_PAY));
                }
            }
        return true;
    }
}
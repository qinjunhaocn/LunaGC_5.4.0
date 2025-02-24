package emu.grasscutter.data.binout;

import com.google.gson.*;
import emu.grasscutter.data.common.DynamicFloat;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import java.io.Serializable;
import java.lang.constant.DynamicCallSiteDesc;
import java.util.*;

public class AbilityMixinData implements Serializable {
    private static final long serialVersionUID = -2001232313615923575L;

    public enum Type {
        AttachToGadgetStateMixin,
        AttachToStateIDMixin,
        ShieldBarMixin,
        SwitchHealToHPDebtsMixin,
        AttachModifierToGlobalValueMixin,
        DoActionOnGlobalValueChangeMixin,
        CurLocalAvatarMixin,
        NyxCostMixin,
        ModifyDamageMixin,
        AvatarChangeSkillMixin,
        PhlogistonCostMixin,
        AttachModifierToSelfGlobalValueMixin,
        AttachActionToModifierMixin,
        AttachModifierToSelfGlobalValueNoInitMixin,
        HPDebtsMixin,
        LimitHpDebtsByTagMixin,
        TileAttackManagerMixin,
        CostStaminaMixin,
        DoActionByEnergyChangeMixin,
        RejectAttackMixin,
        DoActionByTargetsCountMixin,
        AttachToAbilityStateMixin,
        ModifyBeHitDamageMixin,
        MuteHitEffectMixin,
        EntityInVisibleMixin,
        TriggerPostProcessEffectMixin,
        DoActionByKillingMixin,
        AvatarSteerByCameraMixin,
        ModifyDamageCountMixin,
        OnAvatarUseSkillMixin,
        
    }

    @SerializedName("$type")
    public Type type;

    private JsonElement modifierName;
    public DynamicFloat speed = DynamicFloat.ZERO;
    public DynamicFloat costStaminaDelta = DynamicFloat.ZERO;
    public DynamicFloat ratio = DynamicFloat.ONE;

    public List<String> getModifierNames() {
        if (modifierName.isJsonArray()) {
            java.lang.reflect.Type listType = (new TypeToken<List<String>>() {}).getType();
            List<String> list = (new Gson()).fromJson(modifierName, listType);
            return list;
        } else {
            return Arrays.asList(modifierName.getAsString());
        }
    }
}

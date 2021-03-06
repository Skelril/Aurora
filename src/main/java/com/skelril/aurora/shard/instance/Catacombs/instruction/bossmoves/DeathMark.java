/*
 * Copyright (c) 2014 Wyatt Childers.
 *
 * This file is part of Aurora.
 *
 * Aurora is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Aurora is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public
 * License along with Aurora.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.skelril.aurora.shard.instance.Catacombs.instruction.bossmoves;

import com.skelril.OpenBoss.EntityDetail;
import com.skelril.OpenBoss.InstructionResult;
import com.skelril.OpenBoss.condition.DamagedCondition;
import com.skelril.OpenBoss.instruction.DamagedInstruction;
import com.skelril.aurora.shard.instance.Catacombs.CatacombEntityDetail;
import com.skelril.aurora.shard.instance.Catacombs.CatacombsInstance;
import com.skelril.aurora.util.ChanceUtil;
import com.skelril.aurora.util.ChatUtil;
import com.skelril.aurora.util.extractor.entity.CombatantPair;
import com.skelril.aurora.util.extractor.entity.EDBEExtractor;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Collection;

import static com.skelril.aurora.shard.instance.Catacombs.CatacombEntityDetail.getFrom;

public class DeathMark implements DamagedInstruction {

    private final InstructionResult<DamagedInstruction> next;
    private final int baseActivation;

    public DeathMark() {
        this(30);
    }

    public DeathMark(int baseActivation) {
        this(null, baseActivation);
    }

    public DeathMark(InstructionResult<DamagedInstruction> next, int baseActivation) {
        this.next = next;
        this.baseActivation = baseActivation;
    }

    public boolean activate(EntityDetail detail) {
        return ChanceUtil.getChance(baseActivation - CatacombEntityDetail.getFrom(detail).getWave());
    }

    private EDBEExtractor<Player, LivingEntity, Projectile> extractor = new EDBEExtractor<>(
            Player.class,
            LivingEntity.class,
            Projectile.class
    );

    @Override
    public InstructionResult<DamagedInstruction> process(DamagedCondition condition) {
        CatacombEntityDetail detail = getFrom(condition.getBoss().getDetail());
        EntityDamageEvent event = condition.getEvent();
        if (event instanceof EntityDamageByEntityEvent) {
            CombatantPair<Player, LivingEntity, Projectile> result = extractor.extractFrom((EntityDamageByEntityEvent) event);
            if (result != null) {
                CatacombsInstance inst = CatacombsInstance.getInst(detail);
                Collection<Player> players = inst.getContained(Player.class);
                if (detail.getMarked() != null) {
                    if (result.getAttacker().equals(detail.getMarked())) {
                        ChatUtil.send(players, result.getAttacker().getName() + " has been freed!");
                    } else {
                        detail.getMarked().setHealth(0);
                    }
                    detail.setMarked(null);
                } else if (activate(detail)) {
                    Player marked = result.getAttacker();
                    detail.setMarked(marked);
                    ChatUtil.send(players, ChatColor.DARK_RED, marked.getName() + " has been marked!");
                }
            }
        }
        return next;
    }
}

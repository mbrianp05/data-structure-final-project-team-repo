package entities;

import Tree.BinaryTreeNode;
import map.MineRoom;

public class BossEnemy extends Enemy {
    private final float radiusVisual;
    private final float roarCooldown = 3.0f;
    private float roarTimer = 0f;

    public BossEnemy(BinaryTreeNode<MineRoom> node, float startX, float startY, int baseHp, float baseSpeed, int level) {
        super(node, startX, startY, baseHp, baseSpeed, level);
        this.hp = Math.max(this.hp, baseHp + (level - 1) * 40 + 800);
        this.knockbackResistance = Math.min(0.98f, 0.6f + 0.05f * level);
        try {
            java.lang.reflect.Field f = Enemy.class.getDeclaredField("contactDamage");
            f.setAccessible(true);
            f.setInt(this, Math.max(20, this.contactDamage + level * 8));
        } catch (Exception ex) {
        }
        this.radiusVisual = 28f + level * 6f;

    }

    @Override
    public void update(float dt, Player player, MineRoom room) {

        // NECESARIO para que animTick se incremente
        super.update(dt, player, room);

        if (!isAlive()) {
            return;
        }

        roarTimer -= dt;
        float pursuitSpeed = Math.max(12f, this.speed * 0.6f);

        if (roarTimer <= 0f) {
            float burst = 1.8f + (this.level * 0.05f);
            float dx = player.x - this.getX();
            float dy = player.y - this.getY();
            float d2 = dx * dx + dy * dy;
            if (d2 > 0.0001f) {
                float d = (float) Math.sqrt(d2);
                float nx = dx / d;
                float ny = dy / d;
                this.applyKnockback(nx * burst * 120f * (1f - this.knockbackResistance),
                        ny * burst * 120f * (1f - this.knockbackResistance));
            }
            roarTimer = roarCooldown;
        }

        if (room == null) {
            return;
        }

        float dx = player.x - getX();
        float dy = player.y - getY();
        float dist2 = dx * dx + dy * dy;
        if (dist2 > 0.0001f) {
            float dist = (float) Math.sqrt(dist2);
            float nx = dx / dist;
            float ny = dy / dist;
            translate((nx * pursuitSpeed + vx) * dt, (ny * pursuitSpeed + vy) * dt);
        } else {
            translate(vx * dt, vy * dt);
        }

        vx *= Math.max(0f, 1f - 4f * dt);
        vy *= Math.max(0f, 1f - 4f * dt);

        clampIfNearRoom(room);
    }


    public float getRadiusVisual() {
        return radiusVisual;
    }
}
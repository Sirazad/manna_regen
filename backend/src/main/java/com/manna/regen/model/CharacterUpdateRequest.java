package com.manna.regen.model;

/**
 * Request body for directly updating character base stats.
 */
public class CharacterUpdateRequest {

    private Double level;
    private Integer stamina;
    private MagicWieldingType magicWieldingType;
    private Double maxManna;
    private Double maxPszi;
    private Double currentManna;
    private Double currentPszi;
    private Double magicExhaustion;

    public Double getLevel() { return level; }
    public void setLevel(Double level) { this.level = level; }

    public Integer getStamina() { return stamina; }
    public void setStamina(Integer stamina) { this.stamina = stamina; }

    public MagicWieldingType getMagicWieldingType() { return magicWieldingType; }
    public void setMagicWieldingType(MagicWieldingType magicWieldingType) {
        this.magicWieldingType = magicWieldingType;
    }

    public Double getMaxManna() { return maxManna; }
    public void setMaxManna(Double maxManna) { this.maxManna = maxManna; }

    public Double getMaxPszi() { return maxPszi; }
    public void setMaxPszi(Double maxPszi) { this.maxPszi = maxPszi; }

    public Double getCurrentManna() { return currentManna; }
    public void setCurrentManna(Double currentManna) { this.currentManna = currentManna; }

    public Double getCurrentPszi() { return currentPszi; }
    public void setCurrentPszi(Double currentPszi) { this.currentPszi = currentPszi; }

    public Double getMagicExhaustion() { return magicExhaustion; }
    public void setMagicExhaustion(Double magicExhaustion) { this.magicExhaustion = magicExhaustion; }
}

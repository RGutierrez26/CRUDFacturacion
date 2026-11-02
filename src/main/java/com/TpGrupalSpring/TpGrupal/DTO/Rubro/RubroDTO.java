package com.TpGrupalSpring.TpGrupal.DTO.Rubro;

public record RubroDTO(
        Long id,
        Integer codigo,
        String denominacion
) {
    public static RubroDTO toDTO(com.TpGrupalSpring.TpGrupal.Entities.Dominio.Rubro rubro) {
        if (rubro == null) return null;
        return new RubroDTO(
                rubro.getId(),
                rubro.getCodigo(),
                rubro.getDenominacion()
        );
    }
}

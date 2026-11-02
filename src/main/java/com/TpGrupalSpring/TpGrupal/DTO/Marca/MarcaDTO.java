package com.TpGrupalSpring.TpGrupal.DTO.Marca;

public record MarcaDTO(
        Long id,
        Integer codigo,
        String denominacion
) {
    public static MarcaDTO toDTO(com.TpGrupalSpring.TpGrupal.Entities.Dominio.Marca marca) {
        if (marca == null) return null;
        return new MarcaDTO(
                marca.getId(),
                marca.getCodigo(),
                marca.getDenominacion()
        );
    }
}

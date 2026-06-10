package com.utp.cafeteria.service;

import com.utp.cafeteria.dto.ConfiguracionCafeteriaRequest;
import com.utp.cafeteria.dto.ConfiguracionCafeteriaResponse;
import com.utp.cafeteria.entity.ConfiguracionCafeteria;
import com.utp.cafeteria.exception.ResourceNotFoundException;
import com.utp.cafeteria.repository.ConfiguracionCafeteriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConfiguracionCafeteriaService {

    private final ConfiguracionCafeteriaRepository configRepo;

    public ConfiguracionCafeteriaResponse obtener() {
        return configRepo.findAll().stream()
                .findFirst()
                .map(ConfiguracionCafeteriaResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("ConfiguracionCafeteria", "unica", "no encontrada"));
    }

    @Transactional
    public ConfiguracionCafeteriaResponse guardar(ConfiguracionCafeteriaRequest request) {
        ConfiguracionCafeteria config = configRepo.findAll().stream()
                .findFirst()
                .orElse(new ConfiguracionCafeteria());

        config.setNombreCafeteria(request.getNombreCafeteria());
        config.setLogoUrl(request.getLogoUrl());
        config.setMensajeDelDia(request.getMensajeDelDia());
        config.setHoraApertura(request.getHoraApertura());
        config.setHoraCierre(request.getHoraCierre());
        config.setTelefono(request.getTelefono());
        config.setDireccion(request.getDireccion());
        if (request.getAceptaPedidos() != null) {
            config.setAceptaPedidos(request.getAceptaPedidos());
        }
        return ConfiguracionCafeteriaResponse.from(configRepo.save(config));
    }

    @Transactional
    public ConfiguracionCafeteriaResponse toggleAceptaPedidos() {
        ConfiguracionCafeteria config = configRepo.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("ConfiguracionCafeteria", "unica", "no encontrada"));
        config.setAceptaPedidos(!config.getAceptaPedidos());
        return ConfiguracionCafeteriaResponse.from(configRepo.save(config));
    }
}

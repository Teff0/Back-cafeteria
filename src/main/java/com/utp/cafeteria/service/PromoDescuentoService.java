package com.utp.cafeteria.service;

import com.utp.cafeteria.dto.PromoDescuentoRequest;
import com.utp.cafeteria.dto.PromoDescuentoResponse;
import com.utp.cafeteria.entity.PromoDescuento;
import com.utp.cafeteria.exception.BadRequestException;
import com.utp.cafeteria.exception.ConflictException;
import com.utp.cafeteria.exception.ResourceNotFoundException;
import com.utp.cafeteria.repository.PromoDescuentoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PromoDescuentoService {

    private final PromoDescuentoRepository promoRepository;

    public List<PromoDescuentoResponse> obtenerTodas() {
        return promoRepository.findAll().stream()
                .map(PromoDescuentoResponse::from)
                .toList();
    }

    public List<PromoDescuentoResponse> obtenerActivas() {
        return promoRepository.findByActivoTrue().stream()
                .map(PromoDescuentoResponse::from)
                .toList();
    }

    public PromoDescuentoResponse validarCodigo(String codigo) {
        PromoDescuento promo = promoRepository.findByCodigoPromo(codigo)
                .orElseThrow(() -> new BadRequestException("Codigo de promo invalido"));
        if (!promo.esValido()) {
            throw new BadRequestException("El codigo de promo no es valido o ya expiro");
        }
        return PromoDescuentoResponse.from(promo);
    }

    @Transactional
    public PromoDescuentoResponse crear(PromoDescuentoRequest request) {
        if (promoRepository.findByCodigoPromo(request.getCodigoPromo()).isPresent()) {
            throw new ConflictException("Ya existe una promo con ese codigo");
        }
        PromoDescuento promo = PromoDescuento.builder()
                .codigoPromo(request.getCodigoPromo().toUpperCase())
                .descripcion(request.getDescripcion())
                .tipoDescuento(request.getTipoDescuento())
                .valor(request.getValor())
                .usoMaximo(request.getUsoMaximo())
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .build();
        return PromoDescuentoResponse.from(promoRepository.save(promo));
    }

    @Transactional
    public PromoDescuentoResponse toggleActivo(UUID id) {
        PromoDescuento promo = promoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PromoDescuento", "id", id));
        promo.setActivo(!promo.getActivo());
        return PromoDescuentoResponse.from(promoRepository.save(promo));
    }

    @Transactional
    public void eliminar(UUID id) {
        if (!promoRepository.existsById(id)) {
            throw new ResourceNotFoundException("PromoDescuento", "id", id);
        }
        promoRepository.deleteById(id);
    }
}

package com.utp.cafeteria.service;

import com.utp.cafeteria.dto.*;
import com.utp.cafeteria.entity.*;
import com.utp.cafeteria.exception.*;
import com.utp.cafeteria.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;
    private final PedidoRepository pedidoRepository;

    @Transactional
    public PagoResponse procesarPago(PagoRequest request, UUID usuarioId) {
        Pedido pedido = pedidoRepository.findById(request.getPedidoId())
                .orElseThrow(() -> new ResourceNotFoundException("Pedido", "id", request.getPedidoId()));

        if (!pedido.getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedException("No tiene acceso a este pedido");
        }

        if (pedido.getEstado() != Pedido.Estado.PENDIENTE) {
            throw new BadRequestException("El pedido ya fue procesado o cancelado");
        }

        if (pagoRepository.existsByPedidoId(pedido.getId())) {
            throw new ConflictException("Ya existe un pago para este pedido");
        }

        Pago.MetodoPago metodo;
        try {
            metodo = Pago.MetodoPago.valueOf(request.getMetodoPago().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Método de pago inválido");
        }

        Pago pago = Pago.builder()
                .pedido(pedido)
                .monto(request.getMonto())
                .metodoPago(metodo)
                .estado(Pago.EstadoPago.COMPLETADO)
                .codigoTransaccion("TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .fechaPago(LocalDateTime.now())
                .build();

        pagoRepository.save(pago);

        pedido.setEstado(Pedido.Estado.PAGADO);
        pedidoRepository.save(pedido);

        return mapToResponse(pago);
    }

    public PagoResponse obtenerPagoPorPedido(UUID pedidoId, UUID usuarioId) {
        Pago pago = pagoRepository.findByPedidoId(pedidoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pago", "pedidoId", pedidoId));

        if (!pago.getPedido().getUsuario().getId().equals(usuarioId)) {
            throw new UnauthorizedException("No tiene acceso a este pago");
        }

        return mapToResponse(pago);
    }

    private PagoResponse mapToResponse(Pago pago) {
        return PagoResponse.builder()
                .id(pago.getId())
                .pedidoId(pago.getPedido().getId())
                .monto(pago.getMonto())
                .metodoPago(pago.getMetodoPago())
                .estado(pago.getEstado())
                .codigoTransaccion(pago.getCodigoTransaccion())
                .fechaPago(pago.getFechaPago())
                .createdAt(pago.getCreatedAt())
                .build();
    }
}
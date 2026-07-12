package duoc.pagos.repository;

import duoc.pagos.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    Optional<Pago> findByPedidoId(Long pedidoId);
    Optional<Pago> findByPedidoIdAndEstado(Long pedidoId, String estado);
}

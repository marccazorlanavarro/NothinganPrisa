
package cat.copernic.nothinganpri.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.format.annotation.DateTimeFormat;

public class CreateReservaRequest {

    // ISO (yyyy-MM-dd): format per a <input type="date">.
    // Evita que les dates quedin buides en tornar de "Calcular preu".
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataInici;

    // Hora de recollida del vehicle.
    @DateTimeFormat(iso = DateTimeFormat.ISO.TIME)
    private LocalTime horaInici;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataFi;

    private Long vehicleId;
    private Long clientId;
    private Double subtotal;
    private Double fianca;
    private Double total;

    // Getters i Setters

    public LocalDate getDataInici() {
        return dataInici;
    }

    public void setDataInici(LocalDate dataInici) {
        this.dataInici = dataInici;
    }

    public LocalTime getHoraInici() {
        return horaInici;
    }

    public void setHoraInici(LocalTime horaInici) {
        this.horaInici = horaInici;
    }

    public LocalDate getDataFi() {
        return dataFi;
    }

    public void setDataFi(LocalDate dataFi) {
        this.dataFi = dataFi;
    }

    public Long getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(Long vehicleId) {
        this.vehicleId = vehicleId;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public Double getFianca() {
        return fianca;
    }

    public void setFianca(Double fianca) {
        this.fianca = fianca;
    }

    public Double getTotal() {
        return total;
    }

    public void setTotal(Double total) {
        this.total = total;
    }
}

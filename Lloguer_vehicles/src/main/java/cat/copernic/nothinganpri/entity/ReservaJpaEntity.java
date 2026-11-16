
package cat.copernic.nothinganpri.entity;

import cat.copernic.nothinganpri.enums.EstatReserva;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "reserves")
public class ReservaJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String codiReserva;

    @ManyToOne
    @JoinColumn(name = "vehicle_id", nullable = false)
    private VehicleJpaEntity vehicle;

    @ManyToOne
    @JoinColumn(name = "usuari_id", nullable = false)
    private UsuariJpaEntity client;

    @Column(nullable = false)
    private LocalDate dataInici;

    // Hora de recollida del vehicle
    @Column
    private LocalTime horaInici;

    @Column(nullable = false)
    private LocalDate dataFi;

    private Double importTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstatReserva estat = EstatReserva.ACTIVA;

    // El client ha sol·licitat l'anul·lació.
    // La reserva continua ACTIVA fins que l'agent decideix.
    @Column(nullable = false)
    private boolean anulacioSolicitada = false;

    // Relación 1:1 con la Entrega del vehículo
    @OneToOne(mappedBy = "reserva", cascade = CascadeType.ALL, orphanRemoval = true)
    private EntregaJpaEntity entrega;

    // --- Constructores ---
    public ReservaJpaEntity() {
    }

    public ReservaJpaEntity(String codiReserva, VehicleJpaEntity vehicle,
            UsuariJpaEntity client, LocalDate dataInici, LocalDate dataFi) {
        this.codiReserva = codiReserva;
        this.vehicle = vehicle;
        this.client = client;
        this.dataInici = dataInici;
        this.dataFi = dataFi;
        this.estat = EstatReserva.ACTIVA;
    }

    public ReservaJpaEntity(String codiReserva, VehicleJpaEntity vehicle,
            UsuariJpaEntity client, LocalDate dataInici,
            LocalDate dataFi, Double importTotal) {
        this.codiReserva = codiReserva;
        this.vehicle = vehicle;
        this.client = client;
        this.dataInici = dataInici;
        this.dataFi = dataFi;
        this.importTotal = importTotal;
        this.estat = EstatReserva.ACTIVA;
    }

    // --- Getters i Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodiReserva() {
        return codiReserva;
    }

    public void setCodiReserva(String codiReserva) {
        this.codiReserva = codiReserva;
    }

    public VehicleJpaEntity getVehicle() {
        return vehicle;
    }

    public void setVehicle(VehicleJpaEntity vehicle) {
        this.vehicle = vehicle;
    }

    public UsuariJpaEntity getClient() {
        return client;
    }

    public void setClient(UsuariJpaEntity client) {
        this.client = client;
    }

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

    public Double getImportTotal() {
        return importTotal;
    }

    public void setImportTotal(Double importTotal) {
        this.importTotal = importTotal;
    }

    public EstatReserva getEstat() {
        if (this.estat == null) {
            return EstatReserva.ACTIVA;
        }
        return this.estat;
    }

    public void setEstat(EstatReserva estat) {
        this.estat = estat;
    }

    public boolean isAnulacioSolicitada() {
        return anulacioSolicitada;
    }

    public void setAnulacioSolicitada(boolean anulacioSolicitada) {
        this.anulacioSolicitada = anulacioSolicitada;
    }

    public EntregaJpaEntity getEntrega() {
        return entrega;
    }

    public void setEntrega(EntregaJpaEntity entrega) {
        this.entrega = entrega;
        if (entrega != null && entrega.getReserva() != this) {
            entrega.setReserva(this);
        }
    }
}

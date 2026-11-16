
package cat.copernic.nothinganpri.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "solicitudes_validacion")
public class SolicitudValidacionJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id", nullable = false)
    private ClientJpaEntity cliente;

    // REGISTRO, DATOS_PERSONALES, DOCUMENTACION o TARJETA
    @Column(nullable = false, length = 40)
    private String tipo;

    // PENDIENTE, APROBADA o RECHAZADA
    @Column(nullable = false, length = 20)
    private String estado = "PENDIENTE";

    // Datos propuestos en formato JSON.
    // No guardar aquí el CVV ni el número completo de la tarjeta.
    @Lob
    @Column(name = "datos_propuestos")
    private String datosPropuestos;

    // Archivos nuevos propuestos, si los hay.
    @Lob
    @Column(name = "documento_dades")
    private byte[] documento;

    @Column(name = "documento_tipus", length = 100)
    private String documentoTipo;

    @Column(name = "documento_nom", length = 255)
    private String documentoNombre;

    @Column(name = "data_solicitud", nullable = false)
    private LocalDateTime dataSolicitud = LocalDateTime.now();

    @Column(name = "data_revisio")
    private LocalDateTime dataRevisio;

    @Column(name = "revisor_email", length = 255)
    private String revisorEmail;

    @Column(name = "motiu_rebuig", length = 1000)
    private String motiuRebuig;

    public SolicitudValidacionJpaEntity() {
    }

    public Long getId() {
        return id;
    }

    public ClientJpaEntity getCliente() {
        return cliente;
    }

    public void setCliente(ClientJpaEntity cliente) {
        this.cliente = cliente;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getDatosPropuestos() {
        return datosPropuestos;
    }

    public void setDatosPropuestos(String datosPropuestos) {
        this.datosPropuestos = datosPropuestos;
    }

    public byte[] getDocumento() {
        return documento;
    }

    public void setDocumento(byte[] documento) {
        this.documento = documento;
    }

    public String getDocumentoTipo() {
        return documentoTipo;
    }

    public void setDocumentoTipo(String documentoTipo) {
        this.documentoTipo = documentoTipo;
    }

    public String getDocumentoNombre() {
        return documentoNombre;
    }

    public void setDocumentoNombre(String documentoNombre) {
        this.documentoNombre = documentoNombre;
    }

    public LocalDateTime getDataSolicitud() {
        return dataSolicitud;
    }

    public void setDataSolicitud(LocalDateTime dataSolicitud) {
        this.dataSolicitud = dataSolicitud;
    }

    public LocalDateTime getDataRevisio() {
        return dataRevisio;
    }

    public void setDataRevisio(LocalDateTime dataRevisio) {
        this.dataRevisio = dataRevisio;
    }

    public String getRevisorEmail() {
        return revisorEmail;
    }

    public void setRevisorEmail(String revisorEmail) {
        this.revisorEmail = revisorEmail;
    }

    public String getMotiuRebuig() {
        return motiuRebuig;
    }

    public void setMotiuRebuig(String motiuRebuig) {
        this.motiuRebuig = motiuRebuig;
    }
}

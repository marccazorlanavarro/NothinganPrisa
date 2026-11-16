package cat.copernic.nothinganpri.entity;

import cat.copernic.nothinganpri.enums.CategoriaVehicle;
import cat.copernic.nothinganpri.enums.TipusCombustible;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entitat JPA d'un vehicle. Només defineix el mapatge a la base de dades:
 * les validacions de les dades es fan a la capa de negoci (VehicleService).
 */
@Entity
@Table(name = "vehicle")
public class VehicleJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String matricula;

    @Column(nullable = false)
    private String marca;

    @Column(nullable = false)
    private String model;

    private Integer anyMatriculacio;

    @Enumerated(EnumType.STRING)
    private CategoriaVehicle categoria;

    @Enumerated(EnumType.STRING)
    private TipusCombustible combustible;

    private String color;

    private Integer potenciaCv;

    private Integer places;

    @Column(precision = 10, scale = 2)
    private BigDecimal preuDia;

    private Integer minDies = 1;

    private Integer maxDies;

    private Integer limitQuilometres;

    @Column(precision = 10, scale = 2)
    private BigDecimal fiancaEstandard;

    private boolean actiu = true;

    private LocalDate dataFiDesactivacio;

    @Column(length = 2000)
    private String comentariPrivat;

    // Foto guardada a la mateixa taula del vehicle
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] foto;

    @Column(length = 50)
    private String fotoContentType;
    
    @Column(name = "data_baixa")
    private LocalDate dataBaixa;           // Per baixa permanent (RF41)

    @Column(name = "motiu_baixa", length = 500)
    private String motiuBaixa;             // Per baixa permanent

    // ---------------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------------

    /**
     * Constructor buit. JPA/Hibernate i Spring MVC (binding del formulari) el necessiten
     * per crear l'objecte i omplir-lo després amb els setters.
     */
    public VehicleJpaEntity() {
    }

    /**
     * Constructor amb les dades obligatòries d'un vehicle. La resta de camps
     * conserven el valor per defecte (minDies = 1, actiu = true) o queden buits.
     */
    public VehicleJpaEntity(String matricula, String marca, String model, Integer anyMatriculacio,
                            CategoriaVehicle categoria, TipusCombustible combustible, Integer places,
                            BigDecimal preuDia, BigDecimal fiancaEstandard) {
        this.matricula = matricula;
        this.marca = marca;
        this.model = model;
        this.anyMatriculacio = anyMatriculacio;
        this.categoria = categoria;
        this.combustible = combustible;
        this.places = places;
        this.preuDia = preuDia;
        this.fiancaEstandard = fiancaEstandard;
    }

    /**
     * Constructor complet, amb totes les dades llevat de l'id i de la foto.
     * Reutilitza el constructor de dades obligatòries per no repetir codi.
     */
    public VehicleJpaEntity(String matricula, String marca, String model, Integer anyMatriculacio,
                            CategoriaVehicle categoria, TipusCombustible combustible, String color,
                            Integer potenciaCv, Integer places, BigDecimal preuDia, Integer minDies,
                            Integer maxDies, Integer limitQuilometres, BigDecimal fiancaEstandard,
                            boolean actiu, LocalDate dataFiDesactivacio, String comentariPrivat) {
        this(matricula, marca, model, anyMatriculacio, categoria, combustible, places, preuDia, fiancaEstandard);
        this.color = color;
        this.potenciaCv = potenciaCv;
        this.minDies = minDies;
        this.maxDies = maxDies;
        this.limitQuilometres = limitQuilometres;
        this.actiu = actiu;
        this.dataFiDesactivacio = dataFiDesactivacio;
        this.comentariPrivat = comentariPrivat;
    }

    /**
     * Constructor totalment complet: inclou l'id i la foto.
     * Útil per reconstruir un vehicle existent (p. ex. en proves o en mapejos).
     * Per a un vehicle nou, passa null com a id perquè el generi la base de dades.
     */
    public VehicleJpaEntity(Long id, String matricula, String marca, String model, Integer anyMatriculacio,
                            CategoriaVehicle categoria, TipusCombustible combustible, String color,
                            Integer potenciaCv, Integer places, BigDecimal preuDia, Integer minDies,
                            Integer maxDies, Integer limitQuilometres, BigDecimal fiancaEstandard,
                            boolean actiu, LocalDate dataFiDesactivacio, String comentariPrivat,
                            byte[] foto, String fotoContentType) {
        this(matricula, marca, model, anyMatriculacio, categoria, combustible, color, potenciaCv, places,
                preuDia, minDies, maxDies, limitQuilometres, fiancaEstandard, actiu, dataFiDesactivacio,
                comentariPrivat);
        this.id = id;
        this.foto = foto;
        this.fotoContentType = fotoContentType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getMarca() {
        return marca;
    }

    public void setMarca(String marca) {
        this.marca = marca;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Integer getAnyMatriculacio() {
        return anyMatriculacio;
    }

    public void setAnyMatriculacio(Integer anyMatriculacio) {
        this.anyMatriculacio = anyMatriculacio;
    }

    public CategoriaVehicle getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaVehicle categoria) {
        this.categoria = categoria;
    }

    public TipusCombustible getCombustible() {
        return combustible;
    }

    public void setCombustible(TipusCombustible combustible) {
        this.combustible = combustible;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Integer getPotenciaCv() {
        return potenciaCv;
    }

    public void setPotenciaCv(Integer potenciaCv) {
        this.potenciaCv = potenciaCv;
    }

    public Integer getPlaces() {
        return places;
    }

    public void setPlaces(Integer places) {
        this.places = places;
    }

    public BigDecimal getPreuDia() {
        return preuDia;
    }

    public void setPreuDia(BigDecimal preuDia) {
        this.preuDia = preuDia;
    }

    public Integer getMinDies() {
        return minDies;
    }

    public void setMinDies(Integer minDies) {
        this.minDies = minDies;
    }

    public Integer getMaxDies() {
        return maxDies;
    }

    public void setMaxDies(Integer maxDies) {
        this.maxDies = maxDies;
    }

    public Integer getLimitQuilometres() {
        return limitQuilometres;
    }

    public void setLimitQuilometres(Integer limitQuilometres) {
        this.limitQuilometres = limitQuilometres;
    }

    public BigDecimal getFiancaEstandard() {
        return fiancaEstandard;
    }

    public void setFiancaEstandard(BigDecimal fiancaEstandard) {
        this.fiancaEstandard = fiancaEstandard;
    }

    public boolean isActiu() {
        return actiu;
    }

    public void setActiu(boolean actiu) {
        this.actiu = actiu;
    }

    public LocalDate getDataFiDesactivacio() {
        return dataFiDesactivacio;
    }

    public void setDataFiDesactivacio(LocalDate dataFiDesactivacio) {
        this.dataFiDesactivacio = dataFiDesactivacio;
    }

    public String getComentariPrivat() {
        return comentariPrivat;
    }

    public void setComentariPrivat(String comentariPrivat) {
        this.comentariPrivat = comentariPrivat;
    }

    public byte[] getFoto() {
        return foto;
    }

    public void setFoto(byte[] foto) {
        this.foto = foto;
    }

    public String getFotoContentType() {
        return fotoContentType;
    }

    public void setFotoContentType(String fotoContentType) {
        this.fotoContentType = fotoContentType;
    }

    /** URL des d'on es serveix la foto, o null si el vehicle no en té. Les plantilles la fan servir. */
    @Transient
    public String getFotoUrl() {
        return (foto != null && id != null) ? "/vehicles/" + id + "/foto" : null;
    }

    public LocalDate getDataBaixa() {
        return dataBaixa;
    }

    public void setDataBaixa(LocalDate dataBaixa) {
        this.dataBaixa = dataBaixa;
    }

    public String getMotiuBaixa() {
        return motiuBaixa;
    }

    public void setMotiuBaixa(String motiuBaixa) {
        this.motiuBaixa = motiuBaixa;
    }
    
}
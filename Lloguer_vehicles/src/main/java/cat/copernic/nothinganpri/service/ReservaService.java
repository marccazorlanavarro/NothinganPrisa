package cat.copernic.nothinganpri.service;

import cat.copernic.nothinganpri.document.HistoricReservaDocument;
import cat.copernic.nothinganpri.entity.ClientJpaEntity;
import cat.copernic.nothinganpri.entity.EntregaJpaEntity;
import cat.copernic.nothinganpri.entity.ReservaJpaEntity;
import cat.copernic.nothinganpri.entity.UsuariJpaEntity;
import cat.copernic.nothinganpri.entity.VehicleJpaEntity;
import cat.copernic.nothinganpri.enums.EstatReserva;
import cat.copernic.nothinganpri.repository.HistoricReservaRepository;
import cat.copernic.nothinganpri.repository.ReservaJpaRepository;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ReservaService {

    private static final Logger log = LoggerFactory.getLogger(ReservaService.class);

    private static final ZoneId ZONA_ESPANYA = ZoneId.of("Europe/Madrid");

    /** Missatge que veu el client quan intenta reservar sense targeta de crèdit. */
    public static final String MISSATGE_SENSE_TARGETA =
            "No pots crear la reserva perquè no tens cap targeta de crèdit registrada.";

    public static final String ACCIO_CREADA = "CREADA";
    public static final String ACCIO_ANULLADA = "ANUL·LADA";

    @Autowired
    private ReservaJpaRepository reservaRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private HistoricReservaRepository historicReservaRepository;

    @Autowired
    private LogQueryService logQueryService;

    public ReservaJpaEntity crearReserva(ReservaJpaEntity novaReserva) throws Exception {
        try {
            if (novaReserva.getDataInici().isAfter(novaReserva.getDataFi())) {
                throw new Exception("La data d'inici no pot ser posterior a la data final.");
            }

            long conflictes = reservaRepository.countConflictingReserves(
                    novaReserva.getVehicle().getId(),
                    novaReserva.getDataInici(),
                    novaReserva.getDataFi()
            );

            if (conflictes > 0) {
                throw new Exception("El vehicle no està disponible en aquestes dates.");
            }

            novaReserva.setCodiReserva(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
            novaReserva.setEstat(EstatReserva.ACTIVA);

            ReservaJpaEntity reserva = reservaRepository.save(novaReserva);

            // Força la càrrega de vehicle i client dins de la sessió
            if (reserva.getVehicle() != null) {
                reserva.getVehicle().getMatricula();
            }
            if (reserva.getClient() != null) {
                reserva.getClient().getEmail();
            }

            // Snapshot a MongoDB (històric no relacional)
            guardarHistoric(reserva, ACCIO_CREADA);

            logQueryService.registrarInfo(
                    "RESERVES",
                    "Reserva creada correctament. ID: " + reserva.getId()
            );

            return reserva;

        } catch (Exception e) {
            if (!(e instanceof RuntimeException)
                    && (e.getMessage() != null)
                    && (e.getMessage().contains("no pot ser posterior")
                    || e.getMessage().contains("no està disponible"))) {
                logQueryService.registrarWarning("RESERVES", e.getMessage());
            } else {
                registrarError("crearReserva", e);
            }
            throw e;
        }
    }

    public ReservaJpaEntity crearReservaClient(ReservaJpaEntity novaReserva) throws Exception {
        if (!teTargeta(novaReserva.getClient())) {
            logQueryService.registrarWarning(
                    "RESERVES",
                    "Intent de crear una reserva sense tenir targeta de crèdit registrada."
            );
            throw new Exception(MISSATGE_SENSE_TARGETA);
        }

        return crearReserva(novaReserva);
    }

    public boolean teTargeta(UsuariJpaEntity usuari) {
        if (!(usuari instanceof ClientJpaEntity)) {
            return false;
        }

        String targeta = ((ClientJpaEntity) usuari).getNumtarjetacredit();
        return targeta != null && !targeta.isBlank();
    }

    @Transactional(readOnly = true)
    public List<ReservaJpaEntity> obtenirTotes() {
        try {
            return reservaRepository.findAll();
        } catch (RuntimeException e) {
            registrarError("obtenirTotes", e);
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public ReservaJpaEntity buscarPerId(Long id) throws Exception {
        try {
            return reservaRepository.findById(id)
                    .orElseThrow(() -> new Exception("Reserva no trobada amb l'ID: " + id));
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().startsWith("Reserva no trobada")) {
                logQueryService.registrarWarning("RESERVES", e.getMessage());
            } else {
                registrarError("buscarPerId", e);
            }
            throw e;
        }
    }

    @Transactional
    public void solicitarAnulacio(Long id, String emailClient) throws Exception {
        try {
            ReservaJpaEntity reserva = buscarPerId(id);

            if (reserva.getClient() == null || reserva.getClient().getEmail() == null
                    || !reserva.getClient().getEmail().equalsIgnoreCase(emailClient)) {
                logQueryService.registrarWarning(
                        "RESERVES",
                        "Intent de sol·licitar l'anul·lació d'una reserva que no pertany al client."
                );
                throw new Exception("Només pots sol·licitar l'anul·lació de les teves reserves.");
            }
            if (reserva.getEstat() != EstatReserva.ACTIVA && reserva.getEstat() != EstatReserva.PENDENT) {
                logQueryService.registrarWarning(
                        "RESERVES",
                        "Intent d'anul·lar una reserva amb un estat que no ho permet. ID: " + id
                );
                throw new Exception("Aquesta reserva ja no es pot anul·lar (estat: "
                        + reserva.getEstat().getNomFormatejat() + ").");
            }
            if (reserva.isAnulacioSolicitada()) {
                logQueryService.registrarWarning(
                        "RESERVES",
                        "La sol·licitud d'anul·lació ja estava registrada. ID: " + id
                );
                throw new Exception("Ja has sol·licitat l'anul·lació d'aquesta reserva.");
            }

            reserva.setAnulacioSolicitada(true);
            reservaRepository.save(reserva);

            logQueryService.registrarInfo(
                    "RESERVES",
                    "Sol·licitud d'anul·lació registrada. ID: " + id
            );

        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                registrarError("solicitarAnulacio", e);
            }
            throw e;
        }
    }

    /**
     * L'agent accepta l'anul·lació.
     * 24 hores o més: devolució del 100 %.
     * Menys de 24 hores: devolució del 85 %.
     */
    @Transactional
    public void anullarReserva(Long id) throws Exception {
        ReservaJpaEntity reserva = buscarPerId(id);

        if (!reserva.isAnulacioSolicitada()) {
            throw new Exception("Aquesta reserva no té cap sol·licitud d'anul·lació pendent.");
        }

        if (reserva.getEstat() != EstatReserva.ACTIVA
                && reserva.getEstat() != EstatReserva.PENDENT) {
            throw new Exception("Aquesta reserva no es pot anul·lar en el seu estat actual.");
        }

        if (reserva.getDataInici() == null || reserva.getHoraInici() == null) {
            throw new Exception("No es pot calcular la devolució perquè falta la data o l'hora de recollida.");
        }

        if (reserva.getImportTotal() == null || reserva.getImportTotal() < 0) {
            throw new Exception("L'import total de la reserva no és vàlid.");
        }

        if (reserva.getClient() == null
                || reserva.getClient().getEmail() == null
                || reserva.getClient().getEmail().isBlank()) {
            throw new Exception("La reserva no té un correu electrònic de client vàlid.");
        }

        // Data i hora exactes de recollida.
        LocalDateTime dataHoraRecollida = LocalDateTime.of(
                reserva.getDataInici(),
                reserva.getHoraInici()
        );

        // Hora actual d'Espanya.
        LocalDateTime ara = LocalDateTime.now(ZONA_ESPANYA);

        // Temps exacte que falta per a la recollida.
        Duration tempsRestant = Duration.between(ara, dataHoraRecollida);

        // A partir de 24 hores exactes: 100 %.
        // Per sota de 24 hores: 85 %.
        double percentatgeDevolucio =
                tempsRestant.compareTo(Duration.ofHours(24)) >= 0
                ? 100.0 : 85.0;

        double importTotal = reserva.getImportTotal();

        double importDevolucio = Math.round(
                importTotal * percentatgeDevolucio / 100.0 * 100.0
        ) / 100.0;

        double importRetencio = Math.round(
                (importTotal - importDevolucio) * 100.0
        ) / 100.0;

        // Força la càrrega de vehicle i client dins de la transacció (open-in-view=false)
        if (reserva.getVehicle() != null) {
            reserva.getVehicle().getMatricula();
        }
        if (reserva.getClient() != null) {
            reserva.getClient().getEmail();
        }

        // Anul·lar la reserva.
        reserva.setEstat(EstatReserva.ANUL_LADA);
        reserva.setAnulacioSolicitada(false);
        reservaRepository.save(reserva);

        // Preparar i enviar el correu HTML.
        String asunto = "Anul·lació acceptada - Reserva "
                + reserva.getCodiReserva();

        String contenidoHtml = crearCorreoAnulacioHtml(
                reserva.getCodiReserva(),
                importTotal,
                percentatgeDevolucio,
                importDevolucio,
                importRetencio
        );

        emailService.enviarCorreoHtml(
                reserva.getClient().getEmail(),
                asunto,
                contenidoHtml
        );

        // Snapshot a MongoDB (històric no relacional).
        // Es fa després del correu: si el correu falla, es desfà la transacció
        // i no queda cap anul·lació fantasma a l'històric.
        guardarHistoric(reserva, ACCIO_ANULLADA);
    }

    /**
     * Crea el contingut HTML del correu d'anul·lació.
     */
    private String crearCorreoAnulacioHtml(
            String codiReserva,
            double importTotal,
            double percentatge,
            double devolucio,
            double retencio) {

        Locale locale = Locale.forLanguageTag("es-ES");

        String totalFormat = String.format(locale, "%.2f €", importTotal);
        String devolucioFormat = String.format(locale, "%.2f €", devolucio);
        String retencioFormat = String.format(locale, "%.2f €", retencio);
        String percentatgeFormat = String.format(locale, "%.0f %%", percentatge);

        String colorDevolucio = percentatge == 100.0 ? "#15803d" : "#2563eb";

        return "<!DOCTYPE html>"
                + "<html lang='es'>"
                + "<head><meta charset='UTF-8'></head>"
                + "<body style='margin:0;padding:30px 10px;background-color:#f3f4f6;"
                + "font-family:Arial,Helvetica,sans-serif;color:#1f2937;'>"

                + "<div style='max-width:600px;margin:0 auto;background:#ffffff;"
                + "border-radius:14px;overflow:hidden;border:1px solid #e5e7eb;'>"

                + "<div style='background:#172554;padding:30px 25px;text-align:center;'>"
                + "<h1 style='margin:0;color:#ffffff;font-size:25px;'>"
                + "Solicitud de cancelación aceptada"
                + "</h1>"
                + "<p style='margin:10px 0 0;color:#dbeafe;font-size:14px;'>"
                + "Tu reserva ha sido cancelada correctamente"
                + "</p>"
                + "</div>"

                + "<div style='padding:30px 25px;'>"
                + "<p style='font-size:16px;margin-top:0;'>Hola,</p>"
                + "<p style='font-size:15px;line-height:1.6;color:#4b5563;'>"
                + "Te confirmamos que hemos aceptado tu solicitud de cancelación. "
                + "A continuación encontrarás el detalle del cálculo de la devolución."
                + "</p>"

                + "<div style='background:#f9fafb;border:1px solid #e5e7eb;"
                + "border-radius:10px;padding:18px;margin:24px 0;'>"
                + "<p style='margin:0 0 6px;color:#6b7280;font-size:13px;'>"
                + "CÓDIGO DE RESERVA</p>"
                + "<p style='margin:0;font-size:21px;font-weight:bold;color:#172554;'>"
                + codiReserva
                + "</p>"
                + "</div>"

                + "<h2 style='font-size:17px;margin-bottom:15px;'>Resumen económico</h2>"

                + "<table style='width:100%;border-collapse:collapse;font-size:14px;'>"
                + "<tr>"
                + "<td style='padding:12px 5px;border-bottom:1px solid #e5e7eb;'>"
                + "Importe total de la reserva</td>"
                + "<td style='padding:12px 5px;text-align:right;font-weight:bold;"
                + "border-bottom:1px solid #e5e7eb;'>"
                + totalFormat + "</td></tr>"

                + "<tr>"
                + "<td style='padding:12px 5px;border-bottom:1px solid #e5e7eb;'>"
                + "Porcentaje de devolución</td>"
                + "<td style='padding:12px 5px;text-align:right;font-weight:bold;"
                + "border-bottom:1px solid #e5e7eb;'>"
                + percentatgeFormat + "</td></tr>"

                + "<tr>"
                + "<td style='padding:16px 5px;color:" + colorDevolucio
                + ";font-weight:bold;font-size:15px;'>"
                + "Importe de devolución calculado</td>"
                + "<td style='padding:16px 5px;text-align:right;color:"
                + colorDevolucio + ";font-weight:bold;font-size:20px;'>"
                + devolucioFormat + "</td></tr>"

                + "<tr>"
                + "<td style='padding:12px 5px;color:#6b7280;'>"
                + "Importe retenido</td>"
                + "<td style='padding:12px 5px;text-align:right;color:#6b7280;'>"
                + retencioFormat + "</td></tr>"
                + "</table>"

                + "<div style='margin-top:25px;padding:16px;background:#eff6ff;"
                + "border-left:4px solid #2563eb;border-radius:6px;'>"
                + "<p style='margin:0;font-size:13px;line-height:1.6;color:#1e40af;'>"
                + "<strong>Importante:</strong> este importe es únicamente informativo. "
                + "No se ha realizado ningún pago ni transferencia bancaria."
                + "</p></div>"

                + "<p style='margin-top:28px;font-size:14px;line-height:1.6;"
                + "color:#4b5563;'>Gracias por confiar en nosotros.</p>"
                + "</div>"

                + "<div style='background:#f9fafb;padding:18px;text-align:center;"
                + "border-top:1px solid #e5e7eb;'>"
                + "<p style='margin:0;font-size:12px;color:#9ca3af;'>"
                + "Este es un correo automático. Por favor, no respondas a este mensaje."
                + "</p></div>"

                + "</div></body></html>";
    }

    @Transactional
    public void rebutjarAnulacio(Long id) throws Exception {
        ReservaJpaEntity reserva = buscarPerId(id);

        if (!reserva.isAnulacioSolicitada()) {
            throw new Exception("Aquesta reserva no té cap sol·licitud d'anul·lació pendent.");
        }

        if (reserva.getEstat() != EstatReserva.ACTIVA
                && reserva.getEstat() != EstatReserva.PENDENT) {
            throw new Exception("Aquesta reserva ja no es pot modificar.");
        }

        if (reserva.getClient() == null
                || reserva.getClient().getEmail() == null
                || reserva.getClient().getEmail().isBlank()) {
            throw new Exception("La reserva no té un correu electrònic de client vàlid.");
        }

        reserva.setAnulacioSolicitada(false);
        reservaRepository.save(reserva);

        String asunto = "Sol·licitud d'anul·lació rebutjada - Reserva "
                + reserva.getCodiReserva();

        String contenido = "Hola,\n\n"
                + "T'informem que la teva sol·licitud d'anul·lació de la reserva "
                + reserva.getCodiReserva()
                + " ha estat rebutjada.\n\n"
                + "La reserva continua activa.\n\n"
                + "Si tens dubtes, posa't en contacte amb nosaltres.\n\n"
                + "Gràcies.";

        emailService.enviarCorreo(
                reserva.getClient().getEmail(),
                asunto,
                contenido
        );
    }

    // --- RF54: Filtrar reserves ---
    @Transactional(readOnly = true)
    public List<ReservaJpaEntity> filtrarReserves(String codi, LocalDate data, String matricula, String email) {
        try {
            return reservaRepository.cercarAmbFiltres(codi, data, matricula, email);
        } catch (RuntimeException e) {
            registrarError("filtrarReserves", e);
            throw e;
        }
    }

    /**
     * Desa un document nou a MongoDB amb la "foto" actual de la reserva.
     * Si MongoDB falla, no interromp l'operació principal (MySQL ja s'ha guardat).
     */
    private void guardarHistoric(ReservaJpaEntity reserva, String accio) {
        try {
            HistoricReservaDocument doc = new HistoricReservaDocument();
            doc.setAccio(accio);
            doc.setDataEvent(Instant.now());
            doc.setReservaId(reserva.getId());
            doc.setCodiReserva(reserva.getCodiReserva());
            doc.setDataInici(reserva.getDataInici());
            doc.setDataFi(reserva.getDataFi());
            doc.setImportTotal(reserva.getImportTotal());
            doc.setEstat(reserva.getEstat() != null ? reserva.getEstat().name() : null);

            VehicleJpaEntity vehicle = reserva.getVehicle();
            if (vehicle != null) {
                doc.setVehicleId(vehicle.getId());
                doc.setMatricula(vehicle.getMatricula());
                doc.setMarca(vehicle.getMarca());
                doc.setModel(vehicle.getModel());
            }

            UsuariJpaEntity client = reserva.getClient();
            if (client != null) {
                doc.setClientId(client.getId());
                String nomComplet = ((client.getNom() != null ? client.getNom() : "")
                        + " " + (client.getCognoms() != null ? client.getCognoms() : "")).trim();
                doc.setClientNom(nomComplet.isEmpty() ? client.getEmail() : nomComplet);
                doc.setClientEmail(client.getEmail());
            }

            historicReservaRepository.save(doc);
        } catch (Exception e) {
            log.error("No s'ha pogut desar l'històric de la reserva {} a MongoDB (acció {})",
                    reserva.getId(), accio, e);
        }
    }

    // --- RF91: Informe de reserves per client des de MongoDB (històric) ---

    /**
     * Llista de reserves de l'històric (MongoDB) per a un o més clients, o tots.
     * Ordenat per nom de client i data d'inici (més recent primer).
     * Només es consideren els esdeveniments d'acció CREADA (una fila per reserva).
     */
    public List<HistoricReservaDocument> obtenirInformeHistoric(Collection<Long> clientIds) {
        if (clientIds == null || clientIds.isEmpty()) {
            return historicReservaRepository.findByAccioOrderByClientNomAscDataIniciDesc(ACCIO_CREADA);
        }
        return historicReservaRepository
                .findByClientIdInAndAccioOrderByClientNomAscDataIniciDesc(clientIds, ACCIO_CREADA);
    }

    public List<HistoricReservaDocument> obtenirHistoricClient(Long clientId) {
        if (clientId == null) {
            return Collections.emptyList();
        }
        return historicReservaRepository
                .findByClientIdAndAccioOrderByDataIniciDesc(clientId, ACCIO_CREADA);
    }

    public long comptarReservesHistoric(Collection<Long> clientIds) {
        if (clientIds == null || clientIds.isEmpty()) {
            return historicReservaRepository.countByAccio(ACCIO_CREADA);
        }
        return historicReservaRepository.countByClientIdInAndAccio(clientIds, ACCIO_CREADA);
    }

    public double totalImportHistoric(Collection<Long> clientIds) {
        List<HistoricReservaDocument> llista = obtenirInformeHistoric(clientIds);
        return llista.stream()
                .map(HistoricReservaDocument::getImportTotal)
                .filter(i -> i != null)
                .mapToDouble(Double::doubleValue)
                .sum();
    }

    /** Compatibilitat amb codi antic (compta des de Mongo). */
    public long comptarReservesClient(Long clientId) {
        if (clientId == null) {
            return 0L;
        }
        return historicReservaRepository.countByClientIdAndAccio(clientId, ACCIO_CREADA);
    }

    /**
     * RF49 - Lliurar vehicle (AMB FOTOS OBLIGATÒRIES).
     */
    @Transactional
    public void lliurarVehicle(Long reservaId, Integer kmsInicials, String observacions,
                               MultipartFile foto1, MultipartFile foto2, MultipartFile foto3) throws Exception {

        try {
            ReservaJpaEntity reserva = reservaRepository.findById(reservaId)
                    .orElseThrow(() -> new Exception("Reserva no trobada: " + reservaId));

            validarEstatPerLliurar(reserva);

            if (foto1 == null || foto1.isEmpty()) {
                throw new Exception("És obligatori pujar almenys una foto de l'estat del vehicle al lliurar.");
            }

            EntregaJpaEntity entrega = new EntregaJpaEntity(reserva, LocalDateTime.now(), kmsInicials, observacions);

            guardarFoto(foto1, entrega::setFotoLliurament1, entrega::setFotoLliurament1Tipus);
            if (foto2 != null && !foto2.isEmpty()) {
                guardarFoto(foto2, entrega::setFotoLliurament2, entrega::setFotoLliurament2Tipus);
            }
            if (foto3 != null && !foto3.isEmpty()) {
                guardarFoto(foto3, entrega::setFotoLliurament3, entrega::setFotoLliurament3Tipus);
            }

            reserva.setEntrega(entrega);
            reserva.setEstat(EstatReserva.EN_CURS);
            reservaRepository.save(reserva);

        } catch (Exception e) {
            registrarError("lliurarVehicle", e);
            throw e;
        }
    }

    /**
     * RF48 - Retornar vehicle (AMB KM FINALS + FOTOS OBLIGATÒRIES).
     */
    @Transactional
    public void retornarVehicle(Long reservaId, Integer quilometresFinals, String observacionsRetorn,
                                MultipartFile foto1, MultipartFile foto2, MultipartFile foto3) throws Exception {

        try {
            ReservaJpaEntity reserva = reservaRepository.findById(reservaId)
                    .orElseThrow(() -> new Exception("Reserva no trobada: " + reservaId));

            if (reserva.getEstat() != EstatReserva.EN_CURS) {
                throw new Exception("Només es pot retornar un vehicle amb estat EN_CURS. Estat actual: " + reserva.getEstat());
            }
            if (reserva.getEntrega() == null) {
                throw new Exception("La reserva no té dades de lliurament.");
            }
            if (reserva.getEntrega().isRetornat()) {
                throw new Exception("Aquest vehicle ja ha estat retornat el " + reserva.getEntrega().getDataRetorn());
            }
            if (quilometresFinals == null) {
                throw new Exception("Els quilòmetres finals són obligatoris al retornar el vehicle.");
            }
            if (quilometresFinals < reserva.getEntrega().getQuilometresInicials()) {
                throw new Exception("Els quilòmetres finals no poden ser menors que els inicials ("
                        + reserva.getEntrega().getQuilometresInicials() + ").");
            }
            if (foto1 == null || foto1.isEmpty()) {
                throw new Exception("És obligatori pujar almenys una foto de l'estat del vehicle al retornar.");
            }

            EntregaJpaEntity entrega = reserva.getEntrega();
            entrega.setDataRetorn(LocalDateTime.now());
            entrega.setQuilometresFinals(quilometresFinals);
            entrega.setObservacionsRetorn(observacionsRetorn);

            guardarFoto(foto1, entrega::setFotoRetorn1, entrega::setFotoRetorn1Tipus);
            if (foto2 != null && !foto2.isEmpty()) {
                guardarFoto(foto2, entrega::setFotoRetorn2, entrega::setFotoRetorn2Tipus);
            }
            if (foto3 != null && !foto3.isEmpty()) {
                guardarFoto(foto3, entrega::setFotoRetorn3, entrega::setFotoRetorn3Tipus);
            }

            reserva.setEstat(EstatReserva.FINALITZADA);
            reservaRepository.save(reserva);

        } catch (Exception e) {
            registrarError("retornarVehicle", e);
            throw e;
        }
    }

    private void validarEstatPerLliurar(ReservaJpaEntity reserva) throws Exception {
        if (reserva.getEstat() == EstatReserva.ANUL_LADA) throw new Exception("Reserva anul·lada.");
        if (reserva.getEstat() == EstatReserva.EN_CURS) throw new Exception("Ja en curs.");
        if (reserva.getEstat() == EstatReserva.FINALITZADA) throw new Exception("Reserva finalitzada.");
    }

    private void guardarFoto(MultipartFile file, java.util.function.Consumer<byte[]> setDades,
                             java.util.function.Consumer<String> setTipus) {
        try {
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new IllegalArgumentException("La foto supera 5MB.");
            }
            String ct = file.getContentType();
            if (ct == null || !(ct.equals("image/jpeg") || ct.equals("image/png") || ct.equals("image/webp"))) {
                throw new IllegalArgumentException("Format no vàlid. Només JPG, PNG, WEBP.");
            }
            setDades.accept(file.getBytes());
            setTipus.accept(ct);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error llegint la foto: " + e.getMessage());
        }
    }

    private void registrarError(String operacio, Exception e) {
        logQueryService.registrarError(
                "RESERVES",
                "Error en " + operacio + ": " + e.getMessage()
        );
    }
}
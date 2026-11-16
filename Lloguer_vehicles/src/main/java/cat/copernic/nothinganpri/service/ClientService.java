
package cat.copernic.nothinganpri.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import cat.copernic.nothinganpri.dto.DocumentClient;
import cat.copernic.nothinganpri.entity.ClientJpaEntity;
import cat.copernic.nothinganpri.enums.EstatReserva;
import cat.copernic.nothinganpri.repository.ClientRepository;
import cat.copernic.nothinganpri.repository.ReservaJpaRepository;

import jakarta.transaction.Transactional;

/**
 * Servei que gestiona la lògica de negoci dels clients.
 */
@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ReservaJpaRepository reservaRepository;

    @Autowired
    private LogQueryService logQueryService;

    /**
     * Obtiene todos los clientes activos.
     *
     * @return lista de clientes activos
     */
    public List<ClientJpaEntity> llistarTots() {
        try {
            return clientRepository.findByRolAndActivoTrue("CLIENT");

        } catch (RuntimeException e) {
            registrarError("llistarTots", e);
            throw e;
        }
    }

    /**
     * Convierte los valores vacíos o nulos en null y elimina espacios.
     *
     * @param valor texto que se quiere tratar
     * @return texto sin espacios sobrantes o null
     */
    private String buitANull(String valor) {
        return valor == null || valor.trim().isEmpty()
                ? null
                : valor.trim();
    }

    /**
     * Busca clientes aplicando diferentes filtros.
     *
     * @param dni DNI del cliente
     * @param cadDNI caducidad del DNI
     * @param llicencia número de licencia
     * @param cadLlicencia caducidad de la licencia
     * @param adreca dirección del cliente
     * @param nacionalitat nacionalidad del cliente
     * @param activo estado del cliente
     * @return lista de clientes que cumplen los filtros
     */
    public List<ClientJpaEntity> cercarFiltres(
            String dni,
            String cadDNI,
            String llicencia,
            String cadLlicencia,
            String adreca,
            String nacionalitat,
            Boolean activo) {

        try {
            return clientRepository.cercarClients(
                    buitANull(dni),
                    buitANull(cadDNI),
                    buitANull(llicencia),
                    buitANull(cadLlicencia),
                    buitANull(adreca),
                    buitANull(nacionalitat),
                    activo,
                    "CLIENT");

        } catch (RuntimeException e) {
            registrarError("cercarFiltres", e);
            throw e;
        }
    }

    /**
     * Busca un cliente mediante su DNI.
     *
     * @param DNI DNI del cliente
     * @return cliente encontrado o un Optional vacío
     */
    public Optional<ClientJpaEntity> findByDNI(String DNI) {
        try {
            return clientRepository.findByDNIAndRol(DNI, "CLIENT");

        } catch (RuntimeException e) {
            registrarError("findByDNI", e);
            throw e;
        }
    }

    /**
     * Busca un cliente mediante su identificador.
     *
     * @param id identificador del cliente
     * @return cliente encontrado o un Optional vacío
     */
    public Optional<ClientJpaEntity> findById(Long id) {
        try {
            return clientRepository.findById(id);

        } catch (RuntimeException e) {
            registrarError("findById", e);
            throw e;
        }
    }

    /**
     * Desactiva un cliente sin eliminarlo de la base de datos.
     * No permite la desactivación si tiene reservas activas o pendientes.
     *
     * @param id identificador del cliente
     * @return true si se ha desactivado, false si no se puede
     */
    @Transactional
    public boolean eliminarClient(Long id) {

        try {
            Optional<ClientJpaEntity> client = clientRepository.findById(id);

            if (client.isEmpty()
                    || !"CLIENT".equals(client.get().getRol())) {

                logQueryService.registrarWarning(
                        "CLIENTS",
                        "No s'ha pogut desactivar el client: no existeix o no té el rol CLIENT."
                );

                return false;
            }

            boolean tieneReservasActivas =
                    reservaRepository.existsByClient_IdAndEstatIn(
                            id,
                            Arrays.asList(
                                    EstatReserva.PENDENT,
                                    EstatReserva.ACTIVA,
                                    EstatReserva.EN_CURS));

            if (tieneReservasActivas) {

                logQueryService.registrarWarning(
                        "CLIENTS",
                        "No s'ha pogut desactivar el client perquè té reserves actives o pendents."
                );

                return false;
            }

            client.get().setActivo(false);
            clientRepository.save(client.get());

            logQueryService.registrarInfo(
                    "CLIENTS",
                    "Client desactivat correctament. ID: " + id
            );

            return true;

        } catch (RuntimeException e) {
            registrarError("eliminarClient", e);
            throw e;
        }
    }

    /**
     * Reactiva un cliente previamente desactivado.
     *
     * @param id identificador del cliente
     */
    @Transactional
    public void activarClient(Long id) {

        try {
            Optional<ClientJpaEntity> client = clientRepository.findById(id);

            if (client.isPresent()
                    && "CLIENT".equals(client.get().getRol())) {

                client.get().setActivo(true);
                clientRepository.save(client.get());

                logQueryService.registrarInfo(
                        "CLIENTS",
                        "Client reactivat correctament. ID: " + id
                );

            } else {
                logQueryService.registrarWarning(
                        "CLIENTS",
                        "No s'ha pogut reactivar el client: no existeix o no té el rol CLIENT."
                );
            }

        } catch (RuntimeException e) {
            registrarError("activarClient", e);
            throw e;
        }
    }

    /**
     * Obtiene la imagen del DNI del cliente.
     *
     * @param dni DNI del cliente
     * @return documento con la imagen y su tipo, o un Optional vacío
     */
    public Optional<DocumentClient> obtenirDni(String dni) {

        try {
            Optional<ClientJpaEntity> resultat = findByDNI(dni);

            if (resultat.isEmpty()) {
                return Optional.empty();
            }

            ClientJpaEntity client = resultat.get();

            if (client.getFotoDniAnverso() == null) {
                return Optional.empty();
            }

            return Optional.of(
                    new DocumentClient(
                            client.getFotoDniAnverso(),
                            client.getFotoDniAnversoTipus()));

        } catch (RuntimeException e) {
            registrarError("obtenirDni", e);
            throw e;
        }
    }

    /**
     * Obtiene la imagen del permiso de conducir del cliente.
     *
     * @param dni DNI del cliente
     * @return documento con la imagen y su tipo, o un Optional vacío
     */
    public Optional<DocumentClient> obtenirLlicencia(String dni) {

        try {
            Optional<ClientJpaEntity> resultat = findByDNI(dni);

            if (resultat.isEmpty()) {
                return Optional.empty();
            }

            ClientJpaEntity client = resultat.get();

            if (client.getFotoPermiso() == null) {
                return Optional.empty();
            }

            return Optional.of(
                    new DocumentClient(
                            client.getFotoPermiso(),
                            client.getFotoPermisoTipus()));

        } catch (RuntimeException e) {
            registrarError("obtenirLlicencia", e);
            throw e;
        }
    }

    /**
     * Guarda los cambios de un cliente en la base de datos.
     *
     * @param client cliente que se quiere guardar
     * @return cliente guardado
     */
    @Transactional
    public ClientJpaEntity guardarClient(ClientJpaEntity client) {

        try {
            ClientJpaEntity clientGuardat = clientRepository.save(client);

            logQueryService.registrarInfo(
                    "CLIENTS",
                    "Client actualitzat correctament. ID: "
                            + clientGuardat.getId());

            return clientGuardat;

        } catch (RuntimeException e) {
            registrarError("guardarClient", e);
            throw e;
        }
    }

    /**
     * Actualiza los datos personales y los documentos de un cliente.
     *
     * @param dni DNI del cliente que se quiere modificar
     * @param datos nuevos datos del cliente
     * @param fotoDni nueva fotografía del DNI
     * @param fotoPermiso nueva fotografía del permiso de conducir
     * @return cliente actualizado
     * @throws IOException si ocurre un error al leer las fotografías
     */
    @Transactional
    public ClientJpaEntity actualizarClientAdmin(
            String dni,
            ClientJpaEntity datos,
            MultipartFile fotoDni,
            MultipartFile fotoPermiso) throws IOException {

        ClientJpaEntity client = findByDNI(dni)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No s'ha trobat el client."));

        try {
            // Actualizar datos personales
            client.setNom(datos.getNom());
            client.setCognoms(datos.getCognoms());
            client.setTelefon(datos.getTelefon());
            client.setEmail(datos.getEmail());
            client.setAdreca(datos.getAdreca());
            client.setNacionalitat(datos.getNacionalitat());

            // Actualizar documentación
            client.setCadDNI(datos.getCadDNI());
            client.setLliconduir(datos.getLliconduir());
            client.setCdLLiconduir(datos.getCdLLiconduir());

            // Actualizar foto del DNI si se ha seleccionado una
            if (fotoDni != null && !fotoDni.isEmpty()) {

                validarImatge(fotoDni);

                client.setFotoDniAnverso(fotoDni.getBytes());
                client.setFotoDniAnversoTipus(fotoDni.getContentType());
            }

            // Actualizar foto del permiso si se ha seleccionado una
            if (fotoPermiso != null && !fotoPermiso.isEmpty()) {

                validarImatge(fotoPermiso);

                client.setFotoPermiso(fotoPermiso.getBytes());
                client.setFotoPermisoTipus(fotoPermiso.getContentType());
            }

            return guardarClient(client);

        } catch (IOException | RuntimeException e) {
            registrarError("actualizarClientAdmin", e);
            throw e;
        }
    }

    /**
     * Comprueba que el archivo sea una imagen y no supere los 5 MB.
     *
     * @param fitxer archivo que se quiere comprobar
     */
    private void validarImatge(MultipartFile fitxer) {

        if (fitxer.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException(
                    "La imatge no pot superar els 5 MB.");
        }

        String tipus = fitxer.getContentType();

        if (tipus == null || !tipus.startsWith("image/")) {
            throw new IllegalArgumentException(
                    "El fitxer seleccionat ha de ser una imatge.");
        }
    }

    /**
     * Registra un error en el sistema de logs.
     *
     * @param operacio nombre de la operación que ha fallado
     * @param e excepción producida
     */
    private void registrarError(String operacio, Exception e) {

        logQueryService.registrarError(
                "CLIENTS",
                "Error en " + operacio + ": " + e.getMessage());
    }
}

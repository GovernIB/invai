package es.caib.invai.api.interna.controller.soffid;

import es.caib.invai.api.interna.rest.soffid.SoffidClient;
import es.caib.invai.api.interna.rest.soffid.SoffidPageResponse;
import es.caib.invai.api.interna.rest.soffid.SoffidRole;
import es.caib.invai.api.interna.rest.soffid.SoffidUser;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes Soffid personnel/role lookups to {@code invai-back}.
 *
 * @since 1.0.4
 */
@Tag(name = "Soffid", description = "Cerca de personal, rols i grups de Soffid.")
@RestController
@RequestMapping("soffid")
@RequiredArgsConstructor
public class SoffidController {

    /** Client performing the real calls to Soffid's SCIM 2.0 API. */
    private final SoffidClient soffidClient;

    /**
     * Lists active Soffid users, optionally restricted to those matching every word of {@code
     * search} in either the full name or the username.
     *
     * @param search the text to search for, or blank to list every active user
     * @param page   0-based page number
     * @param size   page size
     * @return the requested page of matching Soffid users
     */
    @GetMapping("/users")
    public SoffidPageResponse<SoffidUser> searchUsers(@RequestParam(required = false) String search,
                                                        @RequestParam int page,
                                                        @RequestParam int size) {
        Page<SoffidUser> result = soffidClient.searchUsers(search, PageRequest.of(page, size));
        return new SoffidPageResponse<>(result.getContent(), result.getTotalElements());
    }

    /**
     * Looks up a single active Soffid user by exact e-mail address.
     *
     * @param email the exact e-mail address to look up
     * @return the matching active Soffid user, or 204 No Content if none is found
     */
    @GetMapping("/users/by-email")
    public ResponseEntity<SoffidUser> searchByEmail(@RequestParam String email) {
        SoffidUser user = soffidClient.searchByEmail(email);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.noContent().build();
    }

    /**
     * Lists Soffid roles that can be assigned to an application, optionally restricted to those
     * matching every word of {@code name}.
     *
     * @param name the text to search for, or blank to list every role
     * @param page 0-based page number
     * @param size page size
     * @return the requested page of matching Soffid roles
     */
    @GetMapping("/roles")
    public SoffidPageResponse<SoffidRole> searchRoles(@RequestParam(required = false) String name,
                                                        @RequestParam int page,
                                                        @RequestParam int size) {
        Page<SoffidRole> result = soffidClient.searchRoles(name, PageRequest.of(page, size));
        return new SoffidPageResponse<>(result.getContent(), result.getTotalElements());
    }
}

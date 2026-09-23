package es.caib.invai.api.interna.controller.dir3;

import es.caib.invai.api.interna.rest.dir3.Dir3CaibTreeCache;
import es.caib.invai.api.interna.rest.dir3.UnidadRest;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Exposes DIR3CAIB's organizational unit tree to {@code invai-back}.
 *
 * @since 1.0.4
 */
@Tag(name = "DIR3CAIB", description = "Consulta de l'arbre d'unitats organitzatives de DIR3CAIB.")
@RestController
@RequestMapping("dir3")
@RequiredArgsConstructor
public class Dir3CaibController {

    /** Serves the tree from a proactively refreshed cache when possible, see {@link Dir3CaibTreeCache}. */
    private final Dir3CaibTreeCache dir3CaibTreeCache;

    /**
     * Fetches the full, flattened tree of organizational units under the given root code.
     *
     * @param rootCode   the root unit's code
     * @param coOfficial whether to prefer the co-official denomination when one exists
     * @return every descendant unit (root included), or an empty list if the root itself is not found
     */
    @GetMapping("/tree")
    public List<UnidadRest> getTree(@RequestParam String rootCode, @RequestParam boolean coOfficial) {
        return dir3CaibTreeCache.getTree(rootCode, coOfficial);
    }
}

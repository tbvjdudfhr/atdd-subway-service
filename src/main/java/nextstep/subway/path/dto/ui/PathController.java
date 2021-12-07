package nextstep.subway.path.dto.ui;

import nextstep.subway.path.dto.application.PathService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/paths")
public class PathController {
    private final PathService pathService;

    public PathController(PathService pathService) {
        this.pathService = pathService;
    }

    @GetMapping
    public ResponseEntity findPaths(@RequestParam Long source, @RequestParam Long target) {
        pathService.findPaths(source, target);
        return ResponseEntity.ok().build();
    }
}

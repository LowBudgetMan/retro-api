package io.nickreuter.retroapi.team;

import io.nickreuter.retroapi.team.exception.TeamAlreadyExistsException;
import io.nickreuter.retroapi.team.exception.TeamNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/team")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @PostMapping
    public ResponseEntity<Void> createTeam(@RequestBody CreateTeamRequest request, Principal principal) throws TeamAlreadyExistsException {
        var team = teamService.createTeam(request.name(), principal.getName());
        return ResponseEntity.created(URI.create("/api/team/%s".formatted(team.getId()))).build();
    }

    @GetMapping
    public List<TeamEntity> getTeamsForUser(Principal principal) {
        return teamService.getTeamsForUser(principal.getName());
    }

    @GetMapping("/{teamId}")
    public TeamEntity getTeam(@PathVariable UUID teamId) throws TeamNotFoundException {
        return teamService.getTeam(teamId).orElseThrow(TeamNotFoundException::new);
    }

    @ExceptionHandler(TeamAlreadyExistsException.class)
    public ResponseEntity<Void> handleTeamAlreadyExists() {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @ExceptionHandler(TeamNotFoundException.class)
    public ResponseEntity<Void> handleTeamNotFound() {
        return ResponseEntity.notFound().build();
    }
}

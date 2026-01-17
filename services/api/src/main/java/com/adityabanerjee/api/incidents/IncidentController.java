package com.adityabanerjee.api.incidents;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigInteger;

import com.adityabanerjee.api.incidents.responses.CreateIncidentResponse;

@RestController
@RequestMapping("/incidents")
public class IncidentController {

    @PostMapping
    public ResponseEntity<CreateIncidentResponse> createIncident(@RequestBody Incident incident) {
        // TODO: Implement the logic to create an incident

        BigInteger workflowRunId = BigInteger.valueOf((int) (Math.random() * Integer.MAX_VALUE));
        CreateIncidentResponse response = new CreateIncidentResponse(workflowRunId);

        return ResponseEntity.ok(response);
    }
}